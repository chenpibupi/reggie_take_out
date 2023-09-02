package com.chenwut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenwut.entity.Employee;
import com.chenwut.mapper.EmployeeMapper;
import com.chenwut.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
        implements EmployeeService {

    @Autowired
    private EmployeeMapper empMapper;

    @Override
    public Employee login(HttpServletRequest request, Employee emp) {

        String username = emp.getUsername();
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != username, Employee::getUsername, username);
        Employee selectOne = empMapper.selectOne(queryWrapper);

        return selectOne;
    }

    @Override
    public void saveEmp(HttpServletRequest request, Employee employee) {
        //设置默认密码为123456，并采用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置createTime和updateTime
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人ID和修改人ID
        //1.根据Session获取ID
//        Long empId = (Long) request.getSession().getAttribute("employee");
        //2.设置ID
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        log.info("待存储员工信息为：{}", employee);
        empMapper.insert(employee);
    }

    @Override
    public Page<Employee> page(int page, int pageSize, String name) {
        //1 创建IPage分页对象,设置分页参数
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        //添加条件
        qw.eq(StringUtils.isNotBlank(name), Employee::getName, name)
                .orderByDesc(Employee::getUpdateTime);
        //2 执行分页查询
        empMapper.selectPage(pageInfo, qw);
        return pageInfo;
    }

    @Override
    public void updateEmp(HttpServletRequest request, Employee employee) {
        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        empMapper.updateById(employee);
    }

}




