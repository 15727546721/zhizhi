package cn.xu.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {
    "cn.xu.infrastructure.persistent.dao",
    "cn.xu.infrastructure.mapper"
})
public class MyBatisConfig {
} 