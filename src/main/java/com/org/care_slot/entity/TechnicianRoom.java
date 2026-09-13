package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "technician_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TechnicianRoom.TechnicianRoomId.class)
public class TechnicianRoom {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class TechnicianRoomId implements Serializable {
        private Long userId;
        private Long roomId;
    }
}
