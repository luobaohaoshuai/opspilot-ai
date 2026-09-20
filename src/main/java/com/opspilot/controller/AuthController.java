package com.opspilot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.common.result.ErrorCode;
import com.opspilot.common.RateLimit;
import com.opspilot.common.security.JwtUtil;
import com.opspilot.common.result.Result;
import com.opspilot.entity.LoginRequest;
import com.opspilot.entity.User;
import com.opspilot.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;


    public AuthController(UserMapper userMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder){
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @RateLimit(maxRequests = 10)
    @PostMapping ("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return Result.ok(token);
        }
        return Result.fail(ErrorCode.UNAUTHORIZED, "账号或密码错误");
    }
}
