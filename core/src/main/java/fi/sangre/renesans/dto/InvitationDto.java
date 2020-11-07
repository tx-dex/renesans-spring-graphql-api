package fi.sangre.renesans.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InvitationDto {
    private List<RecipientDto> recipients = new ArrayList<>();
    private String subject;
    private String body;
    private String senderName;
    private EmailNamePair replyTo;
}
