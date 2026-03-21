package com.xuanthi.talentmatchingbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TalentMatchingBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentMatchingBeApplication.class, args);
    }

}
