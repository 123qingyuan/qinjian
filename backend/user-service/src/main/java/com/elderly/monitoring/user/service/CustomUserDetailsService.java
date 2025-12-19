package main.java.com.elderly.monitoring.user.service;

import main.java.com.elderly.monitoring.user.entity.User;
import main.java.com.elderly.monitoring.user.entity.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * 自定义用户详情服务
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User user = userOpt.get();
        
        // 检查用户状态
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UsernameNotFoundException("用户未激活: " + username);
        }
        
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UsernameNotFoundException("用户已被暂停: " + username);
        }
        
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UsernameNotFoundException("用户已被删除: " + username);
        }

        // 构建权限列表
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}