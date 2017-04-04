package aqar.model.repo;

import aqar.model.Advertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface AdvertiseRepository extends JpaRepository<Advertise, Long> {
	
	@Query("select count(o) from Advertise o join o.job j where o.number = :number")
	long countOfJobsProcessedAdvertise(@Param("number") String number);

    @Query("select count(o) > 0 from Advertise o where o.number = :number and o.job.id = :jobId")
    boolean alreadyProcessed(@Param("number") String number, @Param("jobId") Long jobId);

    @Async
    @Transactional
    @Modifying
    @Query("update Advertise o set o.success = true where o.number = :number and o.job.id = :jobId ")
    void markAsSuccess(@Param("number") String number, @Param("jobId") Long jobId);


    long countBySuccessIsTrue();

    List<Advertise> findAllBySuccessIsTrueAndJobClientIdEqualsOrderByIdDesc(String clientId);

    Long countBySuccessIsTrueAndJobClientIdEqualsOrderByIdDesc(String clientId);
}
