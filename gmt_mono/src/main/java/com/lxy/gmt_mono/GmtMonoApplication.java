package com.lxy.gmt_mono;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.lxy.gmt_mono.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class GmtMonoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmtMonoApplication.class, args);
    }

}
