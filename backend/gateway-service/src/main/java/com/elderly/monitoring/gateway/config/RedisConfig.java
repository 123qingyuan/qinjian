package main.java.com.elderly.monitoring.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Redis配置类
 * 
 * @author System
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * 限流Lua脚本
     */
    @Bean
    public RedisScript<List<Long>> rateLimitScript() {
        String script = """
                local key = KEYS[1]
                local replenishRate = tonumber(ARGV[1])
                local burstCapacity = tonumber(ARGV[2])
                local currentTime = tonumber(ARGV[3])
                
                -- 获取当前桶状态
                local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
                local tokens = tonumber(bucket[1]) or burstCapacity
                local lastRefill = tonumber(bucket[2]) or currentTime
                
                -- 计算需要补充的令牌数
                local timePassed = currentTime - lastRefill
                local tokensToAdd = math.floor(timePassed * replenishRate / 1000)
                tokens = math.min(burstCapacity, tokens + tokensToAdd)
                
                -- 检查是否有足够的令牌
                if tokens >= 1 then
                    -- 消耗一个令牌
                    tokens = tokens - 1
                    -- 更新桶状态
                    redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', currentTime)
                    -- 设置过期时间
                    redis.call('EXPIRE', key, math.ceil(burstCapacity / replenishRate) + 1)
                    return {1, tokens}
                else
                    -- 更新桶状态
                    redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', currentTime)
                    -- 设置过期时间
                    redis.call('EXPIRE', key, math.ceil(burstCapacity / replenishRate) + 1)
                    return {0, tokens}
                end
                """;
        
        return RedisScript.of(script, List.class);
    }

    /**
     * 黑名单检查脚本
     */
    @Bean
    public RedisScript<Boolean> blacklistCheckScript() {
        String script = """
                local key = KEYS[1]
                local exists = redis.call('EXISTS', key)
                return exists == 1
                """;
        
        return RedisScript.of(script, Boolean.class);
    }

    /**
     * 添加到黑名单脚本
     */
    @Bean
    public RedisScript<Long> addToBlacklistScript() {
        String script = """
                local key = KEYS[1]
                local ttl = tonumber(ARGV[1])
                local reason = ARGV[2]
                
                redis.call('HSET', key, 'reason', reason, 'timestamp', ARGV[3])
                if ttl > 0 then
                    redis.call('EXPIRE', key, ttl)
                end
                return 1
                """;
        
        return RedisScript.of(script, Long.class);
    }

    /**
     * 从黑名单移除脚本
     */
    @Bean
    public RedisScript<Long> removeFromBlacklistScript() {
        String script = """
                local key = KEYS[1]
                local removed = redis.call('DEL', key)
                return removed
                """;
        
        return RedisScript.of(script, Long.class);
    }

    /**
     * 统计计数脚本
     */
    @Bean
    public RedisScript<Long> incrementCounterScript() {
        String script = """
                local key = KEYS[1]
                local ttl = tonumber(ARGV[1])
                local increment = tonumber(ARGV[2])
                
                local count = redis.call('INCRBY', key, increment)
                if ttl > 0 and count == increment then
                    -- 第一次设置，添加过期时间
                    redis.call('EXPIRE', key, ttl)
                end
                return count
                """;
        
        return RedisScript.of(script, Long.class);
    }

    /**
     * 滑动窗口计数脚本
     */
    @Bean
    public RedisScript<Long> slidingWindowCounterScript() {
        String script = """
                local key = KEYS[1]
                local windowSize = tonumber(ARGV[1])
                local limit = tonumber(ARGV[2])
                local currentTime = tonumber(ARGV[3])
                
                -- 移除过期的记录
                redis.call('ZREMRANGEBYSCORE', key, 0, currentTime - windowSize * 1000)
                
                -- 获取当前窗口内的请求数
                local count = redis.call('ZCARD', key)
                
                if count < limit then
                    -- 添加当前请求
                    redis.call('ZADD', key, currentTime, currentTime)
                    -- 设置过期时间
                    redis.call('EXPIRE', key, windowSize + 1)
                    return count + 1
                else
                    return count
                end
                """;
        
        return RedisScript.of(script, Long.class);
    }

    /**
     * 分布式锁脚本
     */
    @Bean
    public RedisScript<Boolean> distributedLockScript() {
        String script = """
                local lockKey = KEYS[1]
                local lockValue = ARGV[1]
                local ttl = tonumber(ARGV[2])
                
                local result = redis.call('SET', lockKey, lockValue, 'NX', 'EX', ttl)
                return result == 'OK'
                """;
        
        return RedisScript.of(script, Boolean.class);
    }

    /**
     * 释放分布式锁脚本
     */
    @Bean
    public RedisScript<Boolean> releaseLockScript() {
        String script = """
                local lockKey = KEYS[1]
                local expectedValue = ARGV[1]
                
                local currentValue = redis.call('GET', lockKey)
                if currentValue == expectedValue then
                    return redis.call('DEL', lockKey)
                else
                    return 0
                end
                """;
        
        return RedisScript.of(script, Boolean.class);
    }

    /**
     * 批量限流检查脚本
     */
    @Bean
    public RedisScript<List<Long>> batchRateLimitScript() {
        String script = """
                local results = {}
                
                for i = 1, #KEYS do
                    local key = KEYS[i]
                    local replenishRate = tonumber(ARGV[i * 2 - 1])
                    local burstCapacity = tonumber(ARGV[i * 2])
                    local currentTime = tonumber(ARGV[(#KEYS * 2) + 1])
                    
                    local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
                    local tokens = tonumber(bucket[1]) or burstCapacity
                    local lastRefill = tonumber(bucket[2]) or currentTime
                    
                    local timePassed = currentTime - lastRefill
                    local tokensToAdd = math.floor(timePassed * replenishRate / 1000)
                    tokens = math.min(burstCapacity, tokens + tokensToAdd)
                    
                    if tokens >= 1 then
                        tokens = tokens - 1
                        redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', currentTime)
                        redis.call('EXPIRE', key, math.ceil(burstCapacity / replenishRate) + 1)
                        table.insert(results, 1)
                    else
                        redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', currentTime)
                        redis.call('EXPIRE', key, math.ceil(burstCapacity / replenishRate) + 1)
                        table.insert(results, 0)
                    end
                end
                
                return results
                """;
        
        return RedisScript.of(script, List.class);
    }

    /**
     * 限流键解析器
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            // 优先使用用户ID
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return org.springframework.web.server.ServerWebExchange.just(userId);
            }
            
            // 其次使用IP地址
            String ip = exchange.getRequest().getRemoteAddress() != null ? 
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return org.springframework.web.server.ServerWebExchange.just(ip);
        };
    }

    /**
     * IP键解析器
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> {
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return org.springframework.web.server.ServerWebExchange.just(xForwardedFor.split(",")[0].trim());
            }
            
            String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return org.springframework.web.server.ServerWebExchange.just(xRealIp);
            }
            
            String ip = exchange.getRequest().getRemoteAddress() != null ? 
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return org.springframework.web.server.ServerWebExchange.just(ip);
        };
    }

    /**
     * 路径键解析器
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver pathKeyResolver() {
        return exchange -> org.springframework.web.server.ServerWebExchange.just(
                exchange.getRequest().getPath().value());
    }

    /**
     * 主机键解析器
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver hostKeyResolver() {
        return exchange -> org.springframework.web.server.ServerWebExchange.just(
                exchange.getRequest().getURI().getHost());
    }
}