package com.github.veselroger.jwt.security;

import com.github.veselroger.jwt.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * User Details Implementation for Spring Security mechanisms.
 */
public class UserDetailsImpl implements UserDetails {
    @Getter
    private User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles();
    }

    @Override
    public boolean isEnabled() {
        return user.getDisabled() == null || !user.getDisabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isEnabled();
    }

}
