package com.lxy.gmt_mono.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 设置脚本的源文件
        redisScript.setScriptSource(
                new ResourceScriptSource(
                        new ClassPathResource("scripts/seckill.lua")));
        // 设置脚本的返回值类型，因为lua脚本返回的是一个Long类型的值
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
