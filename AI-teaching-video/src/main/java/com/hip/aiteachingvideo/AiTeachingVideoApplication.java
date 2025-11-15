package com.hip.aiteachingvideo;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@MapperScan("com.hip.aiteachingvideo.mapper")
@EnableAsync
@EnableTransactionManagement
@SpringBootApplication
public class AiTeachingVideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiTeachingVideoApplication.class, args);
    }
}