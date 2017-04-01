package aqar.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String clientId;

    @OneToOne
    @JoinColumn(name = "job_detail_id", unique = true, nullable = false, updatable = false)
    private JobDetail jobDetail;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
    private List<Advertise> advertise;

    private boolean active;
}
