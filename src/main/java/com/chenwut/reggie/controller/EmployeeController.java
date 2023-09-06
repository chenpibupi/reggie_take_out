package com.chenwut.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenwut.reggie.common.Result;
import com.chenwut.reggie.entity.Employee;
import com.chenwut.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService empService;

    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee emp) {

        log.info("员工信息：{} : {}", emp.getUsername(), emp.getPassword());
        //接收前端的密码进行 md5 加密
        String password = emp.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        emp.setPassword(password);

        Employee employee = empService.login(request, emp);

        if (employee == null) {
            return Result.error("登陆失败");
        }
        if (!password.equals(employee.getPassword())) {
            return Result.error("账号或密码错误");
        }
        if (employee.getStatus() == 0) {
            return Result.error("账号已禁用");
        }
        //登录成功，将员工 id 存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", employee.getId());
        return Result.success(employee);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功");
    }

    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增的员工信息：{}", employee.toString());
        empService.saveEmp(request, employee);
        return Result.success("员工添加成功");
    }

    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);
        Page<Employee> pageInfo = empService.page(page, pageSize, name);
        return Result.success(pageInfo);
    }

    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        //在这里获取一下线程id
        long id = Thread.currentThread().getId();
        log.info("doFilter的线程id为：{}", id);

        empService.updateEmp(request, employee);
        return Result.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息..");
        Employee employee = empService.getById(id);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error("未查询到该员工信息");
    }
}
