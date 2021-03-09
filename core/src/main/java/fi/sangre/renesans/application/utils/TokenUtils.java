package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import io.jsonwebtoken.Claims;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.UUID;

import static fi.sangre.renesans.service.TokenService.QUESTIONNAIRE_GUEST_USER;
import static fi.sangre.renesans.service.TokenService.QUESTIONNAIRE_USER_TYPE;

public class TokenUtils {
    public static final String SUBJECT_CLAIM_KEY = "sub";
    public static final String TOKEN_TYPE_CLAIM_KEY = "type";
    public static final String PASSWORD_RESET_CLAIM_KEY = "password_reset";
    public static final String ACCOUNT_ACTIVATION_CLAIM_KEY = "account_activation";
    public static final String QUESTIONNAIRE_TOKEN_TYPE = "questionnaire";

    public static boolean isQuestionnaireToken(@Nullable final Claims claims) {
        if (claims != null) {
            return QUESTIONNAIRE_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM_KEY));
        } else {
            return false;
        }
    }

    public static boolean isPasswordResetToken(@Nullable final Claims claims) {
        if (claims != null) {
            return claims.containsKey(PASSWORD_RESET_CLAIM_KEY) && claims.get(PASSWORD_RESET_CLAIM_KEY, Boolean.class);
        } else {
            return false;
        }
    }

    public static boolean isUserActivationToken(@Nullable final Claims claims) {
        if (claims != null) {
            return claims.containsKey(ACCOUNT_ACTIVATION_CLAIM_KEY) && claims.get(ACCOUNT_ACTIVATION_CLAIM_KEY, Boolean.class);
        } else {
            return false;
        }
    }

    @NonNull
    public static Long getUserId(@Nullable final Claims claims) {
        return Optional.ofNullable(claims)
                .map(claim -> claim.get(SUBJECT_CLAIM_KEY, String.class))
                .map(Long::parseLong)
                .orElseThrow(() -> new RuntimeException("Cannot get user id from token"));
    }

    @NonNull
    public static IdValueObject<UUID> getQuestionnaireUserId(@Nullable final Claims claims) {
        final boolean isGuest = Optional.ofNullable(claims)
                .map(claim -> claim.get(QUESTIONNAIRE_USER_TYPE, String.class))
                .map(QUESTIONNAIRE_GUEST_USER::equalsIgnoreCase)
                .orElse(false);

        if (isGuest) {
            return getGuestId(claims);
        } else {
            return getRespondentId(claims);
        }
    }

    @NonNull
    private static GuestId getGuestId(@Nullable final Claims claims) {
        return Optional.ofNullable(claims)
                .map(claim -> claim.get(SUBJECT_CLAIM_KEY, String.class))
                .map(UUID::fromString)
                .map(GuestId::new)
                .orElseThrow(() -> new RuntimeException("Cannot get guest id from token"));
    }

    @NonNull
    private static RespondentId getRespondentId(@Nullable final Claims claims) {
        return Optional.ofNullable(claims)
                .map(claim -> claim.get(SUBJECT_CLAIM_KEY, String.class))
                .map(UUID::fromString)
                .map(RespondentId::new)
                .orElseThrow(() -> new RuntimeException("Cannot get respondent id from token"));
    }
}
