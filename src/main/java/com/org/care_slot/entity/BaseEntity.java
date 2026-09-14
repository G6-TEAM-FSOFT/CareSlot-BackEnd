package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEntity {

    private static final java.time.ZoneId VN_ZONE = java.time.ZoneId.of("Asia/Ho_Chi_Minh");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(VN_ZONE);
        }
        this.updatedAt = LocalDateTime.now(VN_ZONE);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(VN_ZONE);
    }
}
