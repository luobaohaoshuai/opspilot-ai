package com.opspilot.controller;

import com.opspilot.common.result.Result;
import com.opspilot.entity.CreateUserRequest;
import com.opspilot.entity.UpdateUserPasswordRequest;
import com.opspilot.entity.UpdateUserRoleRequest;
import com.opspilot.entity.UserResponse;
import com.opspilot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<UserResponse>> list() {
        return Result.ok(userService.listUsers());
    }

    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return Result.ok(userService.createUser(request));
    }

    @PutMapping("/{id}/password")
    public Result<String> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPasswordRequest request) {
        userService.resetPassword(id, request.getPassword());
        return Result.ok("密码重置成功");
    }

    @PutMapping("/{id}/role")
    public Result<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return Result.ok(userService.updateRole(id, request.getRole()));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, authentication.getName());
        return Result.ok("用户删除成功");
    }
}
