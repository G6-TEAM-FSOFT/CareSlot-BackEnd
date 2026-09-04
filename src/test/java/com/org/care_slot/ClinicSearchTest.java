package com.org.care_slot;

import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.repository.ClinicRepository;
import com.org.care_slot.repository.SpecialtyRepository;
import com.org.care_slot.service.ClinicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ClinicSearchTest {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private ClinicService clinicService;

    private Clinic clinicA;
    private Clinic clinicB;
    private Clinic clinicCInactive;
    private Specialty specialtyCardio;
    private Specialty specialtyDental;

    @BeforeEach
    void setUp() {
        specialtyCardio = specialtyRepository.save(Specialty.builder()
                .name("Tim mạch T131")
                .description("Khoa Tim Mạch")
                .status("ACTIVE")
                .build());

        specialtyDental = specialtyRepository.save(Specialty.builder()
                .name("Nha khoa T131")
                .description("Khoa Răng Hàm Mặt")
                .status("ACTIVE")
                .build());

        clinicA = clinicRepository.save(Clinic.builder()
                .name("Phòng khám Đa khoa Hoàn Mỹ Cầu Giấy")
                .address("123 Cầu Giấy, Quận Cầu Giấy, Hà Nội")
                .phone("0981111111")
                .status("ACTIVE")
                .specialties(new HashSet<>(Set.of(specialtyCardio, specialtyDental)))
                .build());

        clinicB = clinicRepository.save(Clinic.builder()
                .name("Bệnh viện Quốc tế Medlatec Ba Đình")
                .address("456 Kim Mã, Quận Ba Đình, Hà Nội")
                .phone("0982222222")
                .status("ACTIVE")
                .specialties(new HashSet<>(Set.of(specialtyDental)))
                .build());

        clinicCInactive = clinicRepository.save(Clinic.builder()
                .name("Phòng khám Cầu Giấy Tạm Dừng")
                .address("789 Cầu Giấy, Hà Nội")
                .phone("0983333333")
                .status("INACTIVE")
                .specialties(new HashSet<>(Set.of(specialtyCardio)))
                .build());
    }

    @Test
    @DisplayName("T131-01: Search clinics by keyword in name")
    void testSearchByKeywordInName() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Hoàn Mỹ", null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(c -> c.getName().contains("Hoàn Mỹ"));
        assertThat(result.getContent()).noneMatch(c -> c.getStatus().equals("INACTIVE"));
    }

    @Test
    @DisplayName("T131-02: Search clinics by keyword in address")
    void testSearchByKeywordInAddress() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Kim Mã", null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(clinicB.getId());
    }

    @Test
    @DisplayName("T131-03: Filter clinics by specialtyId")
    void testFilterBySpecialtyId() {
        // Specialty Cardio is only active in Clinic A (Clinic C is INACTIVE)
        PageResponse<ClinicResponse> result = clinicService.getClinics(null, specialtyCardio.getId(), null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(clinicA.getId());

        // Specialty Dental is in both Clinic A and Clinic B
        PageResponse<ClinicResponse> dentalResult = clinicService.getClinics(null, specialtyDental.getId(), null,
                PageRequest.of(0, 10));
        List<Long> clinicIds = dentalResult.getContent().stream().map(ClinicResponse::getId).toList();
        assertThat(clinicIds).contains(clinicA.getId(), clinicB.getId());
    }

    @Test
    @DisplayName("T131-04: Filter clinics by location parameter")
    void testFilterByLocation() {
        PageResponse<ClinicResponse> result = clinicService.getClinics(null, null, "Cầu Giấy", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(c -> c.getAddress().contains("Cầu Giấy"));
        assertThat(result.getContent()).noneMatch(c -> c.getId().equals(clinicCInactive.getId()));
    }

    @Test
    @DisplayName("T131-05: Combined filter by keyword, specialtyId, and location")
    void testCombinedFilter() {
        PageResponse<ClinicResponse> result = clinicService.getClinics(
                "Hoàn Mỹ",
                specialtyCardio.getId(),
                "Cầu Giấy",
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(clinicA.getId());
    }

    @Test
    @DisplayName("T131-06: Non-matching filter returns empty list and correct pagination")
    void testNonMatchingFilterReturnsEmpty() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Không Tồn Tại XYZ", null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("T131-07: Inactive clinics are never returned")
    void testInactiveClinicsAreNeverReturned() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Tạm Dừng", null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }
}
