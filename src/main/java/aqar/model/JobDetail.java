package aqar.model;

import javax.persistence.*;

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
    @Column(nullable = false)
    String vertexes;
}
