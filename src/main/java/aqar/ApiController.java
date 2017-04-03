package aqar;

import aqar.model.Advertise;
import aqar.model.Job;
import aqar.model.repo.AdvertiseRepository;
import aqar.model.repo.JobRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static java.util.stream.Collectors.toList;
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

    @PostMapping("/jobs")
    public ResponseEntity<?> addJob(@Valid @RequestBody Job job) {
        log.info("delete all jobs for client id: {} ", job.clientId());
        jobRepository.deleteByClientId(job.clientId());
        log.info("add new job for client id: {}", job.clientId());
        jobRepository.save(job);
        return ok().build();
    }

    @GetMapping("/list/{clientId}")
    public ResponseEntity<?> listAdsByClientId(@PathVariable("clientId") String clientId) {
        return ok(adsRepository.findAllBySuccessIsTrueAndJobClientIdEqualsOrderByIdDesc(clientId)
                .stream()
                .map(Advertise::number)
                .collect(toList()));
    }

    @GetMapping("/count/{clientId}")
    public ResponseEntity<?> countAdsByClientId(@PathVariable("clientId") String clientId) {
        return ok(adsRepository.countBySuccessIsTrueAndJobClientIdEqualsOrderByIdDesc(clientId));
    }

    @Getter
    private static class Stats {
        long allCount, successCount;
    }
}
