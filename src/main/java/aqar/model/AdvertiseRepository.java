package aqar.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;


public interface AdvertiseRepository extends JpaRepository<Advertise, Long> {

    @Query("select count(o) > 0 from Advertise o where o.number = :number")
    boolean alreadyProcessed(@Param("number") String number);

    @Async
    @Transactional
    @Modifying
    @Query("update Advertise set success = true where number = :number")
    void markAsSuccess(@Param("number") String number);


    long countBySuccessIsTrue();
}
