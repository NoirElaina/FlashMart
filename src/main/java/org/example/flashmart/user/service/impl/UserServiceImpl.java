package org.example.flashmart.user.service.impl;

import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.common.response.ResultCode;
import org.example.flashmart.user.mapper.UserMapper;
import org.example.flashmart.user.model.dataobject.UserDO;
import org.example.flashmart.user.model.vo.UserVO;
import org.example.flashmart.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        UserDO userDO = userMapper.selectById(userId.intValue());
        if (userDO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return toVO(userDO);
    }

    private UserVO toVO(UserDO userDO) {
        UserVO vo = new UserVO();
        vo.setId(userDO.getId());
        vo.setUsername(userDO.getUsername());
        vo.setEmail(userDO.getEmail());
        vo.setRole(userDO.getRole());
        vo.setCreatedTime(userDO.getCreatedTime());
        return vo;
    }
}
