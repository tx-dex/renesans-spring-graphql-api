package fi.sangre.renesans.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class RequestUserPasswordResetEvent {
    private final Long userId;
    private final String username;
    private final String email;
    private final String locale;
}
