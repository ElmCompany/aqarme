package aqar.model.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import aqar.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Modifying
    @Transactional
    void deleteByClientId(String clientId);
}
