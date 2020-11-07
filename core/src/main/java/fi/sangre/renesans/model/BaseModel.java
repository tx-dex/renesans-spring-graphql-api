package fi.sangre.renesans.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseModel {
    private Boolean archived = false;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name="mtm", nullable=false)
    private Date modified;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(name="ctm", nullable=false, updatable=false)
    private Date created;
}
