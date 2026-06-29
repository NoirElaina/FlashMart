package org.example.flashmart.user.controller;

import org.example.flashmart.common.response.Result;
import org.example.flashmart.user.model.vo.UserVO;
import org.example.flashmart.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<UserVO> currentUser(@RequestAttribute Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }
}
