package aqar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Component
public class Scheduler {

    private final static int HOUR = 1000 * 60 * 60;
    private final static int RATE = 6;

    @Value("${aqar.messenger.senderList}")
    private String[] senderList;

    private AqarService aqarService;
    private MessengerService messengerService;

    public Scheduler(AqarService aqarService, MessengerService messengerService) {
        this.aqarService = aqarService;
        this.messengerService = messengerService;
    }

    @Scheduled(fixedRate = HOUR * RATE)
    public void run() {

        log.info(" *********JOB STARTED *********");

        Stream<String> run = aqarService.run();
        long count = run.peek(url -> {
            if (senderList != null && senderList.length > 0) {
                log.info("found match url to the criteria {} ...sending via fb Messenger to {}",
                        url, Arrays.toString(senderList));
                Stream.of(senderList).forEach(it -> messengerService.send(it, url));
            } else {
                log.info("found match url to the criteria {}", url);
            }
        }).count();

        log.info(" *********JOB END ********* \n Num of items match = " + count);
    }

}
