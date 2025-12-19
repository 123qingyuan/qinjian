package main.java.com.elderly.monitoring.user.repository;

import main.java.com.elderly.monitoring.user.entity.User;
import main.java.com.elderly.monitoring.user.entity.UserRole;
import main.java.com.elderly.monitoring.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     * 
     * @param username 用户名
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据角色查找用户
     * 
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> findByRole(UserRole role);

    /**
     * 根据状态查找用户
     * 
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 根据角色和状态查找用户
     * 
     * @param role 用户角色
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    /**
     * 根据真实姓名模糊查询用户
     * 
     * @param realName 真实姓名
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:realName% AND u.deleted = false")
    Page<User> findByRealNameContaining(@Param("realName") String realName, Pageable pageable);

    /**
     * 根据手机号模糊查询用户
     * 
     * @param phone 手机号
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE u.phone LIKE %:phone% AND u.deleted = false")
    Page<User> findByPhoneContaining(@Param("phone") String phone, Pageable pageable);

    /**
     * 根据多条件查询用户
     * 
     * @param username 用户名
     * @param realName 真实姓名
     * @param role 用户角色
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "u.deleted = false")
    Page<User> findByMultipleConditions(
            @Param("username") String username,
            @Param("realName") String realName,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable);

    /**
     * 查找所有未删除的用户
     * 
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false ORDER BY u.createdAt DESC")
    Page<User> findAllActiveUsers(Pageable pageable);

    /**
     * 统计各角色用户数量
     * 
     * @return 统计结果
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.deleted = false GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * 统计各状态用户数量
     * 
     * @return 统计结果
     */
    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.deleted = false GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    /**
     * 根据设备ID查找关联用户
     * 
     * @param deviceId 设备ID
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.deviceIds LIKE %:deviceId% AND u.deleted = false")
    List<User> findByDeviceId(@Param("deviceId") String deviceId);
}