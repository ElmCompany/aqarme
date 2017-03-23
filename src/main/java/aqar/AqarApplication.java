package aqar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
public class AqarApplication implements CommandLineRunner {

    @Autowired
    private AqarService aqarService;

    public static void main(String[] args) {
        SpringApplication.run(AqarApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Stream<String> run = aqarService.run();

        run.forEach(System.out::println);
    }
}
