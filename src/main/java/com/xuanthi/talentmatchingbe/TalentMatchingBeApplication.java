package com.xuanthi.talentmatchingbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableRetry
@EnableScheduling
public class TalentMatchingBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentMatchingBeApplication.class, args);
    }

}
