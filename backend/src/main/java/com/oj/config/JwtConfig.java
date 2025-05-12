package com.oj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration
public class JwtConfig {

    /**
     * 手动配置JWT属性，解决无法读取application.properties中的配置问题
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        // JWT配置 - 24小时过期
        properties.setProperty("jwt.expiration", "86400000");
        properties.setProperty("jwt.header", "Authorization");
        properties.setProperty("jwt.token-prefix", "Bearer ");
        configurer.setProperties(properties);
        return configurer;
    }
}