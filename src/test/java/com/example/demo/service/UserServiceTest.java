package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_shouldReturnAllUsers() {
        // 准备：让 mock 返回一个列表
        User u1 = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        User u2 = User.builder().id(2L).username("lisi").email("l@x.com").build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        // 执行
        List<User> result = userService.findAll();

        // 断言
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("zhangsan");
        assertThat(result.get(1).getUsername()).isEqualTo("lisi");

        // 验证：findAll 被调用了一次
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result.getUsername()).isEqualTo("zhangsan");
    }

    @Test
    void findById_shouldThrow_whenNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 断言会抛异常
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void create_shouldSaveUser_whenUsernameAndEmailUnique() {
        User user = User.builder().username("zhangsan").email("z@x.com").build();

        when(userRepository.existsByUsername("zhangsan")).thenReturn(false);
        when(userRepository.existsByEmail("z@x.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.create(user);

        assertThat(result.getUsername()).isEqualTo("zhangsan");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_shouldThrow_whenUsernameExists() {
        User user = User.builder().username("zhangsan").email("z@x.com").build();
        when(userRepository.existsByUsername("zhangsan")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名已存在");

        // 验证不会去保存
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrow_whenEmailExists() {
        User user = User.builder().username("zhangsan").email("z@x.com").build();
        when(userRepository.existsByUsername("zhangsan")).thenReturn(false);
        when(userRepository.existsByEmail("z@x.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("邮箱已存在");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldUpdateAndSave() {
        User existing = User.builder().id(1L).username("old").email("old@x.com").build();
        User incoming = User.builder().username("new").email("new@x.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(1L, incoming);

        assertThat(result.getUsername()).isEqualTo("new");
        assertThat(result.getEmail()).isEqualTo("new@x.com");
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotExists() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository, never()).deleteById(anyLong());
    }
}