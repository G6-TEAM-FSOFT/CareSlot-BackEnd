package com.org.care_slot.repository;

import com.org.care_slot.entity.Room;
import com.org.care_slot.entity.TechnicianRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRoomRepository extends JpaRepository<TechnicianRoom, TechnicianRoom.TechnicianRoomId> {

    @Query("SELECT r FROM Room r JOIN TechnicianRoom tr ON tr.roomId = r.id WHERE tr.userId = :userId")
    List<Room> findRoomsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);
}
