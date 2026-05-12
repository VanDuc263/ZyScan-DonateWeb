package org.example.donatebackend.config;

import org.example.donatebackend.util.JwtFilter;
import org.example.donatebackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/donate/history/received").hasRole("STREAMER")
                        .requestMatchers("/api/donate/history", "/api/donate/history/sent").authenticated()
                        .requestMatchers("/api/donate/**").permitAll()
                        .requestMatchers("/api/streamers/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()

                        .requestMatchers("/api/payments/create-qr").permitAll()
                        .requestMatchers("/api/payments/*/status").permitAll()
                        .requestMatchers("/api/payments/*/sandbox-simulate").permitAll()
                        .requestMatchers("/api/payments/vietqr/callback").permitAll()
                        .requestMatchers("/vqr/**").permitAll()

                        .requestMatchers("/api/user/**").hasAnyRole("USER", "STREAMER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
