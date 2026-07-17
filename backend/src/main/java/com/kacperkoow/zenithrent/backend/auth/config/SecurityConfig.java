package com.kacperkoow.zenithrent.backend.auth.config;

import com.kacperkoow.zenithrent.backend.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public RoleHierarchy roleHierarchy() {

        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("EMPLOYEE")
                .role("EMPLOYEE").implies("CUSTOMER")
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cars/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/cars/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/cars/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/cars/**").hasRole("EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/bookings/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/my").hasRole("CUSTOMER")


                        .requestMatchers(HttpMethod.GET, "/api/bookings/**").hasRole("EMPLOYEE")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}