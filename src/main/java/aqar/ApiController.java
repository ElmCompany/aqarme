package aqar;

import aqar.model.AdvertiseRepository;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api")
public class ApiController {

    private AdvertiseRepository adsRepository;

    public ApiController(AdvertiseRepository adsRepository) {
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
