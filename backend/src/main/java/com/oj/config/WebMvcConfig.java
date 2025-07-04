package com.oj.config;

import com.oj.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/v2/api-docs-ext/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        // 改进上传文件的映射配置，使用绝对路径
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    // Knife4j相关
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/v2/api-docs",
                    "/v2/api-docs-ext/**",
                    "/configuration/ui",
                    "/configuration/security",
                    "/swagger-ui.html",
                    "/favicon.ico",
                    // 用户相关
                    "/api/user/login",
                    "/api/user/register",
                    // 题目相关（仅允许查看公开列表）
                    "/api/problem/list",
                    // 静态资源
                    "/api/uploads/**"
                );
    }
} 