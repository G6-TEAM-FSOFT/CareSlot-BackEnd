package com.org.care_slot.dto.outpatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDetailResponse {
    private Long id;
    private String visitCode;
    private Long appointmentId;
    private String bookingCode;
    private Long patientProfileId;
    private String patientName;
    private String patientPhone;
    private String patientGender;
    private String patientDob;
    private Long clinicId;
    private String clinicName;
    private Long primaryDoctorId;
    private String primaryDoctorName;
    private String status;
    private LocalDateTime checkedInAt;
    private LocalDateTime completedAt;

    private List<EncounterDto> encounters;
    private List<VitalSignDto> vitalSigns;
    private List<ClinicalNoteDto> clinicalNotes;
    private List<ClinicalOrderDto> clinicalOrders;
    private List<InvoiceDto> invoices;
    private PrescriptionDto prescription;
    private DispositionDto disposition;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncounterDto {
        private Long id;
        private Long visitId;
        private String visitCode;
        private String patientName;
        private String patientPhone;
        private String patientGender;
        private String patientDob;
        private String bookingCode;
        private String encounterType;
        private Long roomId;
        private String roomNumber;
        private String roomName;
        private Long doctorId;
        private String doctorName;
        private String queueNumber;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalSignDto {
        private Long id;
        private Long encounterId;
        private BigDecimal heightCm;
        private BigDecimal weightKg;
        private BigDecimal temperatureC;
        private Integer heartRateBpm;
        private Integer respiratoryRate;
        private Integer systolicBp;
        private Integer diastolicBp;
        private BigDecimal spo2;
        private String recordedByName;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClinicalNoteDto {
        private Long id;
        private Long encounterId;
        private String enteredByName;
        private String clinicalAuthorName;
        private String status;
        private String formData;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClinicalOrderDto {
        private Long id;
        private String orderCode;
        private Integer orderRound;
        private String orderedByName;
        private String status;
        private LocalDateTime createdAt;
        private List<ServiceRequestDto> serviceRequests;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceRequestDto {
        private Long id;
        private Long visitId;
        private Long patientProfileId;
        private String visitCode;
        private String bookingCode;
        private String patientName;
        private String patientPhone;
        private String patientGender;
        private String patientDob;
        private String orderedByName;
        private Long serviceId;
        private String serviceCode;
        private String serviceName;
        private String serviceType;
        private BigDecimal price;
        private String status;
        private LocalDateTime createdAt;
        private ServiceTaskDto task;
        private ServiceResultDto result;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceTaskDto {
        private Long id;
        private Long roomId;
        private String roomNumber;
        private String roomName;
        private String queueNumber;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceResultDto {
        private Long id;
        private String enteredByName;
        private String status;
        private String findings;
        private String conclusion;
        private String resultData;
        private LocalDateTime finalizedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDto {
        private Long id;
        private String invoiceCode;
        private String invoiceType;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionDto {
        private Long id;
        private String prescriptionCode;
        private String prescribedByName;
        private String diagnosisNote;
        private BigDecimal totalEstimatedCost;
        private List<PrescriptionItemDto> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItemDto {
        private Long id;
        private String drugName;
        private String dosage;
        private String usageInstruction;
        private Integer quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispositionDto {
        private Long id;
        private String dispositionType;
        private String notes;
        private String destinationFacility;
        private String destinationDepartment;
        private String createdByName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceCatalogDto {
        private Long id;
        private String code;
        private String name;
        private String serviceType;
        private BigDecimal price;
        private String paymentPolicy;
        private String status;
        private Long defaultRoomId;
        private String defaultRoomName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDto {
        private Long id;
        private String roomNumber;
        private String name;
        private String roomType;
        private String status;
        private Long departmentId;
        private String departmentName;
    }
}


