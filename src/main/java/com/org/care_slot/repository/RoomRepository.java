package com.org.care_slot.repository;

import com.org.care_slot.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByClinicId(Long clinicId);
    List<Room> findByDepartmentId(Long departmentId);
}
