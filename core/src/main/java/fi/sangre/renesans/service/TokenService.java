package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static fi.sangre.renesans.application.utils.TokenUtils.*;

@RequiredArgsConstructor
@Slf4j

@Service
public class TokenService {
    public static final String QUESTIONNAIRE_USER_TYPE = "user";
    public static final String QUESTIONNAIRE_RESPONDENT_USER = "respondent";
    public static final String QUESTIONNAIRE_GUEST_USER = "guest";

    private static final Map<String, Object> QUESTIONNAIRE_TOKEN_CLAIMS = ImmutableMap.of(
            TOKEN_TYPE_CLAIM_KEY, QUESTIONNAIRE_TOKEN_TYPE);
    private static final Map<String, Object> RESET_PASSWORD_TOKEN_CLAIMS = ImmutableMap.of(
            PASSWORD_RESET_CLAIM_KEY, true);
    private static final Map<String, Object> USER_ACTIVATION_TOKEN_CLAIMS = ImmutableMap.of(
            PASSWORD_RESET_CLAIM_KEY, true,
            ACCOUNT_ACTIVATION_CLAIM_KEY, true);

    private final JwtTokenService jwtTokenService;
    private final OrganizationSurveyService organizationSurveyService;

    @Value("#{T(java.time.Duration).ofMillis('${spring.security.jwt.reset.token.expirationTimeInMs}')}")
    private Duration resetTokenDuration;
    @Value("#{T(java.time.Duration).ofMillis('${spring.security.jwt.activation.token.expirationTimeInMs}')}")
    private Duration activationTokenDuration;

    @NonNull
    public String generateQuestionnaireToken(@NonNull final RespondentId id) {
        final Map<String, Object> claims = new HashMap<>(QUESTIONNAIRE_TOKEN_CLAIMS);
        claims.put(QUESTIONNAIRE_USER_TYPE, QUESTIONNAIRE_RESPONDENT_USER);

        return jwtTokenService.generateToken(id.asString()
                , claims
                , Duration.ofDays(7).toMillis());
    }

    @NonNull
    public String generateQuestionnaireToken(@NonNull final GuestId id) {
        final Map<String, Object> claims = new HashMap<>(QUESTIONNAIRE_TOKEN_CLAIMS);
        claims.put(QUESTIONNAIRE_USER_TYPE, QUESTIONNAIRE_GUEST_USER);

        return jwtTokenService.generateToken(id.asString()
                , claims
                , Duration.ofDays(7).toMillis());
    }

    @NonNull
    public String generatePasswordResetToken(@NonNull final Long userId) {
        return jwtTokenService.generateToken(userId.toString(), RESET_PASSWORD_TOKEN_CLAIMS, resetTokenDuration.toMillis());
    }

    @NonNull
    public String generateUserActivationToken(@NonNull final Long userId) {
        return jwtTokenService.generateToken(userId.toString(), USER_ACTIVATION_TOKEN_CLAIMS, activationTokenDuration.toMillis());
    }

    public Duration getResetTokenExpirationDuration() {
        return resetTokenDuration;
    }

    public Duration getActivationTokenDuration() {
        return activationTokenDuration;
    }
}
