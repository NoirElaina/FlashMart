package org.example.flashmart.auth.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.flashmart.auth.model.dto.AuthDTO;
import org.example.flashmart.auth.model.dto.LoginDTO;
import org.example.flashmart.auth.model.dto.RefreshTokenDTO;
import org.example.flashmart.auth.model.dto.RegisterDTO;
import org.example.flashmart.auth.service.AuthService;
import org.example.flashmart.common.response.Result;
import org.springframework.web.bind.annotation.*;

@RequestMapping({"/api/auth", "/api/user"})
@Slf4j
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<AuthDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<AuthDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/refresh")
    public Result<AuthDTO> refresh(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        return Result.success(authService.refresh(refreshTokenDTO.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7));
        }
        return Result.success("退出登录成功");
    }
}
