package com.winnerx0.calvera.users.internal;

import com.winnerx0.calvera.auth.TokenPair;
import com.winnerx0.calvera.auth.TokenService;
import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.users.UpdateUserRequest;
import com.winnerx0.calvera.users.UserService;
import com.winnerx0.calvera.users.UserView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final GithubConnectionService githubConnectionService;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Override
    @Transactional
    public TokenPair findOrCreate(String username, String email, String picture, String githubAccessToken) {

        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email != null ? email : username);
            newUser.setPicture(picture);
            newUser.setRole(Role.USER);
            newUser.setJoinedAt(LocalDateTime.now());
            log.info("Created new user {}", username);
            return userRepository.save(newUser);
        });

        // cache github token
//        redisTemplate.opsForValue().setIfAbsent("github:access:" + user.getId(), githubAccessToken, Duration.of(24L, ChronoUnit.HOURS));

        githubConnectionService.saveOrUpdate(user.getId(), githubAccessToken);

        return tokenService.createTokenPair(user.getId());
    }

    @Override
    public Optional<UserView> findById(Long id) {
        return userRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional
    public Optional<UserView> updateUsername(Long id, String username) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(username);
            return toView(userRepository.save(user));
        });
    }

    private UserView toView(User user) {
        return new UserView(user.getId(), user.getUsername(), user.getEmail(), user.getPicture(), user.getJoinedAt());
    }
}
