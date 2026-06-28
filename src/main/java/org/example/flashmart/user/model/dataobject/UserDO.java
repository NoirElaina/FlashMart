package org.example.flashmart.user.model.dataobject;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
