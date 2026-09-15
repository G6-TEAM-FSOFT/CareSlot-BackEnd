package com.org.care_slot.controller;

import com.org.care_slot.dto.request.RoomCreateRequest;
import com.org.care_slot.dto.request.RoomUpdateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.DepartmentResponse;
import com.org.care_slot.dto.response.PartnerRoomResponse;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.security.CurrentUserProvider;
import com.org.care_slot.service.PartnerRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partner")
@RequiredArgsConstructor
public class PartnerRoomController {

    private final PartnerRoomService partnerRoomService;
    private final CurrentUserProvider currentUserProvider;

    private Long getEffectiveClinicId(Long headerClinicId) {
        if (headerClinicId != null) {
            return headerClinicId;
        }
        try {
            return currentUserProvider.getCurrentClinicId();
        } catch (Exception e) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<PartnerRoomResponse>>> getClinicRooms(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        List<PartnerRoomResponse> result = partnerRoomService.getClinicRooms(clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<PartnerRoomResponse>> getRoomDetail(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @PathVariable("id") Long roomId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        PartnerRoomResponse result = partnerRoomService.getRoomDetail(clinicId, roomId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<PartnerRoomResponse>> createRoom(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody RoomCreateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        PartnerRoomResponse result = partnerRoomService.createRoom(clinicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo phòng khám mới thành công", result));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<PartnerRoomResponse>> updateRoom(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @PathVariable("id") Long roomId,
            @RequestBody RoomUpdateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        PartnerRoomResponse result = partnerRoomService.updateRoom(clinicId, roomId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phòng khám thành công", result));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @PathVariable("id") Long roomId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        partnerRoomService.deleteRoom(clinicId, roomId);
        return ResponseEntity.ok(ApiResponse.success("Xóa phòng khám thành công", null));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getClinicDepartments(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        List<DepartmentResponse> result = partnerRoomService.getClinicDepartments(clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
