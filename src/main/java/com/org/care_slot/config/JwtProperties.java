package com.org.care_slot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret = "9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f";
    private long accessTokenExpirationMs = 900000L; // 15 mins
    private long refreshTokenExpirationMs = 1209600000L; // 14 days
    private String cookieAccessTokenName = "ACCESS_TOKEN";
    private String cookieRefreshTokenName = "REFRESH_TOKEN";
    private boolean secureCookie = false;
    private String sameSite = "Lax";
    private int rotationGracePeriodSeconds = 10;
}
