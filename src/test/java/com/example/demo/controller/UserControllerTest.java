package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== 原有测试 ==========

    @Test
    void getAllUsers_shouldReturn200() throws Exception {
        User u1 = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        User u2 = User.builder().id(2L).username("lisi").email("l@x.com").build();
        when(userService.findAll()).thenReturn(Arrays.asList(u1, u2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("zhangsan"))
                .andExpect(jsonPath("$[1].username").value("lisi"));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("zhangsan"))
                .andExpect(jsonPath("$.email").value("z@x.com"));
    }

    @Test
    void createUser_shouldReturn201() throws Exception {
        User input = User.builder().username("zhangsan").email("z@x.com").build();
        User saved = User.builder().id(1L).username("zhangsan").email("z@x.com").build();

        when(userService.create(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("zhangsan"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    void createUser_shouldReturn400_whenUsernameBlank() throws Exception {
        User input = User.builder().username("").email("z@x.com").build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(User.class));
    }

    @Test
    void createUser_shouldReturn400_whenEmailInvalid() throws Exception {
        User input = User.builder().username("zhangsan").email("not-an-email").build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(User.class));
    }

    @Test
    void updateUser_shouldReturn200() throws Exception {
        User input = User.builder().username("new").email("new@x.com").build();
        User updated = User.builder().id(1L).username("new").email("new@x.com").build();

        when(userService.update(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("new"))
                .andExpect(jsonPath("$.email").value("new@x.com"));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1L);
    }

    // ========== 分页测试 ==========

    @Test
    void getUsersPaged_shouldReturnPage() throws Exception {
        User u1 = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        User u2 = User.builder().id(2L).username("lisi").email("l@x.com").build();
        Page<User> page = new PageImpl<>(List.of(u1, u2), PageRequest.of(0, 10), 2);

        when(userService.findAllPaged(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/query/paged?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("zhangsan"))
                .andExpect(jsonPath("$.content[1].username").value("lisi"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getUsersPaged_shouldRespectPageParams() throws Exception {
        User u1 = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        Page<User> page = new PageImpl<>(List.of(u1), PageRequest.of(2, 5), 11);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(userService.findAllPaged(captor.capture())).thenReturn(page);

        mockMvc.perform(get("/api/users/query/paged?page=2&size=5"))
                .andExpect(status().isOk());

        Pageable captured = captor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(2);
        assertThat(captured.getPageSize()).isEqualTo(5);
    }
}