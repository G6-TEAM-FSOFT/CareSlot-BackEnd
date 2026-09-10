package com.org.care_slot.controller;

import com.org.care_slot.dto.outpatient.*;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.service.OutpatientWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/outpatient")
@RequiredArgsConstructor
@Tag(name = "Outpatient Clinical Workflow API", description = "Các API quy trình khám ngoại trú khép kín (Check-in, Vital signs, Order CLS, Thanh toán, Nhập KQ KTV, Return Consultation, Kê đơn, Disposition)")
public class OutpatientWorkflowController {

    private final OutpatientWorkflowService outpatientWorkflowService;

    @PostMapping("/receptionist/check-in")
    @Operation(summary = "Lễ tân Check-in lịch hẹn (Tự động khởi tạo Visit và Initial Encounter)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> checkIn(
            @RequestBody CheckInRequest request,
            @RequestParam(defaultValue = "10") Long currentUserId) {
        VisitDetailResponse response = outpatientWorkflowService.checkIn(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công. Đã tạo đợt khám ngoại trú (Visit).", response));
    }

    @PostMapping("/assistant/vital-signs")
    @Operation(summary = "Trợ lý y tế / Điều dưỡng nhập Chỉ số sinh tồn (Vital Signs)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> recordVitalSigns(
            @RequestBody VitalSignRequest request,
            @RequestParam(defaultValue = "11") Long currentUserId) {
        VisitDetailResponse response = outpatientWorkflowService.recordVitalSigns(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Lưu chỉ số sinh tồn thành công", response));
    }

    @PostMapping("/assistant/clinical-notes")
    @Operation(summary = "Trợ lý y tế / Bác sĩ lưu Khám lâm sàng & Bệnh sử draft/final")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> saveClinicalNote(
            @RequestBody ClinicalNoteRequest request,
            @RequestParam(defaultValue = "11") Long currentUserId) {
        VisitDetailResponse response = outpatientWorkflowService.saveClinicalNote(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Lưu thông tin lâm sàng thành công", response));
    }

    @PostMapping("/doctor/clinical-orders")
    @Operation(summary = "Bác sĩ chỉ định các Dịch vụ Cận lâm sàng (Clinical Order Round N)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> createClinicalOrder(
            @RequestBody ClinicalOrderRequest request,
            @RequestParam(defaultValue = "1") Long currentUserId) {
        VisitDetailResponse response = outpatientWorkflowService.createClinicalOrder(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Tạo chỉ định cận lâm sàng thành công. Đã sinh hóa đơn dịch vụ.", response));
    }

    @PostMapping("/receptionist/invoices/{invoiceId}/pay")
    @Operation(summary = "Lễ tân / Thu ngân thu tiền Hóa đơn Cận lâm sàng (Kích hoạt Task READY)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> payInvoice(
            @PathVariable Long invoiceId,
            @RequestParam(defaultValue = "10") Long currentUserId) {
        VisitDetailResponse response = outpatientWorkflowService.payInvoice(invoiceId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Thanh toán thành công. Đã mở các task phòng cận lâm sàng.", response));
    }

    @PostMapping("/technician/results")
    @Operation(summary = "Kỹ thuật viên nhập Kết quả Cận lâm sàng & Bấm FINAL (Tự động kích hoạt lượt quay lại Bác sĩ nếu đủ KQ)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> submitDiagnosticResult(
            @RequestBody SubmitResultRequest request,
            @RequestParam(defaultValue = "12") Long currentUserId) {
        VisitDetailResponse response = outpatientWorkflowService.submitDiagnosticResult(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Nhập kết quả cận lâm sàng thành công", response));
    }

    @PostMapping("/doctor/finalize-visit")
    @Operation(summary = "Bác sĩ Kê đơn thuốc điện tử, Lưu kết cục Disposition và Hoàn tất đợt khám (Visit Completed)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> finalizeVisit(
            @RequestParam Long visitId,
            @RequestBody(required = false) FinalizeVisitWrapper wrapper,
            @RequestParam(defaultValue = "1") Long currentUserId) {
        PrescriptionRequest prescReq = wrapper != null ? wrapper.getPrescription() : null;
        VisitDispositionRequest dispReq = wrapper != null ? wrapper.getDisposition() : null;
        VisitDetailResponse response = outpatientWorkflowService.finalizeVisit(visitId, prescReq, dispReq, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Đã hoàn tất đợt khám ngoại trú (Visit COMPLETED)", response));
    }

    @GetMapping("/visits/{visitId}")
    @Operation(summary = "Tra cứu thông tin chi tiết Đợt khám & Hành trình bệnh nhân (Patient Journey Tracker)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> getVisitDetail(@PathVariable Long visitId) {
        VisitDetailResponse response = outpatientWorkflowService.getVisitDetail(visitId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đợt khám thành công", response));
    }

    @GetMapping("/visits/appointment/{appointmentId}")
    @Operation(summary = "Tra cứu đợt khám theo ID lịch hẹn")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> getVisitByAppointmentId(@PathVariable Long appointmentId) {
        VisitDetailResponse response = outpatientWorkflowService.getVisitDetailByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đợt khám thành công", response));
    }

    @GetMapping("/patient-history/{patientProfileId}")
    @Operation(summary = "Lấy lịch sử các đợt khám bệnh của bệnh nhân theo ID hồ sơ")
    public ResponseEntity<ApiResponse<List<VisitDetailResponse>>> getPatientHistory(@PathVariable Long patientProfileId) {
        List<VisitDetailResponse> history = outpatientWorkflowService.getPatientHistory(patientProfileId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử khám bệnh thành công", history));
    }

    @PostMapping("/encounters/{encounterId}/start")
    @Operation(summary = "Bác sĩ bấm Bắt đầu khám (Chuyển trạng thái Encounter sang IN_PROGRESS)")
    public ResponseEntity<ApiResponse<VisitDetailResponse>> startEncounter(@PathVariable Long encounterId) {
        VisitDetailResponse response = outpatientWorkflowService.startEncounter(encounterId);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu lượt khám thành công (IN_PROGRESS)", response));
    }

    @GetMapping("/queue/encounters")
    @Operation(summary = "Lấy hàng chờ khám bệnh theo Phòng")
    public ResponseEntity<ApiResponse<List<VisitDetailResponse.EncounterDto>>> getEncounterQueueByRoom(
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "WAITING") String status) {
        List<VisitDetailResponse.EncounterDto> queue = outpatientWorkflowService.getEncounterQueueByRoom(roomId, status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hàng chờ thành công", queue));
    }

    @GetMapping("/queue/tasks")
    @Operation(summary = "Lấy hàng chờ phòng Cận lâm sàng theo Phòng")
    public ResponseEntity<ApiResponse<List<VisitDetailResponse.ServiceRequestDto>>> getTaskQueueByRoom(
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "READY") String status) {
        List<VisitDetailResponse.ServiceRequestDto> queue = outpatientWorkflowService.getTaskQueueByRoom(roomId, status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách task cận lâm sàng thành công", queue));
    }

    @GetMapping("/catalog")
    @Operation(summary = "Lấy danh mục dịch vụ cận lâm sàng theo cơ sở")
    public ResponseEntity<ApiResponse<List<VisitDetailResponse.ServiceCatalogDto>>> getCatalog(
            @RequestParam(defaultValue = "1") Long clinicId) {
        List<VisitDetailResponse.ServiceCatalogDto> catalog = outpatientWorkflowService.getCatalog(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục dịch vụ thành công", catalog));
    }

    @GetMapping("/rooms")
    @Operation(summary = "Lấy danh sách phòng khám và cận lâm sàng theo cơ sở")
    public ResponseEntity<ApiResponse<List<VisitDetailResponse.RoomDto>>> getRooms(
            @RequestParam(defaultValue = "1") Long clinicId) {
        List<VisitDetailResponse.RoomDto> rooms = outpatientWorkflowService.getRooms(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phòng thành công", rooms));
    }


    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FinalizeVisitWrapper {
        private PrescriptionRequest prescription;
        private VisitDispositionRequest disposition;
    }
}
