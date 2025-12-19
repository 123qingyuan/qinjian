package main.java.com.elderly.monitoring.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 设备管理服务主应用类
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}