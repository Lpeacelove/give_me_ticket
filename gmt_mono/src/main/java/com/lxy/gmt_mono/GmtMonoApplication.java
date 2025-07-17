package com.lxy.gmt_mono;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lxy.gmt_mono.mapper")
public class GmtMonoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmtMonoApplication.class, args);
    }

}
