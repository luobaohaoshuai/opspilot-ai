package com.opspilot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.CreateUserRequest;
import com.opspilot.entity.User;
import com.opspilot.entity.UserResponse;
import com.opspilot.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userMapper, passwordEncoder);
    }

    @Test
    void createUserEncryptsPasswordAndNormalizesRole() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(" operator01 ");
        request.setPassword("password123");
        request.setRole("operator");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        UserResponse response = userService.createUser(request);

        assertEquals(2L, response.getId());
        assertEquals("operator01", response.getUsername());
        assertEquals("OPERATOR", response.getRole());
        verify(passwordEncoder).encode("password123");
        verify(userMapper).insert(argThat(user ->
                "operator01".equals(user.getUsername())
                        && "bcrypt-hash".equals(user.getPassword())
                        && "OPERATOR".equals(user.getRole())));
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setPassword("password123");
        request.setRole("ADMIN");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(user(1L, "admin", "ADMIN"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request));

        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void updateRoleDoesNotDemoteLastAdmin() {
        when(userMapper.selectById(1L)).thenReturn(user(1L, "admin", "ADMIN"));
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateRole(1L, "OPERATOR"));

        assertTrue(exception.getMessage().contains("最后一个管理员"));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void deleteUserDoesNotDeleteCurrentAccount() {
        when(userMapper.selectById(1L)).thenReturn(user(1L, "admin", "ADMIN"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(1L, "admin"));

        assertTrue(exception.getMessage().contains("当前登录"));
        verify(userMapper, never()).deleteById(1L);
    }

    @Test
    void resetPasswordStoresEncodedValue() {
        User user = user(2L, "operator01", "OPERATOR");
        when(userMapper.selectById(2L)).thenReturn(user);
        when(passwordEncoder.encode("new-password")).thenReturn("new-bcrypt-hash");

        userService.resetPassword(2L, "new-password");

        assertEquals("new-bcrypt-hash", user.getPassword());
        verify(userMapper).updateById(user);
    }

    private User user(Long id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setPassword("existing-hash");
        return user;
    }
}
