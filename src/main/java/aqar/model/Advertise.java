package aqar.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
public class Advertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    @SuppressWarnings("unused")
	private Boolean success;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    public Advertise(String number, Job job) {
        this.number = number;
        this.job = job;
    }

    public String number() {
        return number;
    }
}
