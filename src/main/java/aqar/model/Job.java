package aqar.model;

import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Stream;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String name;

    private String clientId;

    @OneToOne
    @JoinColumn(name = "job_detail_id", unique = true, nullable = false, updatable = false)
    JobDetail jobDetail;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
    private List<Advertise> advertise;

    private boolean active;
}
