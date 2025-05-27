package com.internship.recommendation_service.config;

import com.internship.authentication_library.config.SecurityConfiguration;
import com.internship.recommendation_service.constant.PathPermissionConstants;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityConfiguration securityConfiguration;
    private final PathPermissionConstants pathPermissionConstants;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        HttpSecurity http = httpSecurity
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll());

        return securityConfiguration.securityFilterChain(http,
                pathPermissionConstants.getPermittedRequestsForAllUsers(),
                pathPermissionConstants.getPermittedRequestForSuperAdmin(),
                pathPermissionConstants.getPermittedRequestsForAdminOrSuperAdmin(),
                pathPermissionConstants.getPermittedRequestsForUsersOrAdminOrSuperAdmin()
        );
    }
}
