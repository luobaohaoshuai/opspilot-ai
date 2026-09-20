package com.opspilot.controller;

import com.opspilot.common.result.ErrorCode;
import com.opspilot.common.result.Result;
import com.opspilot.entity.Employee;
import com.opspilot.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Result<List<Employee>> list() {
        return Result.ok(employeeService.list());
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee e = employeeService.getById(id);
        if (e == null) {
            return Result.fail(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return Result.ok(e);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> save(@Valid @RequestBody Employee employee) {
        boolean success = employeeService.save(employee);
        if (success) {
            return Result.ok("添加成功");
        }
        return Result.fail(ErrorCode.INTERNAL_ERROR, "添加失败");
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> update(@Valid @RequestBody Employee employee) {
        if (employeeService.update(employee)) {
            return Result.ok("修改成功");
        }
        return Result.fail(ErrorCode.INTERNAL_ERROR, "修改失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> remove(@PathVariable Long id) {
        if (employeeService.remove(id)) {
            return Result.ok("删除成功");
        }
        return Result.fail(ErrorCode.INTERNAL_ERROR, "删除失败");
    }
}
