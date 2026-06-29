package org.example.flashmart.user.service;

import org.example.flashmart.user.model.vo.UserVO;

public interface UserService {
    UserVO getCurrentUser(Long userId);
}
