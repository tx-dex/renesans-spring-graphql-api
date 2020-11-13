package fi.sangre.renesans.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"customerId", "driverId"})

@Entity
@IdClass(CustomerDriverWeightsId.class)
@Table(name = "customer_driver_weights")
@DynamicInsert
public class CustomerDriverWeights implements Serializable {
    @Id
    private UUID customerId;
    @Id
    private Long driverId;
    private Double weight;
}
