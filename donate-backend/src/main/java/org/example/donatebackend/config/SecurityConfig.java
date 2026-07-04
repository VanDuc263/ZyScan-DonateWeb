package org.example.donatebackend.config;

import org.example.donatebackend.util.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                .csrf(csrf -> csrf.disable())                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/payments/create-qr").permitAll()
                        .requestMatchers("/api/payments/*/status").permitAll()
                        .requestMatchers("/api/payments/*/sandbox-simulate").permitAll()
                        .requestMatchers("/api/payments/vietqr/callback").permitAll()

                        .requestMatchers("/api/donate/history/received").hasAuthority("ROLE_STREAMER")
                        .requestMatchers("/api/donate/history", "/api/donate/history/sent").authenticated()
                        .requestMatchers("/api/donate/**").permitAll()
                        .requestMatchers("/api/wallet-transactions/**").permitAll()

                        .requestMatchers("/api/streamers/create").authenticated()
                        .requestMatchers("/api/streamers/me/**").hasAuthority("ROLE_STREAMER")
                        .requestMatchers("/api/streamers/**").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll()
                        .requestMatchers("/api/tts/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/vqr/**").permitAll()

                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/user/**").hasAnyAuthority("ROLE_USER", "ROLE_STREAMER", "ROLE_ADMIN")

                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/metrics/**").permitAll()


                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
