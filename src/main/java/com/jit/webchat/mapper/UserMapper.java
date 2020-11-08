package com.jit.webchat.mapper;

import com.jit.webchat.bean.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User queryUserByUsername(String username);

    void insertUser(User user);

    User queryUserByUsernameAndPassword(String username, String password);
}
