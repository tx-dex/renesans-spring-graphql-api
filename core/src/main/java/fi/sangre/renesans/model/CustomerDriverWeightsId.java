package fi.sangre.renesans.model;

import lombok.Data;

import java.io.Serializable;

@Data
class CustomerDriverWeightsId implements Serializable {
    private Long customerId;
    private Long driverId;
}
