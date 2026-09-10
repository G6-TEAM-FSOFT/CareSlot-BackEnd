package com.org.care_slot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ChatRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    public boolean isAllowed(String identifier) {
        String key = "rate_limit:chat:" + identifier;
        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, WINDOW_DURATION);
        }

        return currentCount != null && currentCount <= MAX_REQUESTS_PER_MINUTE;
    }
}
