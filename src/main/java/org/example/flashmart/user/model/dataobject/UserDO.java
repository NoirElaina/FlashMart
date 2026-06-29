package org.example.flashmart.user.model.dataobject;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String role;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
