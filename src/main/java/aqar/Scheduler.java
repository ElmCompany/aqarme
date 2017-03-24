package aqar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class Scheduler {

    private final static int RATE = 3;
    @Value("${aqar.messenger.senderList}")
    private String[] senderList;

    private AqarService aqarService;
    private MessengerService messengerService;

    public Scheduler(AqarService aqarService, MessengerService messengerService) {
        this.aqarService = aqarService;
        this.messengerService = messengerService;
    }

    //    @Scheduled(cron = "0 0 10 * * *", zone = "GMT")       // run every day at 10 AM
    @Scheduled(fixedRate = 1000 * 60 * 60 * RATE)
    public void run() {

        System.out.println(" *********JOB STARTED *********");

        Stream<String> run = aqarService.run();
        long count = run.peek(url -> {
            if (senderList != null && senderList.length > 0) {
                System.out.printf("found match url to the criteria %s ...sending via fb Messenger\n", url);
                Stream.of(senderList).forEach(it -> messengerService.send(it, url));
            } else {
                System.out.printf("found match url to the criteria %s\n", url);
            }
        }).count();

        System.out.println(" *********JOB END ********* \n Num of items match = " + count);
    }

}
