package user.service;

import user.entity.User;

public interface UserService {
    User getUserById(Integer id);
    // 插入
    Integer insertUser(User user);
}
