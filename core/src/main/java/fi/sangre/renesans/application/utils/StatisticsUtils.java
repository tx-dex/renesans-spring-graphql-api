package fi.sangre.renesans.application.utils;

import org.springframework.lang.Nullable;

public class StatisticsUtils {
    private static final Double HUNDRED_D = 100d;
    public final static Double MAX_ANSWER_VALUE = 4d;

    @Nullable
    public static Long engagementRatio(Long participantsAnsweredCount, Long allParticipantsCount) {
        Double engagementRatio = rateToPercent((double) participantsAnsweredCount / allParticipantsCount);
        return engagementRatio == null ? null : Math.round(engagementRatio);
    }

    @Nullable
    public static Double rateToPercent(@Nullable final Double result) {
        if (result != null) {
            return HUNDRED_D * result;
        } else {
            return null;
        }
    }

    @Nullable
    public static Double indexToRate(@Nullable final Double value) {
        if (value == null) {
            return null;
        } else {
            return value / MAX_ANSWER_VALUE;
        }
    }

    @Nullable
    public static Integer indexToRate(@Nullable final Integer value) {
        if (value == null) {
            return null;
        } else {
            return Double.valueOf(value / MAX_ANSWER_VALUE).intValue();
        }
    }
}
