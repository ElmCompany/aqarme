package aqar.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;


public interface ProcessedAdsRepository extends JpaRepository<ProcessedAds, Long> {

    @Query("select count(o) > 0 from ProcessedAds o where o.adNumber = :adNumber")
    boolean addNumberExists(@Param("adNumber") String adNumber);

    @Async
    @Transactional
    @Modifying
    @Query("update ProcessedAds set success = true where adNumber = :adNumber")
    void markAsSuccess(@Param("adNumber") String kbd);


    long countBySuccessIsTrue();
}
