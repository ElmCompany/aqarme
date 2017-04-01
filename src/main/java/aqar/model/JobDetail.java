package aqar.model;

import javax.persistence.*;

@Entity
public class JobDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, mappedBy = "jobDetail")
    private Job job;
}
