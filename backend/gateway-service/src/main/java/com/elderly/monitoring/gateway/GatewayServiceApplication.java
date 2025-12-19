package main.java.com.elderly.monitoring.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableEurekaClient;

/**
 * API网关服务启动类
 * 
 * @author System
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.elderly.monitoring.gateway"})
@EnableEurekaClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}