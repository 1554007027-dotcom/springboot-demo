package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void register_shouldReturn201() throws Exception {
        User saved = User.builder()
                .id(1L).username("newuser").email("new@x.com").role("USER").build();
        when(authService.register(any(), any(), any())).thenReturn(saved);

        Map<String, String> body = Map.of(
                "username", "newuser",
                "email", "new@x.com",
                "password", "123456"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("new@x.com"))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void login_shouldReturnBothTokens() throws Exception {
        User user = User.builder()
                .id(1L).username("admin").password("ENCODED").role("ADMIN").build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "ENCODED")).thenReturn(true);
        when(jwtUtil.generateAccessToken("admin", "ADMIN")).thenReturn("fake.access.token");
        when(jwtUtil.generateRefreshToken("admin", "ADMIN")).thenReturn("fake.refresh.token");

        Map<String, String> body = Map.of(
                "username", "admin",
                "password", "123456"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.accessToken").value("fake.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("fake.refresh.token"))
                .andExpect(jsonPath("$.data.expiresIn").value(1800));
    }

    @Test
    void login_shouldReturn400_whenUserNotFound() throws Exception {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        Map<String, String> body = Map.of(
                "username", "nobody",
                "password", "123456"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void login_shouldReturn400_whenPasswordWrong() throws Exception {
        User user = User.builder()
                .id(1L).username("admin").password("ENCODED").role("ADMIN").build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "ENCODED")).thenReturn(false);

        Map<String, String> body = Map.of(
                "username", "admin",
                "password", "wrong"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void refresh_shouldReturnNewTokens() throws Exception {
        when(jwtUtil.validateToken("valid.refresh.token", "refresh")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid.refresh.token")).thenReturn("admin");
        when(jwtUtil.getRoleFromToken("valid.refresh.token")).thenReturn("ADMIN");
        when(jwtUtil.generateAccessToken("admin", "ADMIN")).thenReturn("new.access.token");
        when(jwtUtil.generateRefreshToken("admin", "ADMIN")).thenReturn("new.refresh.token");

        Map<String, String> body = Map.of("refreshToken", "valid.refresh.token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new.refresh.token"));
    }

    @Test
    void refresh_shouldReturn400_whenInvalid() throws Exception {
        when(jwtUtil.validateToken("invalid.token", "refresh")).thenReturn(false);

        Map<String, String> body = Map.of("refreshToken", "invalid.token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("refreshToken 无效或已过期"));
    }
}