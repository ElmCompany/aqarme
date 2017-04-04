package aqar.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.AccessLevel;
import lombok.Setter;

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
    // comma-separated Strings
    @NotEmpty
    @Column(nullable = false, length=8000)
    String vertexes;

    @OneToOne(optional = false, mappedBy = "jobDetail")
    private Job job;
}
