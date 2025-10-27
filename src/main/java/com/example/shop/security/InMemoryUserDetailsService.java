package com.example.shop.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    public InMemoryUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private static final Map<String, String> RAW_USERS = Map.of(
            "admin", "admin123",
            "user", "user123"
    );

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!RAW_USERS.containsKey(username)) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        return User.withUsername(username)
                .password(passwordEncoder.encode(RAW_USERS.get(username)))
                .roles("USER")
                .build();
    }
}
