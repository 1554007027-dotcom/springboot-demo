package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserService userService;

    // ========== findAll ==========

    @Test
    void findAll_shouldReturnAllUsers() {
        User u1 = User.builder().id(1L).username("zhangsan").email("z@x.com").build();
        User u2 = User.builder().id(2L).username("lisi").email("l@x.com").build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("zhangsan");
        assertThat(result.get(1).getUsername()).isEqualTo("lisi");

        verify(userRepository, times(1)).findAll();
    }

    // ========== findById ==========

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = User.builder().id(1L).username("zhangsan").email("z@x.com").build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result.getUsername()).isEqualTo("zhangsan");
        verify(userRepository, times(1)).findById(1L);
        verify(valueOperations, times(1)).set(eq("user:1"), eq(user), any());
    }

    @Test
    void findById_shouldReturnFromCache_whenCacheHit() {
        User cached = User.builder().id(1L).username("cached").email("c@x.com").build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:1")).thenReturn(cached);

        User result = userService.findById(1L);

        assertThat(result.getUsername()).isEqualTo("cached");
        // 命中缓存时不应该查数据库
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void findById_shouldThrow_whenNotExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:999")).thenReturn(null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    // ========== create ==========

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

    // ========== update ==========

    @Test
    void update_shouldUpdateAndSave() {
        User existing = User.builder().id(1L).username("old").email("old@x.com").build();
        User incoming = User.builder().username("new").email("new@x.com").build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(1L, incoming);

        assertThat(result.getUsername()).isEqualTo("new");
        assertThat(result.getEmail()).isEqualTo("new@x.com");

        // 更新后清缓存
        verify(redisTemplate, times(1)).delete("user:1");
    }

    // ========== delete ==========

    @Test
    void delete_shouldDelete_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
        verify(redisTemplate, times(1)).delete("user:1");
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