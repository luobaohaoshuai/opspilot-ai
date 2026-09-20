package com.opspilot.service.impl;

import com.opspilot.entity.Employee;
import com.opspilot.mapper.EmployeeMapper;
import com.opspilot.service.EmployeeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    @Cacheable(value = "employeeList")
    public List<Employee> list() {
        return employeeMapper.selectList(null);
    }

    @Override
    @Cacheable(value = "employee", key = "#id")
    public Employee getById(Long id) {
        return employeeMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "employeeList", allEntries = true)
    public boolean save(Employee employee) {
        return employeeMapper.insert(employee) > 0;
    }

    @Override
    @CacheEvict(value = {"employeeList", "employee"}, key = "#employee.id")
    public boolean update(Employee employee) {
        return employeeMapper.updateById(employee) > 0;
    }

    @Override
    @CacheEvict(value = {"employeeList", "employee"}, key = "#id")
    public boolean remove(Long id) {
        return employeeMapper.deleteById(id) > 0;
    }
}
