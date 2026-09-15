package com.org.care_slot.service;

import com.org.care_slot.dto.request.RoomCreateRequest;
import com.org.care_slot.dto.request.RoomUpdateRequest;
import com.org.care_slot.dto.response.DepartmentResponse;
import com.org.care_slot.dto.response.PartnerRoomResponse;

import java.util.List;

public interface PartnerRoomService {
    List<PartnerRoomResponse> getClinicRooms(Long clinicId);
    PartnerRoomResponse getRoomDetail(Long clinicId, Long roomId);
    PartnerRoomResponse createRoom(Long clinicId, RoomCreateRequest request);
    PartnerRoomResponse updateRoom(Long clinicId, Long roomId, RoomUpdateRequest request);
    void deleteRoom(Long clinicId, Long roomId);
    List<DepartmentResponse> getClinicDepartments(Long clinicId);
}
