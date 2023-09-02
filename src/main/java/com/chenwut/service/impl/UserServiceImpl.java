package com.chenwut.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenwut.entity.User;
import com.chenwut.mapper.UserMapper;
import com.chenwut.service.UserService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}




