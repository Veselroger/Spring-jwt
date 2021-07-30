package com.github.veselroger.jwt.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Specific config for additional settings that should be handled AFTER {@link SecurityConfig}
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Remove the default ROLE_ prefix
        return new GrantedAuthorityDefaults("");
    }
}
