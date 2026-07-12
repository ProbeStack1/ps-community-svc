package io.probestack.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CommunitySvcApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunitySvcApplication.class, args);
    }
}
