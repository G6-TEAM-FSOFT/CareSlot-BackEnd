package com.org.care_slot.repository;

import com.org.care_slot.entity.ServiceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceTaskRepository extends JpaRepository<ServiceTask, Long> {
    Optional<ServiceTask> findByServiceRequestId(Long serviceRequestId);
    List<ServiceTask> findByRoomIdAndStatusOrderByCreatedAtAsc(Long roomId, String status);
    List<ServiceTask> findByRoomIdAndStatusOrderByCreatedAtDesc(Long roomId, String status);
    List<ServiceTask> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<ServiceTask> findByDepartmentIdAndStatus(Long departmentId, String status);
}
