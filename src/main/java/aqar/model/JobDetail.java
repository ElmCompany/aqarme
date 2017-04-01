package aqar.model;

import lombok.AccessLevel;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Setter(AccessLevel.PACKAGE)
class JobDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    Integer minPrice;
    Integer maxPrice;
    Boolean hasImages;
    Boolean hasElevator;
    // comma-separated numbers
    String numRooms;
    // comma-separated numbers
    String floorNumber;
    @NotNull
    @Column(nullable = false)
    String vertexes;

    @OneToOne(optional = false, mappedBy = "jobDetail")
    private Job job;
}
