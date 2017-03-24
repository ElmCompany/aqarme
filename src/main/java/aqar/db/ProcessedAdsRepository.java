package aqar.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProcessedAdsRepository extends JpaRepository<ProcessedAds, Long> {

    @Query("select count(o) > 0 from ProcessedAds o where o.adNumber = :adNumber")
    boolean addNumberExists(@Param("adNumber") String adNumber);

}
