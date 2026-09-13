package com.org.care_slot.repository;

import com.org.care_slot.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByRefreshJtiHash(String refreshJtiHash);
    List<AuthSession> findByTokenFamilyId(String tokenFamilyId);
    void deleteByUserId(Long userId);
}
