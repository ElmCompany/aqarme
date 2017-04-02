package aqar.model;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

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
    @NotEmpty
    @Column(nullable = false)
    String vertexes;
    // comma-separated Strings

    @OneToOne(optional = false, mappedBy = "jobDetail")
    private Job job;
}
