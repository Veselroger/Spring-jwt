package com.github.veselroger.jwt.requests;

import lombok.Data;

/**
 * DTO for Authentication request from a User.
 */
@Data
public class AuthRequest {
    private String username;
    private String password;
}
