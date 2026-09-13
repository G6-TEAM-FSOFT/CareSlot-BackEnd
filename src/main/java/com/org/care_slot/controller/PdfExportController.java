package com.org.care_slot.controller;

import com.org.care_slot.entity.User;
import com.org.care_slot.service.MedicalConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "PDF Export API", description = "Các API xuất và in file PDF (Phiếu khám, Biên bản hội chẩn khoa, Đơn thuốc, Chỉ định CLS, Kết quả dịch vụ...)")
public class PdfExportController {

    private final MedicalConsultationService medicalConsultationService;

    @GetMapping("/{patientId}/visits/{visitId}/consultations/{consultationId}/pdf")
    @Operation(summary = "Xuất Trích Biên Bản Hội Chẩn Khoa dạng PDF theo lần khám")
    public ResponseEntity<byte[]> exportConsultationPdf(@PathVariable Long patientId,
                                                         @PathVariable Long visitId,
                                                         @PathVariable Long consultationId,
                                                         @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generateConsultationPdf(patientId, visitId, consultationId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"bien-ban-hoi-chan-" + consultationId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/pdf/examination")
    @Operation(summary = "Xuất Phiếu Khám Bệnh dạng PDF theo lần khám")
    public ResponseEntity<byte[]> exportExaminationPdf(@PathVariable Long patientId,
                                                        @PathVariable Long visitId,
                                                        @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generateExaminationPdf(patientId, visitId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"phieu-kham-" + visitId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/prescriptions/{prescriptionId}/pdf")
    @Operation(summary = "Xuất Phiếu Đơn Thuốc dạng PDF theo lần khám")
    public ResponseEntity<byte[]> exportPrescriptionPdf(@PathVariable Long patientId,
                                                         @PathVariable Long visitId,
                                                         @PathVariable Long prescriptionId,
                                                         @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generatePrescriptionPdf(patientId, visitId, prescriptionId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"don-thuoc-" + prescriptionId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/pdf/prescription")
    @Operation(summary = "Xuất Phiếu Đơn Thuốc dạng PDF theo lần khám (tự động lấy đơn thuốc đợt khám)")
    public ResponseEntity<byte[]> exportPrescriptionPdfByVisit(@PathVariable Long patientId,
                                                               @PathVariable Long visitId,
                                                               @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generatePrescriptionPdfByVisit(patientId, visitId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"don-thuoc-" + visitId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/clinical-orders/{orderId}/pdf")
    @Operation(summary = "Xuất Phiếu Các Dịch Vụ Cần Khám / Chỉ Định CLS dạng PDF theo lần khám")
    public ResponseEntity<byte[]> exportClinicalOrderPdf(@PathVariable Long patientId,
                                                          @PathVariable Long visitId,
                                                          @PathVariable Long orderId,
                                                          @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generateClinicalOrderPdf(patientId, visitId, orderId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"phieu-chi-dinh-" + orderId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/service-results/{resultId}/pdf")
    @Operation(summary = "Xuất Phiếu Kết Quả Chi Tiết Từng Dịch Vụ Khám dạng PDF theo lần khám")
    public ResponseEntity<byte[]> exportServiceResultPdf(@PathVariable Long patientId,
                                                          @PathVariable Long visitId,
                                                          @PathVariable Long resultId,
                                                          @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generateServiceResultPdf(patientId, visitId, resultId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ket-qua-dich-vu-" + resultId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/invoices/{invoiceId}/pdf")
    @Operation(summary = "Xuất Hóa Đơn Thanh Toán Dịch Vụ dạng PDF theo lần khám")
    public ResponseEntity<byte[]> exportInvoicePdf(@PathVariable Long patientId,
                                                    @PathVariable Long visitId,
                                                    @PathVariable Long invoiceId,
                                                    @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generateInvoicePdf(patientId, visitId, invoiceId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"hoa-don-" + invoiceId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{patientId}/visits/{visitId}/pdf/summary")
    @Operation(summary = "Xuất Phiếu Tổng Hợp Kết Quả Toàn Bộ Lượt Khám dạng PDF")
    public ResponseEntity<byte[]> exportSummaryPdf(@PathVariable Long patientId,
                                                    @PathVariable Long visitId,
                                                    @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = medicalConsultationService.generateVisitSummaryPdf(patientId, visitId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tong-hop-luot-kham-" + visitId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}

