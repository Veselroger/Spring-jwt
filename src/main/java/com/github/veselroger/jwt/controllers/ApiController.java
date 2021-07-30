package com.github.veselroger.jwt.controllers;

import com.github.veselroger.jwt.dto.UserDto;
import com.github.veselroger.jwt.model.User;
import com.github.veselroger.jwt.requests.AuthRequest;
import com.github.veselroger.jwt.requests.CreateUserRequest;
import com.github.veselroger.jwt.security.UserDetailsImpl;
import com.github.veselroger.jwt.services.JwtService;
import com.github.veselroger.jwt.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that provides public api methods that don't require authentication.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("login")
    public ResponseEntity<UserDto> login(@RequestBody AuthRequest request) {
        try {
            Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authenticate.getPrincipal();
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtService.generateAccessToken(userDetails.getUser()))
                    .body(new UserDto(userDetails.getUser()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("register")
    public UserDto register(@RequestBody CreateUserRequest request) {
        String passwd = passwordEncoder.encode(request.getPassword());
        User newUser = userService.createUser(request.getUsername(), passwd, request.getEmail());
        return new UserDto(newUser);
    }
}
