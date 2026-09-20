package com.opspilot.service;

import com.opspilot.entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> list();
    Employee getById(Long id);
    boolean save(Employee employee);
    boolean update(Employee employee);
    boolean remove(Long id);
}
