package main.java.com.elderly.monitoring.user.controller;

import main.java.com.elderly.monitoring.user.entity.User;
import main.java.com.elderly.monitoring.user.entity.UserRole;
import main.java.com.elderly.monitoring.user.entity.UserStatus;
import main.java.com.elderly.monitoring.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User createdUser = userService.createUser(user);
            response.put("success", true);
            response.put("message", "用户创建成功");
            response.put("data", createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户详情
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            // 移除密码字段
            User userDto = user.get();
            userDto.setPassword(null);
            
            response.put("success", true);
            response.put("data", userDto);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param updatedUser 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id, 
            @RequestBody User updatedUser) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.updateUser(id, updatedUser);
            user.setPassword(null); // 移除密码字段
            
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = userService.deleteUser(id);
        if (success) {
            response.put("success", true);
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取用户列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向
     * @return 用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order) {
        
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        
        Page<User> users = userService.findAllUsers(pageable);
        
        // 移除密码字段
        users.forEach(user -> user.setPassword(null));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索用户
     * 
     * @param username 用户名
     * @param realName 真实姓名
     * @param role 用户角色
     * @param status 用户状态
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userService.searchUsers(username, realName, role, status, pageable);
        
        // 移除密码字段
        users.forEach(user -> user.setPassword(null));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据角色获取用户
     * 
     * @param role 用户角色
     * @return 用户列表
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.findByRole(role);
        
        // 移除密码字段
        users.forEach(user -> user.setPassword(null));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据状态获取用户
     * 
     * @param status 用户状态
     * @return 用户列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsersByStatus(@PathVariable UserStatus status) {
        List<User> users = userService.findByStatus(status);
        
        // 移除密码字段
        users.forEach(user -> user.setPassword(null));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, UserStatus> request) {
        
        UserStatus status = request.get("status");
        boolean success = userService.updateUserStatus(id, status);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "用户状态更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 修改密码
     * 
     * @param id 用户ID
     * @param passwordRequest 密码请求
     * @return 修改结果
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordRequest) {
        
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = userService.changePassword(id, oldPassword, newPassword);
            if (success) {
                response.put("success", true);
                response.put("message", "密码修改成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "密码修改失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 重置密码
     * 
     * @param id 用户ID
     * @param passwordRequest 密码请求
     * @return 重置结果
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordRequest) {
        
        String newPassword = passwordRequest.get("newPassword");
        boolean success = userService.resetPassword(id, newPassword);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "密码重置成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 绑定设备
     * 
     * @param id 用户ID
     * @param deviceRequest 设备请求
     * @return 绑定结果
     */
    @PostMapping("/{id}/bind-device")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Map<String, Object>> bindDevice(
            @PathVariable Long id,
            @RequestBody Map<String, String> deviceRequest) {
        
        String deviceId = deviceRequest.get("deviceId");
        boolean success = userService.bindDevice(id, deviceId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备绑定成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 解绑设备
     * 
     * @param id 用户ID
     * @param deviceRequest 设备请求
     * @return 解绑结果
     */
    @PostMapping("/{id}/unbind-device")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Map<String, Object>> unbindDevice(
            @PathVariable Long id,
            @RequestBody Map<String, String> deviceRequest) {
        
        String deviceId = deviceRequest.get("deviceId");
        boolean success = userService.unbindDevice(id, deviceId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备解绑成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在或设备未绑定");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        UserService.UserStatistics statistics = userService.getUserStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", statistics);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 验证用户名是否可用
     * 
     * @param username 用户名
     * @return 验证结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Optional<User> user = userService.findByUsername(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", !user.isPresent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 验证邮箱是否可用
     * 
     * @param email 邮箱
     * @return 验证结果
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Optional<User> user = userService.findByEmail(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", !user.isPresent());
        
        return ResponseEntity.ok(response);
    }
}