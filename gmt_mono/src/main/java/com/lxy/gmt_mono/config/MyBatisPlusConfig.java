package com.lxy.gmt_mono.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@MapperScan("com.lxy.gmt_mono.mapper")
public class MyBatisPlusConfig {

    /**
     * 分页插件
     * 当MybatisPlus检测到正在执行一个分页查询时，会自动拼接上对应数据库的物理分页
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 创建MybatisPlusInterceptor,是一个拦截器，继承了Interceptor接口
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
