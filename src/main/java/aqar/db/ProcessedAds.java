package aqar.db;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class ProcessedAds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adNumber;

    private Boolean success;

    public ProcessedAds() {
    }

    public ProcessedAds(String adNumber) {
        this.adNumber = adNumber;
    }
}
