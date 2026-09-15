package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.RoomCreateRequest;
import com.org.care_slot.dto.request.RoomUpdateRequest;
import com.org.care_slot.dto.response.DepartmentResponse;
import com.org.care_slot.dto.response.PartnerRoomResponse;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Department;
import com.org.care_slot.entity.Room;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.ClinicRepository;
import com.org.care_slot.repository.DepartmentRepository;
import com.org.care_slot.repository.RoomRepository;
import com.org.care_slot.service.PartnerRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerRoomServiceImpl implements PartnerRoomService {

    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;
    private final ClinicRepository clinicRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PartnerRoomResponse> getClinicRooms(Long clinicId) {
        List<Room> rooms = roomRepository.findByClinicId(clinicId);
        Map<Long, Map<SlotStatus, Long>> slotStatsMap = getSlotStatsMap(clinicId);

        return rooms.stream()
                .map(room -> mapToPartnerRoomResponse(room, slotStatsMap.getOrDefault(room.getId(), new HashMap<>())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerRoomResponse getRoomDetail(Long clinicId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .filter(r -> r.getClinic().getId().equals(clinicId))
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Map<Long, Map<SlotStatus, Long>> slotStatsMap = getSlotStatsMap(clinicId);
        return mapToPartnerRoomResponse(room, slotStatsMap.getOrDefault(room.getId(), new HashMap<>()));
    }

    @Override
    public PartnerRoomResponse createRoom(Long clinicId, RoomCreateRequest request) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .filter(d -> d.getClinic().getId().equals(clinicId))
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        Room room = Room.builder()
                .clinic(clinic)
                .department(department)
                .roomNumber(request.getRoomNumber())
                .name(request.getName())
                .roomType(request.getRoomType() != null ? request.getRoomType() : "CONSULTATION")
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToPartnerRoomResponse(savedRoom, new HashMap<>());
    }

    @Override
    public PartnerRoomResponse updateRoom(Long clinicId, Long roomId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .filter(r -> r.getClinic().getId().equals(clinicId))
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .filter(d -> d.getClinic().getId().equals(clinicId))
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
            room.setDepartment(department);
        }

        if (request.getRoomNumber() != null && !request.getRoomNumber().isBlank()) {
            room.setRoomNumber(request.getRoomNumber());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            room.setName(request.getName());
        }
        if (request.getRoomType() != null && !request.getRoomType().isBlank()) {
            room.setRoomType(request.getRoomType());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            room.setStatus(request.getStatus());
        }

        Room updatedRoom = roomRepository.save(room);
        Map<Long, Map<SlotStatus, Long>> slotStatsMap = getSlotStatsMap(clinicId);
        return mapToPartnerRoomResponse(updatedRoom, slotStatsMap.getOrDefault(updatedRoom.getId(), new HashMap<>()));
    }

    @Override
    public void deleteRoom(Long clinicId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .filter(r -> r.getClinic().getId().equals(clinicId))
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        roomRepository.delete(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getClinicDepartments(Long clinicId) {
        List<Department> departments = departmentRepository.findByClinicId(clinicId);

        if (departments.isEmpty()) {
            // Auto-create default departments if clinic has none yet
            Clinic clinic = clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

            Department defaultDept1 = Department.builder()
                    .clinic(clinic)
                    .code("KHB")
                    .name("Khoa Khám Bệnh")
                    .deptType("CLINICAL")
                    .status("ACTIVE")
                    .build();

            Department defaultDept2 = Department.builder()
                    .clinic(clinic)
                    .code("XN")
                    .name("Khoa Xét Nghiệm & CĐHA")
                    .deptType("LABORATORY")
                    .status("ACTIVE")
                    .build();

            departments = departmentRepository.saveAll(List.of(defaultDept1, defaultDept2));
        }

        return departments.stream()
                .map(d -> DepartmentResponse.builder()
                        .id(d.getId())
                        .clinicId(d.getClinic().getId())
                        .code(d.getCode())
                        .name(d.getName())
                        .deptType(d.getDeptType())
                        .status(d.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<Long, Map<SlotStatus, Long>> getSlotStatsMap(Long clinicId) {
        List<Object[]> rawCounts = appointmentSlotRepository.countSlotsByRoomAndStatusForClinic(clinicId);
        Map<Long, Map<SlotStatus, Long>> stats = new HashMap<>();

        for (Object[] row : rawCounts) {
            Long roomId = (Long) row[0];
            SlotStatus status = (SlotStatus) row[1];
            Long count = (Long) row[2];

            stats.computeIfAbsent(roomId, k -> new HashMap<>()).put(status, count);
        }
        return stats;
    }

    private PartnerRoomResponse mapToPartnerRoomResponse(Room room, Map<SlotStatus, Long> counts) {
        long available = counts.getOrDefault(SlotStatus.AVAILABLE, 0L);
        long booked = counts.getOrDefault(SlotStatus.BOOKED, 0L);
        long held = counts.getOrDefault(SlotStatus.HELD, 0L);
        long total = available + booked + held;

        return PartnerRoomResponse.builder()
                .id(room.getId())
                .clinicId(room.getClinic().getId())
                .departmentId(room.getDepartment() != null ? room.getDepartment().getId() : null)
                .departmentName(room.getDepartment() != null ? room.getDepartment().getName() : "")
                .roomNumber(room.getRoomNumber())
                .name(room.getName())
                .roomType(room.getRoomType())
                .status(room.getStatus())
                .totalSlots(total)
                .availableSlots(available)
                .bookedSlots(booked)
                .heldSlots(held)
                .build();
    }
}
