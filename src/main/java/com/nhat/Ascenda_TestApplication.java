package com.nhat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/*@ConditionalOnProperty(
        value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true
)*/
@SpringBootApplication
@EnableScheduling
//@Profile("default")
public class Ascenda_TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(Ascenda_TestApplication.class, args);
    }
}
