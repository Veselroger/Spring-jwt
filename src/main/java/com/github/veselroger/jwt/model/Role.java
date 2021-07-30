package com.github.veselroger.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Roles control access to rest methods.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // For Hibernate
@Entity(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @Column(name = "role_id")
    private Long id;
    @Column(name = "role_name")
    private String name;

    @Override
    public String getAuthority() {
        return this.name;
    }
}
