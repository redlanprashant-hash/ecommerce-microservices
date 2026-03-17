package com.ecommerce.apigateway.config;

import com.ecommerce.apigateway.interceptor.GatewayLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final GatewayLoggingInterceptor interceptor;

    public WebMvcConfig(GatewayLoggingInterceptor interceptor) {
        this.interceptor = interceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
