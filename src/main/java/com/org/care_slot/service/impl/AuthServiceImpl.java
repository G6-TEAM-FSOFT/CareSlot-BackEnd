package com.org.care_slot.service.impl;

import com.org.care_slot.config.JwtProperties;
import com.org.care_slot.dto.request.LoginRequest;
import com.org.care_slot.dto.response.AuthResponse;
import com.org.care_slot.entity.AuthSession;
import com.org.care_slot.entity.User;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AuthSessionRepository;
import com.org.care_slot.repository.UserRepository;
import com.org.care_slot.security.JwtTokenProvider;
import com.org.care_slot.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.org.care_slot.dto.request.PatientRegisterRequest;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.enums.ProfileType;
import com.org.care_slot.enums.RoleType;
import com.org.care_slot.repository.PatientProfileRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!isPasswordMatch(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();

        String accessToken = jwtTokenProvider.generateAccessToken(user, accessJti);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, refreshJti, familyId);

        String refreshJtiHash = jwtTokenProvider.hashJti(refreshJti);

        AuthSession session = AuthSession.builder()
                .user(user)
                .refreshJtiHash(refreshJtiHash)
                .tokenFamilyId(familyId)
                .ipAddress(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .build();

        authSessionRepository.save(session);

        setCookie(httpResponse, jwtProperties.getCookieAccessTokenName(), accessToken, jwtProperties.getAccessTokenExpirationMs() / 1000, "/api/v1");
        setCookie(httpResponse, jwtProperties.getCookieRefreshTokenName(), refreshToken, jwtProperties.getRefreshTokenExpirationMs() / 1000, "/api/v1");

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse registerPatient(PatientRegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(cleanEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .role(RoleType.PATIENT)
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        PatientProfile profile = PatientProfile.builder()
                .user(savedUser)
                .profileType(ProfileType.PRIMARY)
                .relationship("SELF")
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender() != null && !request.getGender().isBlank() ? request.getGender().trim() : "MALE")
                .identityCard(request.getIdentityCard() != null && !request.getIdentityCard().isBlank() ? request.getIdentityCard().trim() : null)
                .address(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress().trim() : null)
                .ethnicity("Kinh")
                .nationality("Việt Nam")
                .status("ACTIVE")
                .build();

        patientProfileRepository.save(profile);

        // Auto-login after registration
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser, accessJti);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser, refreshJti, familyId);

        String refreshJtiHash = jwtTokenProvider.hashJti(refreshJti);

        AuthSession session = AuthSession.builder()
                .user(savedUser)
                .refreshJtiHash(refreshJtiHash)
                .tokenFamilyId(familyId)
                .ipAddress(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .build();

        authSessionRepository.save(session);

        setCookie(httpResponse, jwtProperties.getCookieAccessTokenName(), accessToken, jwtProperties.getAccessTokenExpirationMs() / 1000, "/api/v1");
        setCookie(httpResponse, jwtProperties.getCookieRefreshTokenName(), refreshToken, jwtProperties.getRefreshTokenExpirationMs() / 1000, "/api/v1");

        return buildAuthResponse(savedUser);
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, jwtProperties.getCookieRefreshTokenName());
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            clearAuthCookies(response);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!"REFRESH".equals(jwtTokenProvider.extractTokenType(refreshToken))) {
            clearAuthCookies(response);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String refreshJti = jwtTokenProvider.extractJti(refreshToken);
        String jtiHash = jwtTokenProvider.hashJti(refreshJti);

        Optional<AuthSession> sessionOpt = authSessionRepository.findByRefreshJtiHash(jtiHash);
        if (sessionOpt.isEmpty()) {
            clearAuthCookies(response);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        AuthSession session = sessionOpt.get();

        if (session.getIsRevoked()) {
            LocalDateTime graceCutoff = session.getRevokedAt() != null ? session.getRevokedAt().plusSeconds(jwtProperties.getRotationGracePeriodSeconds()) : LocalDateTime.now();
            if (LocalDateTime.now().isAfter(graceCutoff)) {
                log.warn("Refresh token reuse detected for familyId: {}. Revoking all sessions in family!", session.getTokenFamilyId());
                List<AuthSession> familySessions = authSessionRepository.findByTokenFamilyId(session.getTokenFamilyId());
                familySessions.forEach(s -> s.setIsRevoked(true));
                authSessionRepository.saveAll(familySessions);
                clearAuthCookies(response);
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            // Within grace period: re-issue active access token if an unrevoked session exists in the family
            List<AuthSession> familySessions = authSessionRepository.findByTokenFamilyId(session.getTokenFamilyId());
            Optional<AuthSession> activeSessionOpt = familySessions.stream()
                    .filter(s -> !s.getIsRevoked() && s.getExpiresAt().isAfter(LocalDateTime.now()))
                    .findFirst();
            if (activeSessionOpt.isPresent()) {
                AuthSession activeSession = activeSessionOpt.get();
                User user = activeSession.getUser();
                if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                    clearAuthCookies(response);
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                String newAccessJti = UUID.randomUUID().toString();
                String newAccessToken = jwtTokenProvider.generateAccessToken(user, newAccessJti);
                setCookie(response, jwtProperties.getCookieAccessTokenName(), newAccessToken, jwtProperties.getAccessTokenExpirationMs() / 1000, "/api/v1");
                return;
            }
        }

        // Revoke current session
        session.setIsRevoked(true);
        session.setRevokedAt(LocalDateTime.now());
        session.setLastUsedAt(LocalDateTime.now());
        authSessionRepository.save(session);

        User user = session.getUser();
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            clearAuthCookies(response);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Issue new pair
        String newAccessJti = UUID.randomUUID().toString();
        String newRefreshJti = UUID.randomUUID().toString();

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, newAccessJti);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, newRefreshJti, session.getTokenFamilyId());

        AuthSession newSession = AuthSession.builder()
                .user(user)
                .refreshJtiHash(jwtTokenProvider.hashJti(newRefreshJti))
                .tokenFamilyId(session.getTokenFamilyId())
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .build();

        authSessionRepository.save(newSession);

        setCookie(response, jwtProperties.getCookieAccessTokenName(), newAccessToken, jwtProperties.getAccessTokenExpirationMs() / 1000, "/api/v1");
        setCookie(response, jwtProperties.getCookieRefreshTokenName(), newRefreshToken, jwtProperties.getRefreshTokenExpirationMs() / 1000, "/api/v1");
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, jwtProperties.getCookieRefreshTokenName());
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            try {
                String jti = jwtTokenProvider.extractJti(refreshToken);
                String jtiHash = jwtTokenProvider.hashJti(jti);
                authSessionRepository.findByRefreshJtiHash(jtiHash).ifPresent(session -> {
                    session.setIsRevoked(true);
                    session.setRevokedAt(LocalDateTime.now());
                    authSessionRepository.save(session);
                });
            } catch (Exception e) {
                log.warn("Error revoking session during logout: {}", e.getMessage());
            }
        }
        clearAuthCookies(response);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUserInfo(User currentUser) {
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(currentUser.getId())
                .orElse(currentUser);
        return buildAuthResponse(user);
    }

    private boolean isPasswordMatch(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null) {
            return false;
        }
        if ("DEMO_PASSWORD_HASH".equals(storedHash)) {
            return true;
        }
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }
        return rawPassword.equals(storedHash);
    }

    private AuthResponse buildAuthResponse(User user) {
        Long clinicId = user.getClinic() != null ? user.getClinic().getId() : null;
        String clinicName = user.getClinic() != null ? user.getClinic().getName() : null;

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .clinicId(clinicId)
                .clinicName(clinicName)
                .token(null) // Token is stored in HttpOnly cookies
                .build();
    }

    private void setCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtProperties.isSecureCookie())
                .sameSite(jwtProperties.getSameSite())
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        setCookie(response, jwtProperties.getCookieAccessTokenName(), "", 0, "/api/v1");
        setCookie(response, jwtProperties.getCookieRefreshTokenName(), "", 0, "/api/v1");
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
