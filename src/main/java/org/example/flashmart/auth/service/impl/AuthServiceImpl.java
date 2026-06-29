package org.example.flashmart.auth.service.impl;

import org.example.flashmart.auth.model.dto.AuthDTO;
import org.example.flashmart.auth.model.dto.LoginDTO;
import org.example.flashmart.auth.model.dto.RegisterDTO;
import org.example.flashmart.auth.service.AuthService;
import org.example.flashmart.auth.service.LoginAttemptService;
import org.example.flashmart.auth.service.TokenBlacklistService;
import org.example.flashmart.auth.util.JwtUtil;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.common.response.ResultCode;
import org.example.flashmart.user.mapper.UserMapper;
import org.example.flashmart.user.model.dataobject.UserDO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(UserMapper userMapper,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder,
                           LoginAttemptService loginAttemptService,
                           TokenBlacklistService tokenBlacklistService) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public AuthDTO login(LoginDTO loginDTO) {
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        String account = loginDTO.getUsername().trim();
        String password = loginDTO.getPassword().trim();
        if (account.isEmpty() || password.isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        if (loginAttemptService.isLocked(account)) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), "登录失败次数过多，请稍后再试");
        }

        UserDO userDO = userMapper.selectByAccount(account);
        if (userDO == null || !passwordEncoder.matches(password, userDO.getPassword())) {
            loginAttemptService.recordFailure(account);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        loginAttemptService.reset(account);
        return buildAuth(userDO);
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

        String encodedPassword = passwordEncoder.encode(password);
        int inserted = userMapper.insertUser(username, email, encodedPassword, DEFAULT_ROLE);
        if (inserted <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "注册失败，请稍后重试");
        }

        UserDO createdUser = userMapper.selectByUsername(username);
        if (createdUser == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "注册失败，请稍后重试");
        }
        return buildAuth(createdUser);
    }

    @Override
    public AuthDTO refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank() || !jwtUtil.isValid(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "刷新令牌无效或已过期");
        }
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(jwtUtil.getTokenType(refreshToken))) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "刷新令牌类型不正确");
        }
        if (tokenBlacklistService.isBlacklisted(jwtUtil.getJti(refreshToken))) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "刷新令牌已失效，请重新登录");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        UserDO userDO = userMapper.selectById(userId.intValue());
        if (userDO == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户不存在");
        }
        return buildAuth(userDO);
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || !jwtUtil.isValid(accessToken)) {
            return;
        }
        long ttl = jwtUtil.getExpiration(accessToken).getTime() - System.currentTimeMillis();
        tokenBlacklistService.blacklist(jwtUtil.getJti(accessToken), ttl);
    }

    private AuthDTO buildAuth(UserDO userDO) {
        String role = userDO.getRole() == null ? DEFAULT_ROLE : userDO.getRole();
        String accessToken = jwtUtil.generateAccessToken(userDO.getId(), userDO.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(userDO.getId(), userDO.getUsername(), role);
        return new AuthDTO(accessToken, refreshToken, userDO.getUsername(), role);
    }
}
