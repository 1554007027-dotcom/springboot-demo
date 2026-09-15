package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveUser_whenUsernameAndEmailUnique() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@x.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("ENCODED_PASSWORD");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register("newuser", "new@x.com", "123456");

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@x.com");
        assertThat(result.getPassword()).isEqualTo("ENCODED_PASSWORD");  // 密码被加密
        assertThat(result.getRole()).isEqualTo("USER");                    // 默认角色

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        when(userRepository.existsByUsername("exists")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("exists", "e@x.com", "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名已存在");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("exists@x.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("newuser", "exists@x.com", "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("邮箱已存在");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldEncodePassword() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@x.com")).thenReturn(false);
        when(passwordEncoder.encode("rawpass")).thenReturn("$2a$10$xxxxx");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register("newuser", "new@x.com", "rawpass");

        // 密码不应该是明文
        assertThat(result.getPassword()).isNotEqualTo("rawpass");
        assertThat(result.getPassword()).startsWith("$2a$");  // BCrypt 格式
    }
}