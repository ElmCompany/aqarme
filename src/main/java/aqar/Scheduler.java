package aqar;

import aqar.model.JobOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Slf4j
@Component
public class Scheduler {

    private final static int HOUR = 1000 * 60 * 60;
    private final static int RATE = 1;

    private AqarService aqarService;
    private MessengerService messengerService;

    public Scheduler(AqarService aqarService, MessengerService messengerService) {
        this.aqarService = aqarService;
        this.messengerService = messengerService;
    }

    @Scheduled(fixedRate = HOUR * RATE)
    public void run() {

        log.info(" *********JOB STARTED *********");

        Stream<JobOutput> output = aqarService.run();

        long count = output.peek(it -> it.getSenders().forEach(it2 -> {
            log.info("found match url to the criteria {} ...sending via fb Messenger to ", it, it2);
            messengerService.send(it2, it.getTitle() + " " + it.getUrl());
        })).count();

        log.info(" *********JOB END ********* \n Num of items match = " + count);
    }

}
