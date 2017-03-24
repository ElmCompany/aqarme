package aqar;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class Scheduler {

    private AqarService aqarService;
    private MessengerService messengerService;

    public Scheduler(AqarService aqarService, MessengerService messengerService) {
        this.aqarService = aqarService;
        this.messengerService = messengerService;
    }

    @Scheduled(cron = "0 0 10 * * *", zone = "GMT")
    public void run() {
        Stream<String> run = aqarService.run();

        run.forEach(url -> {
            System.out.println(url);
            messengerService.send("966593642012", url);
            messengerService.send("00201095771359", url);
        });
    }

}
