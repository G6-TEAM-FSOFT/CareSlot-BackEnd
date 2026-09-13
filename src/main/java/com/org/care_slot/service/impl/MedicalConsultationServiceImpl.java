package com.org.care_slot.service.impl;

import com.org.care_slot.dto.pdf.ClinicalOrderPdfDto;
import com.org.care_slot.dto.pdf.ConsultationPdfDto;
import com.org.care_slot.dto.pdf.ExaminationPdfDto;
import com.org.care_slot.dto.pdf.InvoicePdfDto;
import com.org.care_slot.dto.pdf.PrescriptionPdfDto;
import com.org.care_slot.dto.pdf.ServiceResultPdfDto;
import com.org.care_slot.dto.pdf.VisitSummaryPdfDto;
import com.org.care_slot.entity.*;
import com.org.care_slot.enums.RoleType;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.*;
import com.org.care_slot.service.MedicalConsultationService;
import com.org.care_slot.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicalConsultationServiceImpl implements MedicalConsultationService {

    private final MedicalConsultationRepository medicalConsultationRepository;
    private final VisitRepository visitRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ClinicalOrderRepository clinicalOrderRepository;
    private final ServiceResultRepository serviceResultRepository;
    private final InvoiceRepository invoiceRepository;
    private final VitalSignRepository vitalSignRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final PdfService pdfService;

    private Visit resolveVisit(Long visitOrAppointmentId, Long patientId) {
        Visit visit = visitRepository.findById(visitOrAppointmentId)
                .orElseGet(() -> visitRepository.findByAppointmentId(visitOrAppointmentId)
                        .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)));

        if (!visit.getPatientProfile().getId().equals(patientId) &&
                (visit.getPatientProfile().getUser() == null || !visit.getPatientProfile().getUser().getId().equals(patientId))) {
            throw new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND);
        }

        return visit;
    }

    @Override
    public byte[] generateConsultationPdf(Long patientId, Long visitId, Long consultationId, User currentUser) {
        Visit visit = resolveVisit(visitId, patientId);
        validateAccessPermission(visit.getPatientProfile(), currentUser);

        MedicalConsultation consultation = medicalConsultationRepository
                .findByIdAndPatientIdAndVisitId(consultationId, patientId, visit.getId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        ConsultationPdfDto dto = ConsultationPdfDto.builder()
                .consultationId(consultation.getId())
                .patientId(patientId)
                .visitId(visit.getId())
                .code("27/BV2")
                .medicalRecordNumber(consultation.getMedicalRecordNumber() != null ? consultation.getMedicalRecordNumber() : "HS-" + visit.getId())
                .hospitalAdmissionNumber(consultation.getHospitalAdmissionNumber() != null ? consultation.getHospitalAdmissionNumber() : "VV-" + visit.getId())
                .patientName(consultation.getPatientNameSnapshot())
                .patientAge(consultation.getPatientAge())
                .gender(consultation.getPatientGender())
                .treatedFromDate(consultation.getTreatedFromDate() != null ? consultation.getTreatedFromDate() : LocalDate.now())
                .treatedToDate(consultation.getTreatedToDate() != null ? consultation.getTreatedToDate() : LocalDate.now())
                .bedNumber(consultation.getBedNumber() != null ? consultation.getBedNumber() : "-")
                .roomNumber(consultation.getRoomNumber() != null ? consultation.getRoomNumber() : "-")
                .departmentName(consultation.getDepartmentName() != null ? consultation.getDepartmentName() : "Khoa Khám bệnh")
                .diagnosisText(consultation.getDiagnosisText())
                .consultationTime(consultation.getConsultationTime())
                .chairpersonName(consultation.getChairpersonName())
                .secretaryName(consultation.getSecretaryName())
                .participantsText(consultation.getParticipantsText())
                .clinicalSummary(consultation.getClinicalSummary())
                .conclusionText(consultation.getConclusionText())
                .treatmentPlan(consultation.getTreatmentPlan())
                .build();

        return pdfService.generatePdfFromTemplate("consultation-minutes", Map.of("data", dto));
    }

    @Override
    public byte[] generateExaminationPdf(Long patientId, Long visitId, User currentUser) {
        Visit visit = resolveVisit(visitId, patientId);
        validateAccessPermission(visit.getPatientProfile(), currentUser);

        PatientProfile patient = visit.getPatientProfile();
        Appointment appointment = visit.getAppointment();
        int age = calculateAge(patient.getDateOfBirth());

        String genderText = "Nam";
        if ("FEMALE".equalsIgnoreCase(patient.getGender())) genderText = "Nữ";
        else if (patient.getGender() != null && !patient.getGender().isBlank()) genderText = patient.getGender();

        String statusText = "Đang khám";
        if ("COMPLETED".equalsIgnoreCase(visit.getStatus())) statusText = "Đã hoàn tất (COMPLETED)";
        else if ("CANCELLED".equalsIgnoreCase(visit.getStatus())) statusText = "Đã hủy (CANCELLED)";
        else if ("CHECKED_IN".equalsIgnoreCase(visit.getStatus())) statusText = "Đã Check-in";

        String roomName = "-";
        if (appointment != null && appointment.getSlot() != null && appointment.getSlot().getRoom() != null) {
            roomName = appointment.getSlot().getRoom().getName() + " (Phòng " + appointment.getSlot().getRoom().getRoomNumber() + ")";
        }

        String specialtyName = "-";
        if (appointment != null && appointment.getSlot() != null && appointment.getSlot().getDoctor() != null 
                && appointment.getSlot().getDoctor().getSpecialty() != null) {
            specialtyName = appointment.getSlot().getDoctor().getSpecialty().getName();
        }

        String timeSlot = "-";
        String appointmentDateText = "-";
        if (appointment != null && appointment.getSlot() != null) {
            if (appointment.getSlot().getStartTime() != null && appointment.getSlot().getEndTime() != null) {
                timeSlot = appointment.getSlot().getStartTime() + " - " + appointment.getSlot().getEndTime();
            }
            if (appointment.getSlot().getAppointmentDate() != null) {
                appointmentDateText = appointment.getSlot().getAppointmentDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        }

        String symptom = "Khám sức khỏe tổng quát";
        if (appointment != null && appointment.getSymptomNote() != null && !appointment.getSymptomNote().isBlank()) {
            symptom = appointment.getSymptomNote();
        }

        ExaminationPdfDto dto = ExaminationPdfDto.builder()
                .visitId(visit.getId())
                .patientId(patient.getId())
                .patientCode("PAT-" + String.format("%04d", patient.getId()))
                .visitCode(visit.getVisitCode() != null ? visit.getVisitCode() : "VIS-" + visit.getId())
                .bookingCode(appointment != null && appointment.getBookingCode() != null ? appointment.getBookingCode() : "BK-" + visit.getId())
                .patientName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .age(age)
                .gender(genderText)
                .phone(patient.getPhone() != null ? patient.getPhone() : "-")
                .identityCard(patient.getIdentityCard() != null && !patient.getIdentityCard().isBlank() ? patient.getIdentityCard() : "Chưa cập nhật")
                .healthInsuranceCode("Chưa cập nhật")
                .ethnicity(patient.getEthnicity() != null ? patient.getEthnicity() : "Kinh")
                .nationality(patient.getNationality() != null ? patient.getNationality() : "Việt Nam")
                .occupation(patient.getOccupation() != null && !patient.getOccupation().isBlank() ? patient.getOccupation() : "Tự do")
                .address(patient.getAddress() != null && !patient.getAddress().isBlank() ? patient.getAddress() : "Chưa cập nhật")
                .clinicName(visit.getClinic() != null ? visit.getClinic().getName() : "Phòng khám CareSlot")
                .clinicAddress(visit.getClinic() != null && visit.getClinic().getAddress() != null ? visit.getClinic().getAddress() : "Hệ thống Y tế CareSlot")
                .departmentName(specialtyName != null && !"-".equals(specialtyName) ? "Khoa " + specialtyName : "Khoa Khám bệnh")
                .specialtyName(specialtyName)
                .roomName(roomName)
                .doctorName(visit.getPrimaryDoctor() != null ? visit.getPrimaryDoctor().getFullName() : "BS. Chuyên Khoa")
                .visitDate(visit.getCheckedInAt() != null ? visit.getCheckedInAt() : (visit.getCreatedAt() != null ? visit.getCreatedAt() : java.time.LocalDateTime.now()))
                .appointmentDateText(appointmentDateText)
                .timeSlot(timeSlot)
                .statusText(statusText)
                .consultationFee(appointment != null ? appointment.getConsultationFee() : java.math.BigDecimal.ZERO)
                .chiefComplaint(symptom)
                .symptomNote(symptom)
                .vitalSignsText("Mạch: 80 lần/phút, Huyết áp: 120/80 mmHg, Thân nhiệt: 36.8°C, SpO2: 98%")
                .diagnosisText("Khám sức khỏe bình thường, không ghi nhận bất thường nặng")
                .treatmentPlan("Tái khám theo hẹn hoặc khi có triệu chứng bất thường")
                .advice("Chế độ ăn uống sinh hoạt lành mạnh, giữ gìn sức khỏe")
                .followUpDate("30 ngày sau")
                .build();

        return pdfService.generatePdfFromTemplate("examination-report", Map.of("data", dto));
    }

    @Override
    public byte[] generatePrescriptionPdfByVisit(Long patientId, Long visitId, User currentUser) {
        List<Prescription> prescriptionList = prescriptionRepository.findByVisitId(visitId);
        if (prescriptionList.isEmpty()) {
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        Prescription prescription = prescriptionList.get(0);
        return generatePrescriptionPdf(patientId, visitId, prescription.getId(), currentUser);
    }

    @Override
    public byte[] generatePrescriptionPdf(Long patientId, Long visitId, Long prescriptionId, User currentUser) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        Visit visit = prescription.getVisit();
        validateAccessPermission(visit.getPatientProfile(), currentUser);

        PatientProfile patient = visit.getPatientProfile();
        int age = calculateAge(patient.getDateOfBirth());

        List<PrescriptionPdfDto.PrescriptionItemPdfDto> itemDtos = new ArrayList<>();
        if (prescription.getItems() != null) {
            int idx = 1;
            for (PrescriptionItem item : prescription.getItems()) {
                itemDtos.add(PrescriptionPdfDto.PrescriptionItemPdfDto.builder()
                        .index(idx++)
                        .drugName(item.getDrugName())
                        .dosage(item.getDosage())
                        .usageInstruction(item.getUsageInstruction())
                        .quantity(item.getQuantity())
                        .unit(item.getUnit())
                        .unitPrice(item.getUnitPrice())
                        .amount(item.getAmount())
                        .build());
            }
        }

        PrescriptionPdfDto dto = PrescriptionPdfDto.builder()
                .prescriptionId(prescription.getId())
                .patientId(patient.getId())
                .visitId(visit.getId())
                .prescriptionCode(prescription.getPrescriptionCode())
                .patientName(patient.getFullName())
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender() : "MALE")
                .address(patient.getAddress() != null ? patient.getAddress() : "-")
                .clinicName(visit.getClinic() != null ? visit.getClinic().getName() : "Phòng khám CareSlot")
                .doctorName(prescription.getPrescribedBy() != null ? prescription.getPrescribedBy().getFullName() : "BS. Đã kê đơn")
                .diagnosisNote(prescription.getDiagnosisNote())
                .createdDate(prescription.getCreatedAt() != null ? prescription.getCreatedAt() : java.time.LocalDateTime.now())
                .totalEstimatedCost(prescription.getTotalEstimatedCost())
                .items(itemDtos)
                .build();

        return pdfService.generatePdfFromTemplate("prescription", Map.of("data", dto));
    }

    @Override
    public byte[] generateClinicalOrderPdf(Long patientId, Long visitId, Long orderId, User currentUser) {
        ClinicalOrder order = clinicalOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        Visit visit = order.getVisit();
        validateAccessPermission(visit.getPatientProfile(), currentUser);

        PatientProfile patient = visit.getPatientProfile();
        int age = calculateAge(patient.getDateOfBirth());

        List<ClinicalOrderPdfDto.ServiceRequestPdfItemDto> serviceDtos = new ArrayList<>();
        if (order.getServiceRequests() != null) {
            int idx = 1;
            for (ServiceRequest req : order.getServiceRequests()) {
                ServiceCatalog cat = req.getServiceCatalog();
                serviceDtos.add(ClinicalOrderPdfDto.ServiceRequestPdfItemDto.builder()
                        .index(idx++)
                        .serviceCode(cat != null ? cat.getCode() : "DV-" + req.getId())
                        .serviceName(cat != null ? cat.getName() : "Dịch vụ chỉ định")
                        .category(cat != null ? cat.getServiceType() : "CLS")
                        .note("-")
                        .status(req.getStatus())
                        .build());
            }
        }

        ClinicalOrderPdfDto dto = ClinicalOrderPdfDto.builder()
                .orderId(order.getId())
                .patientId(patient.getId())
                .visitId(visit.getId())
                .orderCode(order.getOrderCode())
                .orderRound(order.getOrderRound())
                .patientName(patient.getFullName())
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender() : "MALE")
                .phone(patient.getPhone())
                .address(patient.getAddress() != null ? patient.getAddress() : "-")
                .clinicName(visit.getClinic() != null ? visit.getClinic().getName() : "Phòng khám CareSlot")
                .doctorName(order.getOrderedBy() != null ? order.getOrderedBy().getFullName() : "BS. Chỉ định")
                .createdDate(order.getCreatedAt() != null ? order.getCreatedAt() : java.time.LocalDateTime.now())
                .services(serviceDtos)
                .build();

        return pdfService.generatePdfFromTemplate("clinical-order", Map.of("data", dto));
    }

    @Override
    public byte[] generateServiceResultPdf(Long patientId, Long visitId, Long resultId, User currentUser) {
        ServiceResult result = serviceResultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        ServiceRequest req = result.getServiceRequest();
        if (req == null || req.getVisit() == null) {
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }

        Visit visit = req.getVisit();
        validateAccessPermission(visit.getPatientProfile(), currentUser);

        PatientProfile patient = visit.getPatientProfile();
        int age = calculateAge(patient.getDateOfBirth());

        ServiceCatalog catalog = req.getServiceCatalog();

        ServiceResultPdfDto dto = ServiceResultPdfDto.builder()
                .resultId(result.getId())
                .patientId(patient.getId())
                .visitId(visit.getId())
                .serviceCode(catalog != null ? catalog.getCode() : "DV-" + req.getId())
                .serviceName(catalog != null ? catalog.getName() : "Dịch vụ khám / CLS")
                .category(catalog != null ? catalog.getServiceType() : "KẾT QUẢ KHÁM")
                .patientName(patient.getFullName())
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender() : "MALE")
                .clinicName(visit.getClinic() != null ? visit.getClinic().getName() : "Phòng khám CareSlot")
                .departmentName(catalog != null && catalog.getDepartment() != null ? catalog.getDepartment().getName() : "Khoa Chẩn đoán")
                .orderingDoctorName(visit.getPrimaryDoctor() != null ? visit.getPrimaryDoctor().getFullName() : "BS. Chỉ định")
                .performingStaffName(result.getEnteredBy() != null ? result.getEnteredBy().getFullName() : "KTV / Bác sĩ thực hiện")
                .finalizedAt(result.getFinalizedAt() != null ? result.getFinalizedAt() : java.time.LocalDateTime.now())
                .findings(result.getFindings() != null ? result.getFindings() : "Không ghi nhận bất thường.")
                .conclusion(result.getConclusion() != null ? result.getConclusion() : "Bình thường.")
                .status(result.getStatus())
                .build();

        return pdfService.generatePdfFromTemplate("service-result", Map.of("data", dto));
    }

    @Override
    public byte[] generateInvoicePdf(Long patientId, Long visitId, Long invoiceId, User currentUser) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        Visit visit = invoice.getVisit();
        validateAccessPermission(invoice.getPatientProfile(), currentUser);

        PatientProfile patient = invoice.getPatientProfile();
        int age = calculateAge(patient.getDateOfBirth());

        List<InvoicePdfDto.InvoiceItemPdfDto> itemDtos = new ArrayList<>();
        if (invoice.getItems() != null) {
            int idx = 1;
            for (InvoiceItem item : invoice.getItems()) {
                itemDtos.add(InvoicePdfDto.InvoiceItemPdfDto.builder()
                        .index(idx++)
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .amount(item.getAmount())
                        .build());
            }
        }

        InvoicePdfDto dto = InvoicePdfDto.builder()
                .invoiceId(invoice.getId())
                .patientId(patient.getId())
                .visitId(visit != null ? visit.getId() : 0L)
                .invoiceCode(invoice.getInvoiceCode())
                .invoiceType(invoice.getInvoiceType())
                .patientName(patient.getFullName())
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender() : "MALE")
                .phone(patient.getPhone())
                .address(patient.getAddress() != null ? patient.getAddress() : "-")
                .clinicName(visit != null && visit.getClinic() != null ? visit.getClinic().getName() : "Phòng khám CareSlot")
                .status(invoice.getStatus())
                .paidAt(invoice.getPaidAt())
                .totalAmount(invoice.getTotalAmount())
                .items(itemDtos)
                .build();

        return pdfService.generatePdfFromTemplate("invoice", Map.of("data", dto));
    }

    @Override
    public byte[] generateVisitSummaryPdf(Long patientId, Long visitId, User currentUser) {
        Visit visit = resolveVisit(visitId, patientId);
        validateAccessPermission(visit.getPatientProfile(), currentUser);

        PatientProfile patient = visit.getPatientProfile();
        int age = calculateAge(patient.getDateOfBirth());

        // Vital signs text
        List<VitalSign> vitalSignsList = vitalSignRepository.findByVisitId(visit.getId());
        String vitalSignsText = "Chưa ghi nhận";
        if (vitalSignsList != null && !vitalSignsList.isEmpty()) {
            VitalSign v = vitalSignsList.get(0);
            vitalSignsText = String.format("Mạch: %d bpm, Huyết áp: %d/%d mmHg, Nhiệt độ: %.1f°C, SpO2: %.1f%%",
                    v.getHeartRateBpm() != null ? v.getHeartRateBpm() : 80,
                    v.getSystolicBp() != null ? v.getSystolicBp() : 120,
                    v.getDiastolicBp() != null ? v.getDiastolicBp() : 80,
                    v.getTemperatureC() != null ? v.getTemperatureC() : 36.8,
                    v.getSpo2() != null ? v.getSpo2() : 98.0);
        }

        // Clinical note & diagnosis text
        List<ClinicalNote> notesList = clinicalNoteRepository.findByVisitId(visit.getId());
        String clinicalNotesText = "Lâm sàng bình thường";
        String diagnosisText = "Chưa kết luận";
        if (notesList != null && !notesList.isEmpty()) {
            ClinicalNote note = notesList.get(0);
            if (note.getFormData() != null) {
                clinicalNotesText = parseClinicalNote(note.getFormData());
            }
        }

        String treatmentPlan = "Tái khám theo lịch hẹn";
        String advice = "Theo dõi sức khỏe định kỳ";
        List<Prescription> prescriptionList = prescriptionRepository.findByVisitId(visit.getId());
        Prescription prescription = prescriptionList != null && !prescriptionList.isEmpty() ? prescriptionList.get(0) : null;
        if (prescription != null && prescription.getDiagnosisNote() != null) {
            diagnosisText = prescription.getDiagnosisNote();
        }

        // Service results
        List<VisitSummaryPdfDto.ServiceSummaryDto> serviceSummaryDtos = new ArrayList<>();
        List<ClinicalOrder> clinicalOrders = clinicalOrderRepository.findByVisitIdOrderByOrderRoundAsc(visit.getId());
        if (clinicalOrders != null) {
            int idx = 1;
            for (ClinicalOrder order : clinicalOrders) {
                if (order.getServiceRequests() != null) {
                    for (ServiceRequest req : order.getServiceRequests()) {
                        if (req.getServiceResult() != null) {
                            ServiceResult res = req.getServiceResult();
                            serviceSummaryDtos.add(VisitSummaryPdfDto.ServiceSummaryDto.builder()
                                    .index(idx++)
                                    .serviceName(req.getServiceCatalog() != null ? req.getServiceCatalog().getName() : "Dịch vụ CLS")
                                    .category(req.getServiceCatalog() != null ? req.getServiceCatalog().getServiceType() : "CLS")
                                    .findings(res.getFindings() != null ? res.getFindings() : "-")
                                    .conclusion(res.getConclusion() != null ? res.getConclusion() : "-")
                                    .build());
                        }
                    }
                }
            }
        }

        // Prescription items
        List<VisitSummaryPdfDto.PrescriptionItemSummaryDto> prescriptionItemDtos = new ArrayList<>();
        if (prescription != null && prescription.getItems() != null) {
            int idx = 1;
            for (PrescriptionItem item : prescription.getItems()) {
                prescriptionItemDtos.add(VisitSummaryPdfDto.PrescriptionItemSummaryDto.builder()
                        .index(idx++)
                        .drugName(item.getDrugName())
                        .dosage(item.getDosage())
                        .usageInstruction(item.getUsageInstruction())
                        .quantity(item.getQuantity())
                        .unit(item.getUnit())
                        .build());
            }
        }

        VisitSummaryPdfDto dto = VisitSummaryPdfDto.builder()
                .visitId(visit.getId())
                .patientId(patient.getId())
                .visitCode(visit.getVisitCode())
                .patientName(patient.getFullName())
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender() : "MALE")
                .phone(patient.getPhone())
                .address(patient.getAddress() != null ? patient.getAddress() : "-")
                .clinicName(visit.getClinic() != null ? visit.getClinic().getName() : "Phòng khám CareSlot")
                .departmentName("Khoa Khám Bệnh")
                .doctorName(visit.getPrimaryDoctor() != null ? visit.getPrimaryDoctor().getFullName() : "BS. Trưởng Khoa")
                .checkedInAt(visit.getCheckedInAt())
                .completedAt(visit.getCompletedAt())
                .vitalSignsText(vitalSignsText)
                .clinicalNotesText(clinicalNotesText)
                .diagnosisText(diagnosisText)
                .treatmentPlan(treatmentPlan)
                .advice(advice)
                .serviceResults(serviceSummaryDtos)
                .prescriptionItems(prescriptionItemDtos)
                .build();

        return pdfService.generatePdfFromTemplate("visit-summary", Map.of("data", dto));
    }

    private void validateAccessPermission(PatientProfile patient, User currentUser) {
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (RoleType.PATIENT.equals(currentUser.getRole())) {
            boolean isOwner = (patient.getUser() != null && patient.getUser().getId().equals(currentUser.getId()));
            if (!isOwner) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
    }

    private int calculateAge(LocalDate dob) {
        if (dob == null) return 30;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private String parseClinicalNote(String formData) {
        if (formData == null || formData.isBlank()) return "Lâm sàng bình thường";
        String trimmed = formData.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(trimmed);
                if (node.has("note")) return node.get("note").asText();
                if (node.has("clinicalNote")) return node.get("clinicalNote").asText();
                if (node.has("text")) return node.get("text").asText();
            } catch (Exception ignored) {
            }
        }
        return formData;
    }
}

