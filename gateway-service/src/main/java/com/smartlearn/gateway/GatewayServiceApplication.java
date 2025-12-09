package com.smartlearn.gateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.smartlearn")
public class GatewayServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(GatewayServiceApplication.class,args);
    }
}
