package service;

import common.User;

public interface UserService {
    User getUserById(Integer id);
    // 插入
    Integer insertUser(User user);
}
