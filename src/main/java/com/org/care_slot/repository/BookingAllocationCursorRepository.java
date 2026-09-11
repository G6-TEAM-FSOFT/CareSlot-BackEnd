package com.org.care_slot.repository;

import com.org.care_slot.entity.BookingAllocationCursor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingAllocationCursorRepository extends JpaRepository<BookingAllocationCursor, Long> {
    Optional<BookingAllocationCursor> findByClinicIdAndSpecialtyId(Long clinicId, Long specialtyId);
}
