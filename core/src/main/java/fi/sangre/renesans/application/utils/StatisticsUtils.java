package fi.sangre.renesans.application.utils;

import org.springframework.lang.Nullable;

public class StatisticsUtils {
    private static final Double HUNDRED_D = 100d;

    @Nullable
    public static Double rateToPercent(@Nullable final Double result) {
        if (result != null) {
            return HUNDRED_D * result;
        } else {
            return null;
        }
    }
}
