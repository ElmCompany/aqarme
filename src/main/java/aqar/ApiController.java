package aqar;

import aqar.model.ProcessedAdsRepository;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api")
public class ApiController {

    private ProcessedAdsRepository adsRepository;

    public ApiController(ProcessedAdsRepository adsRepository) {
        this.adsRepository = adsRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> listProcessedAds() {
        Stats stats = new Stats();
        stats.allCount = adsRepository.count();
        stats.successCount = adsRepository.countBySuccessIsTrue();
        return ok(stats);
    }

    @Getter
    static class Stats{
        long allCount, successCount;
    }
}
