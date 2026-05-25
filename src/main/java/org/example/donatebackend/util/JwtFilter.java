package org.example.donatebackend.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        System.out.println("JWT FILTER PATH = " + path);
        System.out.println("AUTH HEADER = " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            try {
                if (jwtUtil.isTokenValid(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);

                    if (role == null || role.isBlank()) {
                        System.out.println("JWT ROLE IS EMPTY");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    role = role.trim();

                    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                    System.out.println("JWT OK USERNAME = " + username);
                    System.out.println("JWT ROLE = " + role);
                    System.out.println("JWT AUTHORITY = " + authority);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority(authority))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("JWT INVALID");
                }
            } catch (Exception e) {
                System.out.println("JWT FILTER ERROR = " + e.getMessage());
            }
        } else {
            System.out.println("NO BEARER TOKEN");
        }

        filterChain.doFilter(request, response);
    }
}