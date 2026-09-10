package com.org.care_slot.service;

import com.org.care_slot.dto.outpatient.*;
import com.org.care_slot.entity.Room;
import com.org.care_slot.entity.ServiceCatalog;

import java.util.List;

public interface OutpatientWorkflowService {
    VisitDetailResponse checkIn(CheckInRequest request, Long currentUserId);
    VisitDetailResponse recordVitalSigns(VitalSignRequest request, Long currentUserId);
    VisitDetailResponse saveClinicalNote(ClinicalNoteRequest request, Long currentUserId);
    VisitDetailResponse createClinicalOrder(ClinicalOrderRequest request, Long currentUserId);
    VisitDetailResponse payInvoice(Long invoiceId, Long currentUserId);
    VisitDetailResponse submitDiagnosticResult(SubmitResultRequest request, Long currentUserId);
    VisitDetailResponse finalizeVisit(Long visitId, PrescriptionRequest prescReq, VisitDispositionRequest dispReq, Long currentUserId);
    VisitDetailResponse getVisitDetail(Long visitId);
    VisitDetailResponse getVisitDetailByAppointmentId(Long appointmentId);
    List<VisitDetailResponse.EncounterDto> getEncounterQueueByRoom(Long roomId, String status);
    List<VisitDetailResponse.ServiceRequestDto> getTaskQueueByRoom(Long roomId, String status);
    List<VisitDetailResponse.ServiceCatalogDto> getCatalog(Long clinicId);
    List<VisitDetailResponse.RoomDto> getRooms(Long clinicId);
    List<VisitDetailResponse> getPatientHistory(Long patientProfileId);
    VisitDetailResponse startEncounter(Long encounterId);
}

