package org.example.flashmart.auth.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.flashmart.auth.model.dto.AuthDTO;
import org.example.flashmart.auth.model.dto.LoginDTO;
import org.example.flashmart.auth.model.dto.RegisterDTO;
import org.example.flashmart.auth.service.AuthService;
import org.example.flashmart.common.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping({"/api/auth", "/api/user"})
@Slf4j
@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<AuthDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthDTO authDTO = authService.login(loginDTO);
        return Result.success(authDTO);
    }

    @PostMapping("/register")
    public Result<AuthDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        AuthDTO authDTO = authService.register(registerDTO);
        return Result.success(authDTO);
    }
}
