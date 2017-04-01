package aqar.model;

import javax.persistence.*;

@Entity
public class Advertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    private Boolean success;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    public Advertise(String number, Job job) {
        this.number = number;
        this.job = job;
    }
}
