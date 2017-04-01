package aqar.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String clientId;
    @OneToOne
    @JoinColumn(name = "job_detail_id", unique = true, nullable = false, updatable = false)
    JobDetail jobDetail;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
    private List<Advertise> advertise;

    private boolean active;
}
