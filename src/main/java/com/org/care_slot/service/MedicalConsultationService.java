package com.org.care_slot.service;

import com.org.care_slot.entity.User;

public interface MedicalConsultationService {
    byte[] generateConsultationPdf(Long patientId, Long visitId, Long consultationId, User currentUser);
    byte[] generateExaminationPdf(Long patientId, Long visitId, User currentUser);
    byte[] generatePrescriptionPdf(Long patientId, Long visitId, Long prescriptionId, User currentUser);
    byte[] generatePrescriptionPdfByVisit(Long patientId, Long visitId, User currentUser);
    byte[] generateClinicalOrderPdf(Long patientId, Long visitId, Long orderId, User currentUser);
    byte[] generateServiceResultPdf(Long patientId, Long visitId, Long resultId, User currentUser);
    byte[] generateInvoicePdf(Long patientId, Long visitId, Long invoiceId, User currentUser);
    byte[] generateVisitSummaryPdf(Long patientId, Long visitId, User currentUser);
}

