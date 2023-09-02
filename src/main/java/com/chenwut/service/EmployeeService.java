package com.chenwut.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.entity.Employee;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface EmployeeService extends IService<Employee> {
    Employee login(HttpServletRequest request, Employee emp);

    void saveEmp(HttpServletRequest request, Employee employee);

    Page page(int page, int pageSize, String name);

    void updateEmp(HttpServletRequest request, Employee employee);
}
