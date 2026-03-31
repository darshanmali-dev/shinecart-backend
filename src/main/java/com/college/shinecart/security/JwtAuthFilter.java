package com.college.shinecart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        final String authHeader = req.getHeader("Authorization");
        final String token;
        final String username;

        // 1. Quick check for Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        // 2. Extract token and username
        token = authHeader.substring(7);

        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            // If token is invalid/expired, just let it fail naturally
            // at the Security Filter Chain level
            chain.doFilter(req, res);
            return;
        }

        // 3. Authenticate if username is present and context is empty
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(token, user)) {
                // Cast to your User entity to access the ID
                com.college.shinecart.entity.User appUser = (com.college.shinecart.entity.User) user;

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        appUser,  // This is now your User entity, not just UserDetails
                        null,
                        appUser.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // 4. Update the Security Context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(req, res);
    }
}