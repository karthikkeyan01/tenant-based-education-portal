package com.fts.tenantbasededuportal.config;

import com.fts.tenantbasededuportal.security.CustomUserDetailsService;
import com.fts.tenantbasededuportal.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/auth/login",
                                        "/auth/register", "/auth/resend-otp",
                                        "/auth/verify-otp", "/auth/activate-account",
                                        "/auth/forgot-password", "/auth/reset-password",
                                        "/auth/logout", "/error").permitAll()
                                .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")
                                .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))
                .authenticationProvider(this.authenticationProvider())
                .addFilterBefore(this.jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider(this.customUserDetailsService);
        provider.setPasswordEncoder(this.passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration configuration) {

        return configuration.getAuthenticationManager();
    }
}


