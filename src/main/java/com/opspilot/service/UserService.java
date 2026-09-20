package com.opspilot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.CreateUserRequest;
import com.opspilot.entity.User;
import com.opspilot.entity.UserResponse;
import com.opspilot.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {
    private static final String ADMIN = "ADMIN";
    private static final Set<String> ALLOWED_ROLES = Set.of(ADMIN, "OPERATOR");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userMapper.selectList(null).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.getUsername().trim();
        if (findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole()));
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("用户名已存在", e);
        }
        return toResponse(user);
    }

    @Transactional
    public void resetPassword(Long id, String password) {
        User user = requireUser(id);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    @Transactional
    public UserResponse updateRole(Long id, String role) {
        User user = requireUser(id);
        String normalizedRole = normalizeRole(role);
        if (ADMIN.equals(user.getRole()) && !ADMIN.equals(normalizedRole) && countAdmins() <= 1) {
            throw new IllegalArgumentException("不能降级系统中的最后一个管理员");
        }
        user.setRole(normalizedRole);
        userMapper.updateById(user);
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        User user = requireUser(id);
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("不能删除当前登录的管理员账户");
        }
        if (ADMIN.equals(user.getRole()) && countAdmins() <= 1) {
            throw new IllegalArgumentException("不能删除系统中的最后一个管理员");
        }
        userMapper.deleteById(id);
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    private long countAdmins() {
        return userMapper.selectCount(new QueryWrapper<User>().eq("role", ADMIN));
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new IllegalArgumentException("角色只能是 ADMIN 或 OPERATOR");
        }
        return normalized;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
