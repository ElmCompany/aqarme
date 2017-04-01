package aqar.model;

import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Setter
@Entity
public class JobDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, mappedBy = "jobDetail")
    private Job job;

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
}
