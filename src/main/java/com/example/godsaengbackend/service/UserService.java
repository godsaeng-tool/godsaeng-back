package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.UserDto;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    @Transactional
    public UserDto.UserResponse signup(UserDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role("ROLE_USER")
                .isGodMode(false)
                .build();

        User savedUser = userRepository.save(user);
        return UserDto.UserResponse.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserProfile(String email) {
        User user = findByEmail(email);
        return UserDto.UserResponse.fromEntity(user);
    }
    
    @Transactional
    public UserDto.UserResponse updateGodMode(String email, Boolean isGodMode) {
        User user = findByEmail(email);
        user.setIsGodMode(isGodMode);
        User updatedUser = userRepository.save(user);
        return UserDto.UserResponse.fromEntity(updatedUser);
    }
}
