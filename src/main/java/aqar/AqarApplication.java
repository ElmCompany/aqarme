package aqar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;

@SpringBootApplication
public class AqarApplication implements CommandLineRunner {

    @Autowired
    private AqarService aqarService;

    @Autowired
    private MessengerService messengerService;

    public static void main(String[] args) {
        SpringApplication.run(AqarApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Stream<String> run = aqarService.run();

        run.forEach(url -> {
            messengerService.send("966593642012", url);
            messengerService.send("00201095771359", url);
        });
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
