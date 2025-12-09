package com.smartlearn.common.config;

import com.smartlearn.common.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonAutoConfiguration {
}
