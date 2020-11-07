package fi.sangre.renesans.model;

import lombok.*;

import java.util.List;
import java.util.TimeZone;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultilingualPhraseQuery {

    private List<Long> ids;
    private List<String> keys;
    private String locale;
    private List<String> args;
    private TimeZone tz;

    private static final String TZ_UTC = "UTC";

    public boolean hasIds() {
        return ids != null && !ids.isEmpty();
    }

    public boolean hasKeys() {
        return keys != null && !keys.isEmpty();
    }

    public boolean hasArgs() {
        return args != null && !args.isEmpty();
    }

    public boolean hasLocale() {
        return locale != null;
    }

    public String getLocale() {
        return locale;
    }

    public TimeZone getTimezone() {
        return tz != null ? tz : TimeZone.getTimeZone(TZ_UTC);
    }
}
