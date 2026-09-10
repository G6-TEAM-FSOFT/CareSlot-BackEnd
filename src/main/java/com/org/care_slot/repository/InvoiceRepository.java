package com.org.care_slot.repository;

import com.org.care_slot.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceCode(String invoiceCode);
    Optional<Invoice> findByAppointmentId(Long appointmentId);
    List<Invoice> findByVisitId(Long visitId);
    List<Invoice> findByPatientProfileId(Long patientProfileId);
    List<Invoice> findByStatus(String status);
}
