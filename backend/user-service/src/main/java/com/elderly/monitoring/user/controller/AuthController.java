package main.java.com.elderly.monitoring.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import main.java.com.elderly.monitoring.user.entity.User;
import main.java.com.elderly.monitoring.user.entity.UserRole;
import main.java.com.elderly.monitoring.user.entity.UserStatus;
import main.java.com.elderly.monitoring.user.service.UserService;
import main.java.com.elderly.monitoring.user.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证控制器
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>();

        try {
            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOpt.get();

            // 生成JWT token
            String token = jwtUtil.generateToken(username, user.getRole().name());

            // 构建响应数据
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("realName", user.getRealName());
            userData.put("role", user.getRole());
            userData.put("status", user.getStatus());
            userData.put("avatarUrl", user.getAvatarUrl());

            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("user", userData);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> registerRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = (String) registerRequest.get("username");
            String password = (String) registerRequest.get("password");
            String email = (String) registerRequest.get("email");
            String realName = (String) registerRequest.get("realName");
            String phone = (String) registerRequest.get("phone");
            String idCard = (String) registerRequest.get("idCard");
            String roleStr = (String) registerRequest.get("role");

            // 验证必填字段
            if (username == null || password == null || email == null) {
                response.put("success", false);
                response.put("message", "用户名、密码和邮箱为必填项");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(username).isPresent()) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查邮箱是否已存在
            if (userService.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "邮箱已存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 解析角色
            UserRole role = UserRole.ELDERLY; // 默认角色
            if (roleStr != null) {
                try {
                    role = UserRole.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "无效的用户角色");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRealName(realName);
            user.setPhone(phone);
            user.setIdCard(idCard);
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE);

            User createdUser = userService.createUser(user);

            // 移除敏感信息
            createdUser.setPassword(null);

            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", createdUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 刷新token
     * 
     * @param request 请求头中包含token
     * @return 新的token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "无效的认证信息");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);

        try {
            // 验证token
            String username = jwtUtil.getUsernameFromToken(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOpt.get();

            // 验证token有效性
            if (jwtUtil.validateToken(token, username)) {
                // 生成新token
                String newToken = jwtUtil.generateToken(username, user.getRole().name());

                response.put("success", true);
                response.put("message", "Token刷新成功");
                response.put("token", newToken);
                response.put("type", "Bearer");

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Token已过期或无效");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token刷新失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 验证token有效性
     * 
     * @param request 请求头中包含token
     * @return 验证结果
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "无效的认证信息");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);

        try {
            boolean isValid = jwtUtil.isTokenValid(token);
            
            if (isValid) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                response.put("success", true);
                response.put("valid", true);
                response.put("username", username);
                response.put("role", role);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", true);
                response.put("valid", false);
                
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token验证失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 登出（客户端清除token即可，服务端无需特殊处理）
     * 
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }
}