package org.example.flashmart.user.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdTime;
}
