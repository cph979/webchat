package com.jit.webchat.service;

import com.jit.webchat.bean.User;

public interface UserService {
    Boolean existsUsername(String username);
    void saveUser(User user);
    User login(String username, String password);
}
