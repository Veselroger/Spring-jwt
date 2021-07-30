package com.github.veselroger.jwt.requests;

import lombok.Data;

/**
 * DTO for Sign Up request from a User.
 */
@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
}
