package fi.sangre.renesans.dto;


import lombok.Data;

import java.util.Date;

@Data
@Deprecated
public class InvitationDetailsDto {

    private long id;
    private String respondentGroupId;
    private String email;
    private String status;
    private Date createdTime;
    private Date sentTime;
    private String subject;
    private String body;
    private String hash;
}
