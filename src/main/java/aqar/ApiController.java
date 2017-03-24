package aqar;

import aqar.db.ProcessedAdsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/")
public class ApiController {

    private ProcessedAdsRepository adsRepository;

    @GetMapping
    public ResponseEntity<?> listProcessedAds() {
        return ok(adsRepository.findAll());
    }
}
