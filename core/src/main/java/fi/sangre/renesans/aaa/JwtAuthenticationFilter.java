package fi.sangre.renesans.aaa;

import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.TokenUtils;
import fi.sangre.renesans.service.OrganizationSurveyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
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

@RequiredArgsConstructor
@Slf4j

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final OrganizationSurveyService organizationSurveyService;
    private final UserPrincipalService userPrincipalService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain) throws ServletException, IOException {

        try {
            final Jws<Claims> token = Optional.ofNullable(getTokenFromRequest(request))
                    .map(tokenService::getClaims)
                    .orElse(null);
            if (token != null) {
                final UserDetails userDetails;
                if (TokenUtils.isQuestionnaireToken(token.getBody())) {
                    final RespondentId respondentId = TokenUtils.getRespondent(token.getBody());
                    final Respondent respondent = organizationSurveyService.getRespondent(respondentId);
                    userDetails = new RespondentPrincipal(respondentId,
                            respondent.getEmail(),
                            respondent.getSurveyId());
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
            jwtAuthenticationEntryPoint.commence(request, response, ex); //TODO: Fix CORS headers when getting 401 there are no CORS headers in the response
        } catch (final Exception ex) {
            log.warn("Can't set security context. Unknown exception. Should verify it", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}