package main.java.com.elderly.monitoring.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 限流过滤器
 * 
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisScript<List<Long>> rateLimitScript;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String clientIp = getClientIp(request);
        String userId = getUserId(request);

        log.debug("Rate limit check for path: {}, IP: {}, User: {}", path, clientIp, userId);

        // 检查IP黑名单
        if (isIpBlacklisted(clientIp)) {
            log.warn("Blacklisted IP access attempt: {}", clientIp);
            return handleRateLimitExceeded(exchange, "IP地址已被限制访问");
        }

        // 检查用户黑名单
        if (userId != null && isUserBlacklisted(userId)) {
            log.warn("Blacklisted user access attempt: {}", userId);
            return handleRateLimitExceeded(exchange, "用户账户已被限制访问");
        }

        // 获取限流配置
        RateLimitConfig config = getRateLimitConfig(path);
        
        // 执行限流检查
        return checkRateLimit(clientIp, userId, config)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    } else {
                        log.warn("Rate limit exceeded for IP: {}, User: {}, Path: {}", 
                                clientIp, userId, path);
                        return handleRateLimitExceeded(exchange, "请求频率过高，请稍后再试");
                    }
                });
    }

    /**
     * 检查IP是否在黑名单中
     */
    private Mono<Boolean> isIpBlacklisted(String clientIp) {
        String key = BLACKLIST_PREFIX + "ip:" + clientIp;
        return redisTemplate.hasKey(key).defaultIfEmpty(false);
    }

    /**
     * 检查用户是否在黑名单中
     */
    private Mono<Boolean> isUserBlacklisted(String userId) {
        String key = BLACKLIST_PREFIX + "user:" + userId;
        return redisTemplate.hasKey(key).defaultIfEmpty(false);
    }

    /**
     * 执行限流检查
     */
    private Mono<Boolean> checkRateLimit(String clientIp, String userId, RateLimitConfig config) {
        String key = buildRateLimitKey(clientIp, userId, config);
        
        // 使用Redis Lua脚本执行原子限流操作
        List<String> keys = Arrays.asList(key);
        List<String> args = Arrays.asList(
                String.valueOf(config.getReplenishRate()),
                String.valueOf(config.getBurstCapacity()),
                String.valueOf(System.currentTimeMillis())
        );

        return redisTemplate.execute(rateLimitScript, keys, args)
                .next()
                .map(result -> {
                    List<Long> results = (List<Long>) result;
                    return results.get(0) == 1; // 1表示允许，0表示拒绝
                })
                .defaultIfEmpty(true);
    }

    /**
     * 构建限流键
     */
    private String buildRateLimitKey(String clientIp, String userId, RateLimitConfig config) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_PREFIX);
        
        switch (config.getKeyType()) {
            case IP:
                keyBuilder.append("ip:").append(clientIp);
                break;
            case USER:
                keyBuilder.append("user:").append(userId != null ? userId : "anonymous");
                break;
            case PATH:
                keyBuilder.append("path:").append(config.getPath());
                break;
            case COMBINED:
                keyBuilder.append("combined:")
                        .append(clientIp).append(":")
                        .append(userId != null ? userId : "anonymous").append(":")
                        .append(config.getPath());
                break;
        }
        
        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 获取用户ID
     */
    private String getUserId(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-User-Id");
    }

    /**
     * 获取限流配置
     */
    private RateLimitConfig getRateLimitConfig(String path) {
        // 根据路径返回不同的限流配置
        if (path.startsWith("/api/auth/")) {
            return RateLimitConfig.builder()
                    .path("/api/auth/**")
                    .replenishRate(5)
                    .burstCapacity(10)
                    .keyType(KeyType.USER)
                    .build();
        } else if (path.startsWith("/api/monitoring/realtime")) {
            return RateLimitConfig.builder()
                    .path("/api/monitoring/realtime")
                    .replenishRate(30)
                    .burstCapacity(60)
                    .keyType(KeyType.USER)
                    .build();
        } else if (path.startsWith("/api/history/export/")) {
            return RateLimitConfig.builder()
                    .path("/api/history/export/**")
                    .replenishRate(2)
                    .burstCapacity(5)
                    .keyType(KeyType.USER)
                    .build();
        } else {
            return RateLimitConfig.builder()
                    .path(path)
                    .replenishRate(10)
                    .burstCapacity(20)
                    .keyType(KeyType.COMBINED)
                    .build();
        }
    }

    /**
     * 处理限流超出
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", "60");

        String body = String.format("""
                {
                    "success": false,
                    "code": 429,
                    "message": "%s",
                    "timestamp": "%s",
                    "retryAfter": 60
                }
                """, message, java.time.Instant.now().toString());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -50; // 在认证过滤器之后执行
    }

    /**
     * 限流配置
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RateLimitConfig {
        private String path;
        private int replenishRate;
        private int burstCapacity;
        private KeyType keyType;
        private Duration timeout = Duration.ofSeconds(5);
    }

    /**
     * 限流键类型
     */
    public enum KeyType {
        IP,        // 基于IP限流
        USER,      // 基于用户限流
        PATH,      // 基于路径限流
        COMBINED   // 基于IP+用户+路径组合限流
    }
}