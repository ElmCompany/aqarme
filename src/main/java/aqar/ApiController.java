package aqar;

import aqar.model.AdvertiseRepository;
import aqar.model.Job;
import aqar.model.JobRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private AdvertiseRepository adsRepository;
    private JobRepository jobRepository;

    public ApiController(AdvertiseRepository adsRepository, JobRepository jobRepository) {
        this.adsRepository = adsRepository;
        this.jobRepository = jobRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Stats stats = new Stats();
        stats.allCount = adsRepository.count();
        stats.successCount = adsRepository.countBySuccessIsTrue();
        return ok(stats);
    }

    @PostMapping("/job")
    public ResponseEntity<?> addJob(@Valid @RequestBody Job job) {
        log.info("delete all jobs for client id: {} ", job.getClientId());
        jobRepository.deleteByClientId(job.getClientId());
        log.info("add new job for client id: {}", job.getClientId());
        jobRepository.save(job);
        return ok().build();
    }

    @Getter
    static class Stats {
        long allCount, successCount;
    }
}
