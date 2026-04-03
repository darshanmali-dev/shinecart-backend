package com.college.shinecart.config;

import com.college.shinecart.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Authentication
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public endpoints - Products
                        .requestMatchers(HttpMethod.GET,"/api/products", "/api/products/**").permitAll()

                        // Public endpoints - Test and Uploads
                        .requestMatchers("/test-mail", "/uploads/**").permitAll()
                                .requestMatchers("/error").permitAll()

                                // Public endpoints - Stores
                        .requestMatchers("/api/stores/**").permitAll()
                        .requestMatchers("/api/stores").permitAll()
                                .requestMatchers("/api/chatbot/**").permitAll()
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()

                        // WebSocket endpoints - MUST be public
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        .requestMatchers("/topic/**").permitAll()

                        // Public endpoints - Auction viewing
                        .requestMatchers(HttpMethod.GET, "/api/auctions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bids/auction/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bids/auction/*/winning").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/cart").permitAll()
                                .requestMatchers("/actuator/health").permitAll()
                                .requestMatchers("/ping").permitAll()


                        .requestMatchers("/api/payments/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/orders/track/*").permitAll()

                                // Admin-only endpoints - Auctions
                                .requestMatchers(HttpMethod.POST, "/api/admin/auctions").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST,"/api/products", "/api/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/api/products", "/api/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/api/products", "/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/auctions/**").hasRole("ADMIN")

                        // Admin-only endpoints - Other admin routes
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
// Public endpoints - Orders (order tracking by order number - must come BEFORE protected routes)
                                 // if you have tracking

// Protected endpoints - Orders (must come BEFORE the wildcard)
                                .requestMatchers("/api/admin/users/**").authenticated()
                                .requestMatchers("/api/orders/my-orders").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/api/orders/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}