package org.example.flashmart.auth.service;

import org.example.flashmart.auth.model.dto.AuthDTO;
import org.example.flashmart.auth.model.dto.LoginDTO;
import org.example.flashmart.auth.model.dto.RegisterDTO;

public interface AuthService {
    AuthDTO login(LoginDTO loginDTO);

    AuthDTO register(RegisterDTO registerDTO);

    AuthDTO refresh(String refreshToken);

    void logout(String accessToken);
}
