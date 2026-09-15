package com.example.demo.repository;

import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 保存并查询用户() {
        User user = User.builder()
                .username("zhangsan")
                .email("zhangsan@example.com")
                .password("123456")
                .role("USER")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("zhangsan");
    }

    @Test
    void 按用户名查找() {
        User user = User.builder()
                .username("lisi")
                .email("lisi@example.com")
                .password("123456")
                .role("USER")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("lisi");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("lisi@example.com");
    }

    @Test
    void 判断用户名是否存在() {
        userRepository.save(User.builder()
                .username("wangwu")
                .email("wangwu@example.com")
                .password("123456")
                .role("USER")
                .build());

        assertThat(userRepository.existsByUsername("wangwu")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void 删除用户() {
        User user = userRepository.save(User.builder()
                .username("temp")
                .email("temp@example.com")
                .password("123456")
                .role("USER")
                .build());

        userRepository.deleteById(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}