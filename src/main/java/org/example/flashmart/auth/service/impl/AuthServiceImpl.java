package org.example.flashmart.auth.service.impl;

import org.example.flashmart.auth.model.dto.AuthDTO;
import org.example.flashmart.auth.model.dto.LoginDTO;
import org.example.flashmart.auth.model.dto.RegisterDTO;
import org.example.flashmart.auth.service.AuthService;
import org.example.flashmart.auth.util.JwtUtil;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.user.mapper.UserMapper;
import org.example.flashmart.user.model.dataobject.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthDTO login(LoginDTO loginDTO) {
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String account = loginDTO.getUsername().trim();
        if (account.isEmpty() || loginDTO.getPassword().trim().isEmpty()) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        UserDO userDO = userMapper.selectByAccount(account);
        if (userDO == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!loginDTO.getPassword().trim().equals(userDO.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(userDO.getId(), userDO.getUsername(), "");
        return new AuthDTO(token, userDO.getUsername());
    }

    @Override
    public AuthDTO register(RegisterDTO registerDTO) {
        if (registerDTO.getUsername() == null || registerDTO.getEmail() == null || registerDTO.getPassword() == null) {
            throw new BusinessException("请完整填写注册信息");
        }

        String username = registerDTO.getUsername().trim();
        String email = registerDTO.getEmail().trim();
        String password = registerDTO.getPassword().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            throw new BusinessException("请完整填写注册信息");
        }

        if (userMapper.countByUsername(username) > 0) {
            throw new BusinessException("用户名已存在");
        }

        if (userMapper.countByEmail(email) > 0) {
            throw new BusinessException("邮箱已存在");
        }

        if (userMapper.countByEmail(username) > 0) {
            throw new BusinessException("用户名不能与已有邮箱重复");
        }

        if (userMapper.countByUsername(email) > 0) {
            throw new BusinessException("邮箱不能与已有用户名重复");
        }

        int inserted = userMapper.insertUser(username, email, password);
        if (inserted <= 0) {
            throw new BusinessException(500, "注册失败，请稍后重试");
        }

        UserDO createdUser = userMapper.selectByUsername(username);
        if (createdUser == null) {
            throw new BusinessException(500, "注册失败，请稍后重试");
        }

        String token = jwtUtil.generateToken(createdUser.getId(), createdUser.getUsername(), "");
        return new AuthDTO(token, createdUser.getUsername());
    }
}
