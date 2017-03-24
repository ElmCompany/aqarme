package aqar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class Scheduler {

    private final static int RATE = 3;

    private AqarService aqarService;
    private MessengerService messengerService;

    public Scheduler(AqarService aqarService, MessengerService messengerService) {
        this.aqarService = aqarService;
        this.messengerService = messengerService;
    }

//    @Scheduled(cron = "0 0 10 * * *", zone = "GMT")       // run every day at 10 AM
    @Scheduled(fixedRate = 1000 * 60 * 60 * RATE)
    public void run() {
        Stream<String> run = aqarService.run();

        run.forEach(url -> {
            System.out.println(url);
            messengerService.send("966593642012", url);
            messengerService.send("00201095771359", url);
        });
    }

}
