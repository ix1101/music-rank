package com.musicrank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.musicrank.mapper")
@EnableScheduling
public class MusicRankBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicRankBackendApplication.class, args);
    }

}
