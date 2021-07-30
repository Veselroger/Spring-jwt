package com.github.veselroger.jwt.controllers;

import com.github.veselroger.jwt.dto.UserDto;
import com.github.veselroger.jwt.model.Roles;
import com.github.veselroger.jwt.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

/**
 * Controller that provides methods related to {@link UserDto}.
 */
@RestController
@RequestMapping("/users")
@RolesAllowed(Roles.USER)
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{name}")
    UserDto getUser(@PathVariable String name) {
        return new UserDto(userService.getUser(name));
    }
}
