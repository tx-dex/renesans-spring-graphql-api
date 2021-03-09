package fi.sangre.renesans.aaa;

import fi.sangre.renesans.application.dao.GuestDao;
import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.TokenUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.SurveyGuest;
import fi.sangre.renesans.service.OrganizationSurveyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final OrganizationSurveyService organizationSurveyService;
    private final GuestDao guestDao;
    private final UserPrincipalService userPrincipalService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain) throws ServletException, IOException {
        AuthenticationException authenticationError = null;

        try {
            final Jws<Claims> token = Optional.ofNullable(getTokenFromRequest(request))
                    .map(tokenService::getClaims)
                    .orElse(null);
            if (token != null) {
                final UserDetails userDetails;
                if (TokenUtils.isQuestionnaireToken(token.getBody())) {
                    final IdValueObject<UUID> id = TokenUtils.getQuestionnaireUserId(token.getBody());
                    if (id instanceof RespondentId) {
                        final RespondentId respondentId = (RespondentId) id;
                        final Respondent respondent = organizationSurveyService.getRespondent(respondentId);
                        userDetails = new RespondentPrincipal(respondentId,
                                respondent.getEmail(),
                                respondent.getSurveyId());
                    } else if (id instanceof GuestId) {
                        final GuestId guestId = (GuestId) id;
                        userDetails = guestDao.getGuest(guestId, this::getGuest);
                    } else {
                        throw new SurveyException("Invalid id");
                    }
                } else {
                    final Long userId = TokenUtils.getUserId(token.getBody());
                    userDetails = userPrincipalService.loadUserById(userId);
                }

                final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (final AuthenticationException ex) {
            log.warn("Can't set security context. {}", ex.getMessage());
            authenticationError = ex;
        } catch (final Exception ex) {
            log.warn("Can't set security context. Unknown exception. Should verify it", ex);
            authenticationError = new AuthenticationServiceException("Unknown exception", ex);
        }

        try {
            if (authenticationError != null) {
                customAuthenticationEntryPoint.commence(request, response, authenticationError);
            } else {
                filterChain.doFilter(request, response);
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @NonNull
    private GuestPrincipal getGuest(@NonNull final SurveyGuest entity) {
        return new GuestPrincipal(new GuestId(entity.getId()), entity.getEmail(), new SurveyId(entity.getSurveyId()));
    }
}