package main.java.com.elderly.monitoring.user.service;

import main.java.com.elderly.monitoring.user.entity.User;
import main.java.com.elderly.monitoring.user.entity.UserRole;
import main.java.com.elderly.monitoring.user.entity.UserStatus;
import main.java.com.elderly.monitoring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 创建的用户
     */
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    /**
     * 根据ID查找用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param updatedUser 更新的用户信息
     * @return 更新后的用户信息
     */
    public User updateUser(Long id, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (!existingUserOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User existingUser = existingUserOpt.get();

        // 检查用户名是否被其他用户使用
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) 
                && userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否被其他用户使用
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) 
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 更新用户信息
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setRealName(updatedUser.getRealName());
        existingUser.setIdCard(updatedUser.getIdCard());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setStatus(updatedUser.getStatus());
        existingUser.setAvatarUrl(updatedUser.getAvatarUrl());
        existingUser.setEmergencyContact(updatedUser.getEmergencyContact());
        existingUser.setEmergencyPhone(updatedUser.getEmergencyPhone());
        existingUser.setHealthInfo(updatedUser.getHealthInfo());

        // 如果提供了新密码，则加密并更新
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }

    /**
     * 重置密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    public boolean resetPassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }

    /**
     * 删除用户（软删除）
     * 
     * @param id 用户ID
     * @return 是否删除成功
     */
    public boolean deleteUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return false;
        }

        User user = userOpt.get();
        user.setDeleted(true);
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        
        return true;
    }

    /**
     * 分页查询用户
     * 
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAllActiveUsers(pageable);
    }

    /**
     * 根据角色查询用户
     * 
     * @param role 用户角色
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * 根据状态查询用户
     * 
     * @param status 用户状态
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    /**
     * 多条件查询用户
     * 
     * @param username 用户名
     * @param realName 真实姓名
     * @param role 用户角色
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String username, String realName, UserRole role, UserStatus status, Pageable pageable) {
        return userRepository.findByMultipleConditions(username, realName, role, status, pageable);
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public boolean updateUserStatus(Long userId, UserStatus status) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }

        User user = userOpt.get();
        user.setStatus(status);
        userRepository.save(user);
        
        return true;
    }

    /**
     * 绑定设备到用户
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 是否绑定成功
     */
    public boolean bindDevice(Long userId, String deviceId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }

        User user = userOpt.get();
        String currentDeviceIds = user.getDeviceIds();
        
        if (currentDeviceIds == null || currentDeviceIds.isEmpty()) {
            user.setDeviceIds(deviceId);
        } else {
            // 检查设备是否已绑定
            if (currentDeviceIds.contains(deviceId)) {
                return true; // 已绑定
            }
            user.setDeviceIds(currentDeviceIds + "," + deviceId);
        }
        
        userRepository.save(user);
        return true;
    }

    /**
     * 解绑设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 是否解绑成功
     */
    public boolean unbindDevice(Long userId, String deviceId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }

        User user = userOpt.get();
        String currentDeviceIds = user.getDeviceIds();
        
        if (currentDeviceIds == null || currentDeviceIds.isEmpty()) {
            return false;
        }
        
        // 移除指定设备ID
        String newDeviceIds = currentDeviceIds.replaceAll(deviceId + ",?", "").replaceAll(",$", "");
        user.setDeviceIds(newDeviceIds);
        
        userRepository.save(user);
        return true;
    }

    /**
     * 获取用户统计信息
     * 
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        List<Object[]> roleStats = userRepository.countUsersByRole();
        List<Object[]> statusStats = userRepository.countUsersByStatus();
        
        UserStatistics statistics = new UserStatistics();
        
        // 处理角色统计
        for (Object[] stat : roleStats) {
            UserRole role = (UserRole) stat[0];
            Long count = (Long) stat[1];
            statistics.addRoleCount(role, count);
        }
        
        // 处理状态统计
        for (Object[] stat : statusStats) {
            UserStatus status = (UserStatus) stat[0];
            Long count = (Long) stat[1];
            statistics.addStatusCount(status, count);
        }
        
        return statistics;
    }

    /**
     * 验证用户密码
     * 
     * @param username 用户名
     * @param password 密码
     * @return 是否验证成功
     */
    @Transactional(readOnly = true)
    public boolean validatePassword(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return false;
        }

        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 用户统计信息类
     */
    public static class UserStatistics {
        private Long totalUsers = 0L;
        private Long adminCount = 0L;
        private Long familyMemberCount = 0L;
        private Long caregiverCount = 0L;
        private Long doctorCount = 0L;
        private Long elderlyCount = 0L;
        private Long activeCount = 0L;
        private Long inactiveCount = 0L;
        private Long suspendedCount = 0L;

        // Getter和Setter方法
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

        public Long getAdminCount() { return adminCount; }
        public void setAdminCount(Long adminCount) { this.adminCount = adminCount; }

        public Long getFamilyMemberCount() { return familyMemberCount; }
        public void setFamilyMemberCount(Long familyMemberCount) { this.familyMemberCount = familyMemberCount; }

        public Long getCaregiverCount() { return caregiverCount; }
        public void setCaregiverCount(Long caregiverCount) { this.caregiverCount = caregiverCount; }

        public Long getDoctorCount() { return doctorCount; }
        public void setDoctorCount(Long doctorCount) { this.doctorCount = doctorCount; }

        public Long getElderlyCount() { return elderlyCount; }
        public void setElderlyCount(Long elderlyCount) { this.elderlyCount = elderlyCount; }

        public Long getActiveCount() { return activeCount; }
        public void setActiveCount(Long activeCount) { this.activeCount = activeCount; }

        public Long getInactiveCount() { return inactiveCount; }
        public void setInactiveCount(Long inactiveCount) { this.inactiveCount = inactiveCount; }

        public Long getSuspendedCount() { return suspendedCount; }
        public void setSuspendedCount(Long suspendedCount) { this.suspendedCount = suspendedCount; }

        public void addRoleCount(UserRole role, Long count) {
            totalUsers += count;
            switch (role) {
                case ADMIN:
                    adminCount = count;
                    break;
                case FAMILY_MEMBER:
                    familyMemberCount = count;
                    break;
                case CAREGIVER:
                    caregiverCount = count;
                    break;
                case DOCTOR:
                    doctorCount = count;
                    break;
                case ELDERLY:
                    elderlyCount = count;
                    break;
            }
        }

        public void addStatusCount(UserStatus status, Long count) {
            switch (status) {
                case ACTIVE:
                    activeCount = count;
                    break;
                case INACTIVE:
                    inactiveCount = count;
                    break;
                case SUSPENDED:
                    suspendedCount = count;
                    break;
                case DELETED:
                    // 已删除用户不计入总统计
                    break;
            }
        }
    }
}