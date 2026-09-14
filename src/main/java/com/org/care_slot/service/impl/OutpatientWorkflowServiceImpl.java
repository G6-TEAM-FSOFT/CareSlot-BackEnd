package com.org.care_slot.service.impl;

import com.org.care_slot.dto.outpatient.*;
import com.org.care_slot.entity.*;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.repository.*;
import com.org.care_slot.service.OutpatientWorkflowService;
import com.org.care_slot.service.ReceptionCheckInService;
import com.org.care_slot.dto.response.AppointmentSlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutpatientWorkflowServiceImpl implements OutpatientWorkflowService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final ReceptionCheckInService receptionCheckInService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final RoomRepository roomRepository;
    private final VisitRepository visitRepository;
    private final EncounterRepository encounterRepository;
    private final VitalSignRepository vitalSignRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final ClinicalOrderRepository clinicalOrderRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ServiceTaskRepository serviceTaskRepository;
    private final ServiceResultRepository serviceResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final VisitDispositionRepository visitDispositionRepository;
    private final MedicalRecordTemplateRepository medicalRecordTemplateRepository;
    private final TechnicianRoomRepository technicianRoomRepository;

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public VisitDetailResponse checkIn(CheckInRequest request, Long currentUserId) {
        return getVisitDetail(receptionCheckInService.checkIn(request, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> getReplacementSlots(Long appointmentId, Long currentUserId) {
        return receptionCheckInService.getReplacementSlots(appointmentId, currentUserId);
    }

    @Override
    @Transactional
    public VisitDetailResponse recordVitalSigns(VitalSignRequest request, Long currentUserId) {
        Visit visit = visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new AppException("Visit không tồn tại"));
        Encounter encounter = encounterRepository.findById(request.getEncounterId())
                .orElseThrow(() -> new AppException("Encounter không tồn tại"));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("User không tồn tại"));

        List<VitalSign> existingVitalSigns = vitalSignRepository.findByVisitId(visit.getId());
        VitalSign vitalSign;
        if (!existingVitalSigns.isEmpty()) {
            vitalSign = existingVitalSigns.get(0);
        } else {
            vitalSign = VitalSign.builder()
                    .visit(visit)
                    .build();
        }

        vitalSign.setEncounter(encounter);
        vitalSign.setRecordedBy(user);
        vitalSign.setHeightCm(request.getHeightCm());
        vitalSign.setWeightKg(request.getWeightKg());
        vitalSign.setTemperatureC(request.getTemperatureC());
        vitalSign.setHeartRateBpm(request.getHeartRateBpm());
        vitalSign.setRespiratoryRate(request.getRespiratoryRate());
        vitalSign.setSystolicBp(request.getSystolicBp());
        vitalSign.setDiastolicBp(request.getDiastolicBp());
        vitalSign.setSpo2(request.getSpo2());

        vitalSignRepository.save(vitalSign);

        if ("WAITING".equals(encounter.getStatus())) {
            encounter.setStatus("IN_PROGRESS");
            if (encounter.getStartedAt() == null) {
                encounter.setStartedAt(LocalDateTime.now());
            }
            encounterRepository.save(encounter);
        }

        return getVisitDetail(visit.getId());
    }

    @Override
    @Transactional
    public VisitDetailResponse saveClinicalNote(ClinicalNoteRequest request, Long currentUserId) {
        Visit visit = visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new AppException("Visit không tồn tại"));
        Encounter encounter = encounterRepository.findById(request.getEncounterId())
                .orElseThrow(() -> new AppException("Encounter không tồn tại"));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("User không tồn tại"));

        MedicalRecordTemplate tpl = null;
        if (request.getTemplateId() != null) {
            tpl = medicalRecordTemplateRepository.findById(request.getTemplateId()).orElse(null);
        }

        String rawData = request.getFormData();
        String jsonFormData;
        if (rawData == null || rawData.isBlank()) {
            jsonFormData = "{\"note\":\"\"}";
        } else {
            String trimmed = rawData.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                jsonFormData = trimmed;
            } else {
                try {
                    jsonFormData = "{\"note\":" + new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(trimmed) + "}";
                } catch (Exception e) {
                    jsonFormData = "{\"note\":\"" + trimmed.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
                }
            }
        }

        ClinicalNote note = clinicalNoteRepository.findFirstByEncounterId(encounter.getId()).orElse(null);
        if (note == null) {
            note = ClinicalNote.builder()
                    .visit(visit)
                    .encounter(encounter)
                    .template(tpl)
                    .enteredBy(user)
                    .clinicalAuthor(encounter.getDoctor())
                    .status("DRAFT")
                    .formData(jsonFormData)
                    .build();
        } else {
            note.setFormData(jsonFormData);
            note.setStatus("FINAL");
        }
        clinicalNoteRepository.save(note);

        if ("WAITING".equals(encounter.getStatus())) {
            encounter.setStatus("IN_PROGRESS");
            if (encounter.getStartedAt() == null) {
                encounter.setStartedAt(LocalDateTime.now());
            }
            encounterRepository.save(encounter);
        }

        return getVisitDetail(visit.getId());
    }

    @Override
    @Transactional
    public VisitDetailResponse createClinicalOrder(ClinicalOrderRequest request, Long currentUserId) {
        Visit visit = visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new AppException("Visit không tồn tại"));
        Encounter encounter = encounterRepository.findById(request.getEncounterId())
                .orElseThrow(() -> new AppException("Encounter không tồn tại"));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("User không tồn tại"));

        List<VitalSign> vitalSigns = vitalSignRepository.findByVisitId(visit.getId());
        List<ClinicalNote> clinicalNotes = clinicalNoteRepository.findByVisitId(visit.getId());
        if (vitalSigns.isEmpty() || clinicalNotes.isEmpty()) {
            throw new AppException("Vui lòng hoàn tất và lưu Chỉ số sinh tồn & Bệnh sử trước khi tạo lệnh chỉ định cận lâm sàng!");
        }

        int round = clinicalOrderRepository.findByVisitIdOrderByOrderRoundAsc(visit.getId()).size() + 1;
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderCode = "ORD-" + dateStr + "-V" + visit.getId() + "-" + String.format("%02d", round);

        ClinicalOrder order = ClinicalOrder.builder()
                .orderCode(orderCode)
                .visit(visit)
                .encounter(encounter)
                .orderRound(round)
                .orderedBy(encounter.getDoctor())
                .enteredBy(user)
                .status("ACTIVE")
                .build();
        clinicalOrderRepository.save(order);

        List<ServiceCatalog> services = serviceCatalogRepository.findAllById(request.getServiceIds());
        BigDecimal totalAmount = BigDecimal.ZERO;

        String invCode = "INV-CLS-" + System.currentTimeMillis();
        Invoice invoice = Invoice.builder()
                .invoiceCode(invCode)
                .visit(visit)
                .patientProfile(visit.getPatientProfile())
                .invoiceType("CLINICAL_SERVICE")
                .status("PENDING")
                .build();
        invoiceRepository.save(invoice);

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        int taskNo = 1;
        for (ServiceCatalog svc : services) {
            ServiceRequest sr = ServiceRequest.builder()
                    .clinicalOrder(order)
                    .visit(visit)
                    .serviceCatalog(svc)
                    .status("ORDERED")
                    .build();
            serviceRequestRepository.save(sr);

            totalAmount = totalAmount.add(svc.getPrice());

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .serviceRequest(sr)
                    .itemName(svc.getName())
                    .quantity(1)
                    .unitPrice(svc.getPrice())
                    .amount(svc.getPrice())
                    .build();
            invoiceItems.add(item);

            Room taskRoom = svc.getDefaultRoom();
            if (taskRoom == null) {
                List<Room> rooms = roomRepository.findByClinicId(visit.getClinic().getId());
                taskRoom = rooms.stream().filter(r -> r.getDepartment().getId().equals(svc.getDepartment().getId())).findFirst()
                        .orElse(rooms.get(0));
            }

            ServiceTask task = ServiceTask.builder()
                    .serviceRequest(sr)
                    .invoice(invoice)
                    .department(svc.getDepartment())
                    .room(taskRoom)
                    .queueNumber(svc.getCode() + "-" + String.format("%03d", taskNo++))
                    .status("BLOCKED")
                    .build();
            serviceTaskRepository.save(task);
        }

        invoiceItemRepository.saveAll(invoiceItems);
        invoice.setTotalAmount(totalAmount);
        invoiceRepository.save(invoice);

        if (encounter.getStartedAt() == null) {
            encounter.setStartedAt(LocalDateTime.now());
        }
        encounter.setStatus("COMPLETED");
        encounter.setCompletedAt(LocalDateTime.now());
        encounterRepository.save(encounter);

        return getVisitDetail(visit.getId());
    }

    @Override
    @Transactional
    public VisitDetailResponse payInvoice(Long invoiceId, Long currentUserId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException("Hóa đơn không tồn tại"));

        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        for (InvoiceItem item : items) {
            if (item.getServiceRequest() != null) {
                serviceTaskRepository.findByServiceRequestId(item.getServiceRequest().getId())
                        .ifPresent(task -> {
                            task.setStatus("READY");
                            serviceTaskRepository.save(task);
                        });
            }
        }

        return getVisitDetail(invoice.getVisit().getId());
    }

    @Override
    @Transactional
    public VisitDetailResponse submitDiagnosticResult(SubmitResultRequest request, Long currentUserId) {
        ServiceTask task = serviceTaskRepository.findById(request.getServiceTaskId())
                .orElseThrow(() -> new AppException("Service Task không tồn tại"));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("User không tồn tại"));

        if (user.getRole() == com.org.care_slot.enums.RoleType.TECHNICIAN) {
            boolean hasPermission = technicianRoomRepository.existsByUserIdAndRoomId(user.getId(), task.getRoom().getId());
            if (!hasPermission) {
                List<Room> assigned = technicianRoomRepository.findRoomsByUserId(user.getId());
                if (assigned != null && !assigned.isEmpty()) {
                    boolean sameDepartment = assigned.stream()
                            .anyMatch(r -> r.getDepartment() != null 
                                    && task.getRoom().getDepartment() != null 
                                    && r.getDepartment().getId().equals(task.getRoom().getDepartment().getId()));
                    if (!sameDepartment) {
                        throw new AppException("Bạn không được phân công làm việc tại phòng cận lâm sàng này.");
                    }
                }
            }
        }

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        serviceTaskRepository.save(task);

        ServiceRequest sr = task.getServiceRequest();
        sr.setStatus("COMPLETED");
        serviceRequestRepository.save(sr);

        String safeResultData = (request.getResultData() != null && !request.getResultData().trim().isEmpty())
                ? request.getResultData()
                : "{}";

        ServiceResult result = ServiceResult.builder()
                .serviceRequest(sr)
                .template(sr.getServiceCatalog().getResultTemplate())
                .enteredBy(user)
                .status("FINAL")
                .findings(request.getFindings())
                .conclusion(request.getConclusion())
                .resultData(safeResultData)
                .finalizedAt(LocalDateTime.now())
                .build();
        serviceResultRepository.save(result);

        ClinicalOrder order = sr.getClinicalOrder();
        List<ServiceRequest> allRequests = serviceRequestRepository.findByClinicalOrderId(order.getId());
        boolean allFinal = allRequests.stream().allMatch(req -> {
            ServiceResult r = serviceResultRepository.findByServiceRequestId(req.getId()).orElse(null);
            return r != null && "FINAL".equals(r.getStatus());
        });

        if (allFinal) {
            order.setStatus("COMPLETED");
            clinicalOrderRepository.save(order);

            Visit visit = order.getVisit();
            long returnCount = encounterRepository.findByVisitIdOrderByCreatedAtAsc(visit.getId()).size() + 1;
            Room room = visit.getAppointment().getSlot().getRoom();
            if (room == null) {
                room = roomRepository.findByClinicId(visit.getClinic().getId()).get(0);
            }

            Encounter returnEncounter = Encounter.builder()
                    .visit(visit)
                    .encounterType("FOLLOW_UP_CONSULTATION")
                    .room(room)
                    .doctor(visit.getPrimaryDoctor())
                    .queueNumber("GASTRO-RETURN-" + String.format("%03d", returnCount))
                    .status("WAITING")
                    .build();
            encounterRepository.save(returnEncounter);
        }

        return getVisitDetail(sr.getVisit().getId());
    }

    @Override
    @Transactional
    public VisitDetailResponse finalizeVisit(Long visitId, PrescriptionRequest prescReq, VisitDispositionRequest dispReq, Long currentUserId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new AppException("Visit không tồn tại"));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("User không tồn tại"));

        if (prescReq != null && prescReq.getItems() != null && !prescReq.getItems().isEmpty()) {
            Encounter enc = encounterRepository.findById(prescReq.getEncounterId())
                    .orElseThrow(() -> new AppException("Encounter không tồn tại"));
            String prescCode = "RX-" + System.currentTimeMillis();

            BigDecimal totalCost = BigDecimal.ZERO;
            List<PrescriptionItem> pItems = new ArrayList<>();

            Prescription prescription = Prescription.builder()
                    .prescriptionCode(prescCode)
                    .visit(visit)
                    .encounter(enc)
                    .prescribedBy(enc.getDoctor())
                    .enteredBy(user)
                    .diagnosisNote(prescReq.getDiagnosisNote())
                    .status("FINAL")
                    .build();
            prescriptionRepository.save(prescription);

            for (PrescriptionRequest.Item itemDto : prescReq.getItems()) {
                BigDecimal itemAmount = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                totalCost = totalCost.add(itemAmount);

                PrescriptionItem pItem = PrescriptionItem.builder()
                        .prescription(prescription)
                        .drugName(itemDto.getDrugName())
                        .dosage(itemDto.getDosage())
                        .usageInstruction(itemDto.getUsageInstruction())
                        .quantity(itemDto.getQuantity())
                        .unit(itemDto.getUnit())
                        .unitPrice(itemDto.getUnitPrice())
                        .amount(itemAmount)
                        .build();
                pItems.add(pItem);
            }

            prescriptionItemRepository.saveAll(pItems);
            prescription.setTotalEstimatedCost(totalCost);
            prescriptionRepository.save(prescription);
        }

        if (dispReq != null) {
            VisitDisposition disp = visitDispositionRepository.findByVisitId(visit.getId()).orElse(null);
            if (disp == null) {
                disp = VisitDisposition.builder()
                        .visit(visit)
                        .dispositionType(dispReq.getDispositionType())
                        .notes(dispReq.getNotes())
                        .destinationFacility(dispReq.getDestinationFacility())
                        .destinationDepartment(dispReq.getDestinationDepartment())
                        .createdBy(user)
                        .build();
            } else {
                disp.setDispositionType(dispReq.getDispositionType());
                disp.setNotes(dispReq.getNotes());
            }
            visitDispositionRepository.save(disp);
        }

        List<Encounter> visitEncounters = encounterRepository.findByVisitIdOrderByCreatedAtAsc(visit.getId());
        for (Encounter enc : visitEncounters) {
            if (!"COMPLETED".equals(enc.getStatus())) {
                enc.setStatus("COMPLETED");
                if (enc.getCompletedAt() == null) {
                    enc.setCompletedAt(LocalDateTime.now());
                }
                encounterRepository.save(enc);
            }
        }

        visit.setStatus("COMPLETED");
        visit.setCompletedAt(LocalDateTime.now());
        visitRepository.save(visit);

        if (visit.getAppointment() != null) {
            Appointment appointment = visit.getAppointment();
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        return getVisitDetail(visit.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public VisitDetailResponse getVisitDetail(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new AppException("Visit không tồn tại: " + visitId));
        return buildVisitDetailResponse(visit);
    }

    @Override
    @Transactional(readOnly = true)
    public VisitDetailResponse getVisitDetailByAppointmentId(Long appointmentId) {
        Visit visit = visitRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppException("Chưa có Visit cho lịch hẹn: " + appointmentId));
        return buildVisitDetailResponse(visit);
    }

    private Doctor resolveDoctorForUser(Long currentUserId) {
        if (currentUserId == null) return null;
        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) return null;

        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        if (doctor != null) return doctor;

        if (user.getClinic() != null && user.getFullName() != null) {
            doctor = doctorRepository.findByFullNameAndClinicId(user.getFullName(), user.getClinic().getId()).orElse(null);
        }
        return doctor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitDetailResponse.EncounterDto> getEncounterQueueByRoom(Long roomId, String status, Long currentUserId) {
        User user = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        Doctor doctor = (user != null && com.org.care_slot.enums.RoleType.DOCTOR == user.getRole())
                ? resolveDoctorForUser(currentUserId)
                : null;

        List<Encounter> encounters;
        if (doctor != null) {
            if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                encounters = encounterRepository.findByRoomIdAndDoctorIdOrderByCreatedAtAsc(roomId, doctor.getId());
            } else {
                encounters = encounterRepository.findByRoomIdAndDoctorIdAndStatusOrderByCreatedAtAsc(roomId, doctor.getId(), status);
            }
        } else {
            if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                encounters = encounterRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
            } else {
                encounters = encounterRepository.findByRoomIdAndStatusOrderByCreatedAtAsc(roomId, status);
            }
        }

        return encounters.stream().map(e -> {
            var v = e.getVisit();
            var profile = v != null ? v.getPatientProfile() : null;
            var apt = v != null ? v.getAppointment() : null;
            return VisitDetailResponse.EncounterDto.builder()
                    .id(e.getId())
                    .visitId(v != null ? v.getId() : null)
                    .visitCode(v != null ? v.getVisitCode() : null)
                    .patientName(profile != null ? profile.getFullName() : null)
                    .patientPhone(profile != null ? profile.getPhone() : null)
                    .patientGender(profile != null ? profile.getGender() : null)
                    .patientDob(profile != null && profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null)
                    .bookingCode(apt != null ? apt.getBookingCode() : null)
                    .encounterType(e.getEncounterType())
                    .roomId(e.getRoom().getId())
                    .roomNumber(e.getRoom().getRoomNumber())
                    .roomName(e.getRoom().getName())
                    .doctorId(e.getDoctor().getId())
                    .doctorName(e.getDoctor().getFullName())
                    .queueNumber(e.getQueueNumber())
                    .status(e.getStatus())
                    .startedAt(e.getStartedAt())
                    .completedAt(e.getCompletedAt())
                    .createdAt(e.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitDetailResponse.ServiceRequestDto> getTaskQueueByRoom(Long roomId, String status, Long currentUserId) {
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null && com.org.care_slot.enums.RoleType.TECHNICIAN == user.getRole()) {
                boolean hasPermission = technicianRoomRepository.existsByUserIdAndRoomId(user.getId(), roomId);
                if (!hasPermission) {
                    List<Room> assigned = technicianRoomRepository.findRoomsByUserId(user.getId());
                    if (assigned != null && !assigned.isEmpty()) {
                        Room targetRoom = roomRepository.findById(roomId).orElse(null);
                        boolean sameDepartment = targetRoom != null && assigned.stream()
                                .anyMatch(r -> r.getDepartment() != null 
                                        && targetRoom.getDepartment() != null 
                                        && r.getDepartment().getId().equals(targetRoom.getDepartment().getId()));
                        if (!sameDepartment) {
                            throw new AppException("Bạn không có quyền truy cập hàng chờ của phòng này.");
                        }
                    }
                }
            }
        }

        List<ServiceTask> tasks;
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            tasks = serviceTaskRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            tasks = serviceTaskRepository.findByRoomIdAndStatusOrderByCreatedAtDesc(roomId, status);
        } else {
            tasks = serviceTaskRepository.findByRoomIdAndStatusOrderByCreatedAtAsc(roomId, status);
        }

        return tasks.stream().map(t -> {
            ServiceRequest sr = t.getServiceRequest();
            ServiceCatalog svc = sr.getServiceCatalog();
            Visit v = sr.getVisit();
            PatientProfile p = v != null ? v.getPatientProfile() : null;
            Appointment apt = v != null ? v.getAppointment() : null;
            ClinicalOrder order = sr.getClinicalOrder();
            Doctor doctor = order != null ? order.getOrderedBy() : null;
            ServiceResult res = serviceResultRepository.findByServiceRequestId(sr.getId()).orElse(null);

            return VisitDetailResponse.ServiceRequestDto.builder()
                    .id(sr.getId())
                    .visitId(v != null ? v.getId() : null)
                    .patientProfileId(p != null ? p.getId() : null)
                    .visitCode(v != null ? v.getVisitCode() : null)
                    .bookingCode(apt != null ? apt.getBookingCode() : null)
                    .patientName(p != null ? p.getFullName() : null)
                    .patientPhone(p != null ? p.getPhone() : null)
                    .patientGender(p != null ? p.getGender() : null)
                    .patientDob(p != null && p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null)
                    .orderedByName(doctor != null ? doctor.getFullName() : null)
                    .serviceId(svc.getId())
                    .serviceCode(svc.getCode())
                    .serviceName(svc.getName())
                    .serviceType(svc.getServiceType())
                    .price(svc.getPrice())
                    .status(sr.getStatus())
                    .createdAt(t.getCreatedAt())
                    .task(VisitDetailResponse.ServiceTaskDto.builder()
                            .id(t.getId())
                            .roomId(t.getRoom().getId())
                            .roomNumber(t.getRoom().getRoomNumber())
                            .roomName(t.getRoom().getName())
                            .queueNumber(t.getQueueNumber())
                            .status(t.getStatus())
                            .build())
                    .result(res != null ? VisitDetailResponse.ServiceResultDto.builder()
                            .id(res.getId())
                            .enteredByName(res.getEnteredBy() != null ? res.getEnteredBy().getFullName() : null)
                            .status(res.getStatus())
                            .findings(res.getFindings())
                            .conclusion(res.getConclusion())
                            .resultData(res.getResultData())
                            .finalizedAt(res.getFinalizedAt())
                            .build() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitDetailResponse.ServiceCatalogDto> getCatalog(Long clinicId) {
        Long effectiveClinicId = (clinicId != null) ? clinicId : 1L;
        List<ServiceCatalog> catalog = serviceCatalogRepository.findByClinicId(effectiveClinicId);
        if (catalog == null || catalog.isEmpty()) {
            catalog = serviceCatalogRepository.findAll();
        }
        return catalog.stream().map(s -> VisitDetailResponse.ServiceCatalogDto.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .serviceType(s.getServiceType())
                .price(s.getPrice())
                .paymentPolicy(s.getPaymentPolicy())
                .status(s.getStatus())
                .defaultRoomId(s.getDefaultRoom() != null ? s.getDefaultRoom().getId() : null)
                .defaultRoomName(s.getDefaultRoom() != null ? s.getDefaultRoom().getName() : null)
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitDetailResponse.RoomDto> getRooms(Long clinicId, Long currentUserId) {
        Long effectiveClinicId = (clinicId != null) ? clinicId : 1L;
        User user = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        List<Room> rooms;
        if (user != null && com.org.care_slot.enums.RoleType.DOCTOR == user.getRole()) {
            Doctor doctor = resolveDoctorForUser(currentUserId);
            if (doctor != null) {
                List<Room> doctorRoomsFromSlots = appointmentSlotRepository.findRoomsByDoctorId(doctor.getId());
                List<Room> doctorRoomsFromEncounters = encounterRepository.findRoomsByDoctorId(doctor.getId());

                java.util.Set<Room> combined = new java.util.LinkedHashSet<>();
                if (doctorRoomsFromSlots != null) combined.addAll(doctorRoomsFromSlots);
                if (doctorRoomsFromEncounters != null) combined.addAll(doctorRoomsFromEncounters);

                if (!combined.isEmpty()) {
                    rooms = new ArrayList<>(combined);
                } else {
                    rooms = roomRepository.findByClinicId(effectiveClinicId).stream()
                            .filter(r -> "CONSULTATION".equalsIgnoreCase(r.getRoomType()))
                            .collect(Collectors.toList());
                }
            } else {
                rooms = roomRepository.findByClinicId(effectiveClinicId);
            }
        } else if (user != null && com.org.care_slot.enums.RoleType.TECHNICIAN == user.getRole()) {
            List<Room> techRooms = technicianRoomRepository.findRoomsByUserId(user.getId());
            if (techRooms != null && !techRooms.isEmpty()) {
                List<Long> deptIds = techRooms.stream()
                        .map(r -> r.getDepartment() != null ? r.getDepartment().getId() : null)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
                List<Room> deptRooms = roomRepository.findByClinicId(effectiveClinicId).stream()
                        .filter(r -> r.getDepartment() != null && deptIds.contains(r.getDepartment().getId()))
                        .collect(Collectors.toList());
                rooms = !deptRooms.isEmpty() ? deptRooms : techRooms;
            } else {
                rooms = roomRepository.findByClinicId(effectiveClinicId).stream()
                        .filter(r -> !"CONSULTATION".equalsIgnoreCase(r.getRoomType()) && !"CASHIER".equalsIgnoreCase(r.getRoomType()))
                        .collect(Collectors.toList());
            }
        } else {
            rooms = roomRepository.findByClinicId(effectiveClinicId);
        }

        return rooms.stream().map(r -> VisitDetailResponse.RoomDto.builder()
                .id(r.getId())
                .roomNumber(r.getRoomNumber())
                .name(r.getName())
                .roomType(r.getRoomType())
                .status(r.getStatus())
                .departmentId(r.getDepartment() != null ? r.getDepartment().getId() : null)
                .departmentName(r.getDepartment() != null ? r.getDepartment().getName() : null)
                .build()
        ).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<VisitDetailResponse> getPatientHistory(Long patientProfileId) {
        List<Visit> visits = visitRepository.findByPatientProfileIdOrderByCreatedAtDesc(patientProfileId);
        return visits.stream()
                .filter(v -> "COMPLETED".equals(v.getStatus()))
                .map(this::buildVisitDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VisitDetailResponse startEncounter(Long encounterId, Long currentUserId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new AppException("Encounter không tồn tại"));

        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null && com.org.care_slot.enums.RoleType.DOCTOR == user.getRole()) {
                Doctor doctor = resolveDoctorForUser(currentUserId);
                if (doctor != null && !encounter.getDoctor().getId().equals(doctor.getId())) {
                    throw new AppException("Bạn không có quyền thao tác trên ca khám của bác sĩ khác.");
                }
            }
        }

        if ("WAITING".equals(encounter.getStatus())) {
            encounter.setStatus("IN_PROGRESS");
            if (encounter.getStartedAt() == null) {
                encounter.setStartedAt(LocalDateTime.now());
            }
            encounterRepository.save(encounter);
        }
        return getVisitDetail(encounter.getVisit().getId());
    }

    private VisitDetailResponse buildVisitDetailResponse(Visit visit) {
        List<Encounter> encounters = encounterRepository.findByVisitIdOrderByCreatedAtAsc(visit.getId());
        List<VitalSign> vitalSigns = vitalSignRepository.findByVisitId(visit.getId());
        List<ClinicalNote> clinicalNotes = clinicalNoteRepository.findByVisitId(visit.getId());
        List<ClinicalOrder> clinicalOrders = clinicalOrderRepository.findByVisitIdOrderByOrderRoundAsc(visit.getId());
        List<Invoice> invoices = invoiceRepository.findByVisitId(visit.getId());
        Prescription prescription = prescriptionRepository.findByVisitId(visit.getId()).stream().findFirst().orElse(null);
        VisitDisposition disposition = visitDispositionRepository.findByVisitId(visit.getId()).orElse(null);

        return VisitDetailResponse.builder()
                .id(visit.getId())
                .visitCode(visit.getVisitCode())
                .appointmentId(visit.getAppointment().getId())
                .bookingCode(visit.getAppointment().getBookingCode())
                .patientProfileId(visit.getPatientProfile().getId())
                .patientName(visit.getPatientProfile().getFullName())
                .patientPhone(visit.getPatientProfile().getPhone())
                .patientGender(visit.getPatientProfile().getGender())
                .patientDob(visit.getPatientProfile().getDateOfBirth() != null ? visit.getPatientProfile().getDateOfBirth().toString() : null)
                .clinicId(visit.getClinic().getId())
                .clinicName(visit.getClinic().getName())
                .primaryDoctorId(visit.getPrimaryDoctor().getId())
                .primaryDoctorName(visit.getPrimaryDoctor().getFullName())
                .status(visit.getStatus())
                .checkedInAt(visit.getCheckedInAt())
                .completedAt(visit.getCompletedAt())
                .encounters(encounters.stream().map(e -> {
                    var profile = visit.getPatientProfile();
                    var apt = visit.getAppointment();
                    return VisitDetailResponse.EncounterDto.builder()
                            .id(e.getId())
                            .visitId(visit.getId())
                            .visitCode(visit.getVisitCode())
                            .patientName(profile != null ? profile.getFullName() : null)
                            .patientPhone(profile != null ? profile.getPhone() : null)
                            .patientGender(profile != null ? profile.getGender() : null)
                            .patientDob(profile != null && profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null)
                            .bookingCode(apt != null ? apt.getBookingCode() : null)
                            .encounterType(e.getEncounterType())
                            .roomId(e.getRoom().getId())
                            .roomNumber(e.getRoom().getRoomNumber())
                            .roomName(e.getRoom().getName())
                            .doctorId(e.getDoctor().getId())
                            .doctorName(e.getDoctor().getFullName())
                            .queueNumber(e.getQueueNumber())
                            .status(e.getStatus())
                            .startedAt(e.getStartedAt())
                            .completedAt(e.getCompletedAt())
                            .createdAt(e.getCreatedAt())
                            .build();
                }).collect(Collectors.toList()))
                .vitalSigns(vitalSigns.stream().map(v -> VisitDetailResponse.VitalSignDto.builder()
                        .id(v.getId())
                        .encounterId(v.getEncounter().getId())
                        .heightCm(v.getHeightCm())
                        .weightKg(v.getWeightKg())
                        .temperatureC(v.getTemperatureC())
                        .heartRateBpm(v.getHeartRateBpm())
                        .respiratoryRate(v.getRespiratoryRate())
                        .systolicBp(v.getSystolicBp())
                        .diastolicBp(v.getDiastolicBp())
                        .spo2(v.getSpo2())
                        .recordedByName(v.getRecordedBy().getFullName())
                        .createdAt(v.getCreatedAt())
                        .build()).collect(Collectors.toList()))
                .clinicalNotes(clinicalNotes.stream().map(n -> VisitDetailResponse.ClinicalNoteDto.builder()
                        .id(n.getId())
                        .encounterId(n.getEncounter().getId())
                        .enteredByName(n.getEnteredBy().getFullName())
                        .clinicalAuthorName(n.getClinicalAuthor().getFullName())
                        .status(n.getStatus())
                        .formData(n.getFormData())
                        .createdAt(n.getCreatedAt())
                        .build()).collect(Collectors.toList()))
                .clinicalOrders(clinicalOrders.stream().map(o -> VisitDetailResponse.ClinicalOrderDto.builder()
                        .id(o.getId())
                        .orderCode(o.getOrderCode())
                        .orderRound(o.getOrderRound())
                        .orderedByName(o.getOrderedBy().getFullName())
                        .status(o.getStatus())
                        .createdAt(o.getCreatedAt())
                        .serviceRequests(serviceRequestRepository.findByClinicalOrderId(o.getId()).stream().map(sr -> {
                            ServiceTask task = serviceTaskRepository.findByServiceRequestId(sr.getId()).orElse(null);
                            ServiceResult res = serviceResultRepository.findByServiceRequestId(sr.getId()).orElse(null);
                            return VisitDetailResponse.ServiceRequestDto.builder()
                                    .id(sr.getId())
                                    .visitId(visit.getId())
                                    .patientProfileId(visit.getPatientProfile() != null ? visit.getPatientProfile().getId() : null)
                                    .visitCode(visit.getVisitCode())
                                    .bookingCode(visit.getAppointment() != null ? visit.getAppointment().getBookingCode() : null)
                                    .patientName(visit.getPatientProfile() != null ? visit.getPatientProfile().getFullName() : null)
                                    .patientPhone(visit.getPatientProfile() != null ? visit.getPatientProfile().getPhone() : null)
                                    .patientGender(visit.getPatientProfile() != null ? visit.getPatientProfile().getGender() : null)
                                    .patientDob(visit.getPatientProfile() != null && visit.getPatientProfile().getDateOfBirth() != null ? visit.getPatientProfile().getDateOfBirth().toString() : null)
                                    .orderedByName(o.getOrderedBy() != null ? o.getOrderedBy().getFullName() : null)
                                    .serviceId(sr.getServiceCatalog().getId())
                                    .serviceCode(sr.getServiceCatalog().getCode())
                                    .serviceName(sr.getServiceCatalog().getName())
                                    .serviceType(sr.getServiceCatalog().getServiceType())
                                    .price(sr.getServiceCatalog().getPrice())
                                    .status(sr.getStatus())
                                    .task(task != null ? VisitDetailResponse.ServiceTaskDto.builder()
                                            .id(task.getId())
                                            .roomId(task.getRoom().getId())
                                            .roomNumber(task.getRoom().getRoomNumber())
                                            .roomName(task.getRoom().getName())
                                            .queueNumber(task.getQueueNumber())
                                            .status(task.getStatus())
                                            .build() : null)
                                    .result(res != null ? VisitDetailResponse.ServiceResultDto.builder()
                                            .id(res.getId())
                                            .enteredByName(res.getEnteredBy().getFullName())
                                            .status(res.getStatus())
                                            .findings(res.getFindings())
                                            .conclusion(res.getConclusion())
                                            .resultData(res.getResultData())
                                            .finalizedAt(res.getFinalizedAt())
                                            .build() : null)
                                    .build();
                        }).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList()))
                .invoices(invoices.stream().map(inv -> VisitDetailResponse.InvoiceDto.builder()
                        .id(inv.getId())
                        .invoiceCode(inv.getInvoiceCode())
                        .invoiceType(inv.getInvoiceType())
                        .totalAmount(inv.getTotalAmount())
                        .status(inv.getStatus())
                        .paidAt(inv.getPaidAt())
                        .build()).collect(Collectors.toList()))
                .prescription(prescription != null ? VisitDetailResponse.PrescriptionDto.builder()
                        .id(prescription.getId())
                        .prescriptionCode(prescription.getPrescriptionCode())
                        .prescribedByName(prescription.getPrescribedBy().getFullName())
                        .diagnosisNote(prescription.getDiagnosisNote())
                        .totalEstimatedCost(prescription.getTotalEstimatedCost())
                        .items(prescriptionItemRepository.findByPrescriptionId(prescription.getId()).stream().map(pi ->
                                VisitDetailResponse.PrescriptionItemDto.builder()
                                        .id(pi.getId())
                                        .drugName(pi.getDrugName())
                                        .dosage(pi.getDosage())
                                        .usageInstruction(pi.getUsageInstruction())
                                        .quantity(pi.getQuantity())
                                        .unit(pi.getUnit())
                                        .unitPrice(pi.getUnitPrice())
                                        .amount(pi.getAmount())
                                        .build()).collect(Collectors.toList()))
                        .build() : null)
                .disposition(disposition != null ? VisitDetailResponse.DispositionDto.builder()
                        .id(disposition.getId())
                        .dispositionType(disposition.getDispositionType())
                        .notes(disposition.getNotes())
                        .destinationFacility(disposition.getDestinationFacility())
                        .destinationDepartment(disposition.getDestinationDepartment())
                        .createdByName(disposition.getCreatedBy().getFullName())
                        .build() : null)
                .build();
    }
}
