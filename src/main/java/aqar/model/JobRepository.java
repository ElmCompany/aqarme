package aqar.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findAllByActiveIsTrue();

    Long countByActiveIsTrue();

    @Modifying
    @Transactional
    void deleteByClientId(String clientId);
}
