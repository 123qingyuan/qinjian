package main.java.com.elderly.monitoring.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${gateway.security.public-paths}")
    private List<String> publicPaths;

    @Value("${gateway.security.admin-paths}")
    private List<String> adminPaths;

    @Value("${gateway.security.super-admin-paths}")
    private List<String> superAdminPaths;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_NAME_HEADER = "X-User-Name";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing request: {} {}", request.getMethod(), path);

        // 检查是否为公开路径
        if (isPublicPath(path)) {
            log.debug("Public path access allowed: {}", path);
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid authorization header for path: {}", path);
            return handleUnauthorized(exchange, "缺少有效的认证令牌");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            // 验证JWT令牌
            Claims claims = validateToken(token);
            
            // 检查权限
            if (!hasRequiredRole(path, claims)) {
                log.warn("Insufficient permissions for user {} on path: {}", 
                        claims.getSubject(), path);
                return handleForbidden(exchange, "权限不足");
            }

            // 添加用户信息到请求头
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header(USER_ID_HEADER, claims.getSubject())
                    .header(USER_ROLE_HEADER, claims.get("role", String.class))
                    .header(USER_NAME_HEADER, claims.get("username", String.class))
                    .build();

            log.debug("Authentication successful for user: {}", claims.getSubject());
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Token validation failed for path: {}", path, e);
            return handleUnauthorized(exchange, "认证令牌无效或已过期");
        }
    }

    /**
     * 验证JWT令牌
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查是否为公开路径
     */
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(pattern -> 
                pathMatches(pattern, path));
    }

    /**
     * 检查用户是否具有所需权限
     */
    private boolean hasRequiredRole(String path, Claims claims) {
        String userRole = claims.get("role", String.class);
        
        // 检查超级管理员权限
        if (isSuperAdminPath(path) && !"SUPER_ADMIN".equals(userRole)) {
            return false;
        }
        
        // 检查管理员权限
        if (isAdminPath(path) && !("ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole))) {
            return false;
        }
        
        return true;
    }

    /**
     * 检查是否为超级管理员路径
     */
    private boolean isSuperAdminPath(String path) {
        return superAdminPaths.stream().anyMatch(pattern -> 
                pathMatches(pattern, path));
    }

    /**
     * 检查是否为管理员路径
     */
    private boolean isAdminPath(String path) {
        return adminPaths.stream().anyMatch(pattern -> 
                pathMatches(pattern, path));
    }

    /**
     * 路径匹配
     */
    private boolean pathMatches(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("""
                {
                    "success": false,
                    "code": 401,
                    "message": "%s",
                    "timestamp": "%s"
                }
                """, message, java.time.Instant.now().toString());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 处理禁止访问请求
     */
    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("""
                {
                    "success": false,
                    "code": 403,
                    "message": "%s",
                    "timestamp": "%s"
                }
                """, message, java.time.Instant.now().toString());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 确保在其他过滤器之前执行
    }
}