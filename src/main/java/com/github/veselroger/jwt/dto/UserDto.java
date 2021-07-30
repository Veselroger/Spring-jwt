package com.github.veselroger.jwt.dto;

import com.github.veselroger.jwt.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Specific "View" for {@link User}. Allows to avoid exposing of entities to REST.
 */
@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}
