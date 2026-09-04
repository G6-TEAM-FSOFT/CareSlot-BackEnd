package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.ClinicUpdateRequest;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.dto.response.SpecialtyResponse;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.ClinicRepository;
import com.org.care_slot.repository.SpecialtyRepository;
import com.org.care_slot.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final com.org.care_slot.repository.DoctorRepository doctorRepository;

    @Override
    public PageResponse<ClinicResponse> getClinics(
            String keyword,
            Long specialtyId,
            String location,
            BigDecimal userLat,
            BigDecimal userLng,
            String sortBy,
            Pageable pageable
    ) {
        String sortType = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim().toUpperCase() : "DEFAULT";

        if ("DISTANCE_ASC".equals(sortType)) {
            if (userLat == null || userLng == null) {
                throw new AppException(ErrorCode.INVALID_COORDINATES);
            }
        }

        // 1. Fetch all matching active clinics from DB
        List<Clinic> clinics = clinicRepository.findAllActiveFiltered(keyword, specialtyId, location);

        if (clinics.isEmpty()) {
            return PageResponse.<ClinicResponse>builder()
                    .content(Collections.emptyList())
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .totalElements(0L)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        // 2. Query future available slots and active doctors for all matched clinics in batch
        List<Long> clinicIds = clinics.stream().map(Clinic::getId).toList();
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime nowVn = ZonedDateTime.now(vnZone);
        LocalDate today = nowVn.toLocalDate();
        LocalTime nowTime = nowVn.toLocalTime();

        List<AppointmentSlot> futureSlots = appointmentSlotRepository.findFutureAvailableSlotsByClinicIds(clinicIds, today, nowTime);
        Map<Long, List<AppointmentSlot>> slotsByClinicId = futureSlots.stream()
                .collect(Collectors.groupingBy(s -> s.getDoctor().getClinic().getId()));

        List<Doctor> activeDoctors = doctorRepository.findActiveDoctorsByClinicIds(clinicIds);
        Map<Long, List<Doctor>> doctorsByClinicId = activeDoctors.stream()
                .collect(Collectors.groupingBy(d -> d.getClinic().getId()));

        // 3. Map entities to DTOs with derived data
        List<ClinicResponse> allDtoList = new ArrayList<>(clinics.stream().map(clinic -> {
            // Specialty names
            List<String> specialtyNames = clinic.getSpecialties().stream()
                    .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                    .map(Specialty::getName)
                    .toList();

            // Min & Max consultation fee from active doctors
            List<Doctor> clinicDoctors = doctorsByClinicId.getOrDefault(clinic.getId(), Collections.emptyList());

            BigDecimal minFee = null;
            BigDecimal maxFee = null;
            if (!clinicDoctors.isEmpty()) {
                minFee = clinicDoctors.stream()
                        .map(Doctor::getConsultationFee)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo)
                        .orElse(null);
                maxFee = clinicDoctors.stream()
                        .map(Doctor::getConsultationFee)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo)
                        .orElse(null);
            }

            // Earliest available slot
            List<AppointmentSlot> clinicSlots = slotsByClinicId.getOrDefault(clinic.getId(), Collections.emptyList());
            LocalDateTime earliestSlot = clinicSlots.stream()
                    .map(s -> LocalDateTime.of(s.getAppointmentDate(), s.getStartTime()))
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            // Distance calculation using Haversine
            Double distanceKm = null;
            if (userLat != null && userLng != null && clinic.getLatitude() != null && clinic.getLongitude() != null) {
                distanceKm = calculateHaversine(
                        userLat.doubleValue(),
                        userLng.doubleValue(),
                        clinic.getLatitude().doubleValue(),
                        clinic.getLongitude().doubleValue()
                );
            }

            return ClinicResponse.builder()
                    .id(clinic.getId())
                    .name(clinic.getName())
                    .address(clinic.getAddress())
                    .latitude(clinic.getLatitude())
                    .longitude(clinic.getLongitude())
                    .phone(clinic.getPhone())
                    .description(clinic.getDescription())
                    .status(clinic.getStatus())
                    .specialtyNames(specialtyNames)
                    .minConsultationFee(minFee)
                    .maxConsultationFee(maxFee)
                    .earliestAvailableSlot(earliestSlot)
                    .distanceKm(distanceKm)
                    .build();
        }).toList());

        // 4. Global Sorting
        switch (sortType) {
            case "DISTANCE_ASC" -> allDtoList.sort(
                    Comparator.comparing(ClinicResponse::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(ClinicResponse::getName, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(ClinicResponse::getId)
            );
            case "EARLIEST_SLOT" -> allDtoList.sort(
                    Comparator.comparing(ClinicResponse::getEarliestAvailableSlot, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(ClinicResponse::getName, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(ClinicResponse::getId)
            );
            default -> allDtoList.sort(
                    Comparator.comparing(ClinicResponse::getName, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(ClinicResponse::getId)
            );
        }

        // 5. In-Memory Pagination
        int total = allDtoList.size();
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int fromIndex = Math.min((int) pageable.getOffset(), total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ClinicResponse> pagedContent = (fromIndex < total) ? allDtoList.subList(fromIndex, toIndex) : Collections.emptyList();
        int totalPages = (int) Math.ceil((double) total / size);

        return PageResponse.<ClinicResponse>builder()
                .content(pagedContent)
                .page(page)
                .size(size)
                .totalElements((long) total)
                .totalPages(totalPages)
                .last(toIndex >= total)
                .build();
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return Math.round(distance * 10.0) / 10.0;
    }

    @Override
    public ClinicDetailResponse getClinicDetail(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        List<SpecialtyResponse> specialties = specialtyRepository.findByClinics_IdAndStatus(id, "ACTIVE").stream()
                .map(s -> SpecialtyResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .status(s.getStatus())
                        .build())
                .toList();

        return ClinicDetailResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .latitude(clinic.getLatitude())
                .longitude(clinic.getLongitude())
                .phone(clinic.getPhone())
                .description(clinic.getDescription())
                .status(clinic.getStatus())
                .specialties(specialties)
                .build();
    }

    @Override
    @Transactional
    public ClinicDetailResponse updateClinic(Long clinicId, ClinicUpdateRequest request, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        if (request.getLatitude() != null) {
            clinic.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            clinic.setLongitude(request.getLongitude());
        }
        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }
        if (request.getDescription() != null) {
            clinic.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            clinic.setStatus(request.getStatus());
        }

        Clinic updated = clinicRepository.save(clinic);
        return getClinicDetail(updated.getId());
    }

    @Override
    public List<SpecialtyResponse> getClinicSpecialties(Long clinicId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        if (!clinicRepository.existsById(clinicId)) {
            throw new AppException(ErrorCode.CLINIC_NOT_FOUND);
        }

        return specialtyRepository.findByClinics_IdAndStatus(clinicId, "ACTIVE").stream()
                .map(s -> SpecialtyResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .status(s.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ClinicDetailResponse addSpecialtyToClinic(Long clinicId, Long specialtyId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(specialty.getStatus())) {
            throw new AppException(ErrorCode.SPECIALTY_NOT_FOUND);
        }

        clinic.getSpecialties().add(specialty);
        clinicRepository.save(clinic);

        return getClinicDetail(clinicId);
    }

    @Override
    @Transactional
    public ClinicDetailResponse removeSpecialtyFromClinic(Long clinicId, Long specialtyId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        clinic.getSpecialties().removeIf(s -> s.getId().equals(specialtyId));
        clinicRepository.save(clinic);

        return getClinicDetail(clinicId);
    }

    private ClinicResponse mapToClinicResponse(Clinic clinic) {
        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .latitude(clinic.getLatitude())
                .longitude(clinic.getLongitude())
                .phone(clinic.getPhone())
                .description(clinic.getDescription())
                .status(clinic.getStatus())
                .build();
    }
}
