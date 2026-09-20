package com.opspilot.service;

import com.opspilot.TestConfig;
import com.opspilot.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class EmployeeServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void 查询所有员工_应返回初始数据() {
        List<Employee> list = employeeService.list();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    void 按ID查询员工() {
        Employee e = employeeService.getById(1L);
        assertNotNull(e);
        assertEquals("张三", e.getName());
    }

    @Test
    void 新增员工() {
        Employee employee = new Employee();
        employee.setName("测试员工");
        employee.setAge(22);
        employee.setDepartment("技术部");

        boolean result = employeeService.save(employee);
        assertTrue(result);
        assertNotNull(employee.getId());
    }

    @Test
    void 修改员工() {
        Employee e = employeeService.getById(1L);
        assertNotNull(e);

        e.setName("改名后的张三");
        boolean result = employeeService.update(e);
        assertTrue(result);

        Employee updated = employeeService.getById(1L);
        assertEquals("改名后的张三", updated.getName());
    }

    @Test
    void 删除员工() {
        Employee employee = new Employee();
        employee.setName("待删除");
        employee.setAge(20);
        employee.setDepartment("测试部");
        employeeService.save(employee);

        boolean result = employeeService.remove(employee.getId());
        assertTrue(result);

        Employee deleted = employeeService.getById(employee.getId());
        assertNull(deleted);
    }

    @Test
    void 新增后缓存应被清理() {
        employeeService.list();

        Employee employee = new Employee();
        employee.setName("缓存测试");
        employee.setAge(21);
        employee.setDepartment("缓存部");
        employeeService.save(employee);

        List<Employee> list = employeeService.list();
        boolean found = list.stream().anyMatch(s -> "缓存测试".equals(s.getName()));
        assertTrue(found, "新增后 list 应包含新员工（缓存已清除）");
    }

    @Test
    void 按ID查询不存在的ID应返回null() {
        Employee e = employeeService.getById(9999L);
        assertNull(e);
    }
}
