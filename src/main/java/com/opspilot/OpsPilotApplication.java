package com.opspilot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@MapperScan("com.opspilot.mapper")
public class OpsPilotApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpsPilotApplication.class, args);
    }
}
