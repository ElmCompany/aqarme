package aqar.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ProcessedAds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adNumber;

    public ProcessedAds() {
    }

    public ProcessedAds(String adNumber) {
        this.adNumber = adNumber;
    }

    public String getAdNumber() {
        return adNumber;
    }
}
