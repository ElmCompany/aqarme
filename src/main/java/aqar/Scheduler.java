package aqar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
public class Scheduler {

    private final static int HOUR = 1000 * 60 * 60;
    private final static int RATE = 6;

    private AqarService aqarService;

    public Scheduler(AqarService aqarService) {
        this.aqarService = aqarService;
    }

    @Scheduled(fixedRate = HOUR * RATE)
    public void run() {

        log.info(" *********JOB STARTED *********");

        Map<String, List<String>> clientToAdList = aqarService.run();

        log.info("output of job run is: {}",  clientToAdList);
        log.info(" *********JOB END *********");
    }

}
