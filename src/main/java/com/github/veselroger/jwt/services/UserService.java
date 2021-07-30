package com.github.veselroger.jwt.services;

import com.github.veselroger.jwt.model.User;
import com.github.veselroger.jwt.repositories.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Service to work with {@link User}
 */
@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String name) {
        return userRepository.findByName(name);
    }

    public User createUser(String name, String password, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);
        return user;
    }
}
