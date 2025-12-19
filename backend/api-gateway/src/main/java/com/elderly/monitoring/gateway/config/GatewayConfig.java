package com.elderly.monitoring.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户管理服务路由
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri("http://localhost:8081"))
                
                // 设备管理服务路由
                .route("device-service", r -> r.path("/api/v1/devices/**")
                        .uri("http://localhost:8082"))
                
                // 实时监控服务路由
                .route("monitoring-service", r -> r.path("/api/v1/monitoring/**")
                        .uri("http://localhost:8083"))
                
                // 预警系统服务路由
                .route("alert-service", r -> r.path("/api/v1/alerts/**")
                        .uri("http://localhost:8084"))
                
                // 历史数据服务路由
                .route("history-service", r -> r.path("/api/v1/history/**")
                        .uri("http://localhost:8085"))
                
                // 消息通知服务路由
                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .uri("http://localhost:8086"))
                
                .build();
    }
}