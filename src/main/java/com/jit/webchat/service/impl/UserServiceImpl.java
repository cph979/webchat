package com.jit.webchat.service.impl;

import com.jit.webchat.bean.User;
import com.jit.webchat.mapper.UserMapper;
import com.jit.webchat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Boolean existsUsername(String username) {
        if (userMapper.queryUserByUsername(username) != null) {
            return true;
        }
        return false;
    }

    @Override
    public void saveUser(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public User login(String username, String password) {
        return userMapper.queryUserByUsernameAndPassword(username, password);
    }
}
