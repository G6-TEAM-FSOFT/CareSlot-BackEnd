package com.org.care_slot;

import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.ClinicRepository;
import com.org.care_slot.repository.DoctorRepository;
import com.org.care_slot.repository.SpecialtyRepository;
import com.org.care_slot.service.ClinicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ClinicSearchTest {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    private ClinicService clinicService;

    private Clinic clinicA;
    private Clinic clinicB;
    private Clinic clinicCInactive;
    private Specialty specialtyCardio;
    private Specialty specialtyDental;
    private Doctor doctorA1;
    private Doctor doctorA2;
    private Doctor doctorB1;

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

        // Clinic A: Cau Giay, Lat 21.033333, Lng 105.790000
        clinicA = clinicRepository.save(Clinic.builder()
                .name("Phòng khám Đa khoa Hoàn Mỹ Cầu Giấy")
                .address("123 Cầu Giấy, Quận Cầu Giấy, Hà Nội")
                .latitude(new BigDecimal("21.0333330"))
                .longitude(new BigDecimal("105.7900000"))
                .phone("0981111111")
                .status("ACTIVE")
                .specialties(new HashSet<>(Set.of(specialtyCardio, specialtyDental)))
                .build());

        // Clinic B: Ba Dinh, Lat 21.031000, Lng 105.820000
        clinicB = clinicRepository.save(Clinic.builder()
                .name("Bệnh viện Quốc tế Medlatec Ba Đình")
                .address("456 Kim Mã, Quận Ba Đình, Hà Nội")
                .latitude(new BigDecimal("21.0310000"))
                .longitude(new BigDecimal("105.8200000"))
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

        // Doctors for Clinic A
        doctorA1 = doctorRepository.save(Doctor.builder()
                .clinic(clinicA)
                .specialty(specialtyCardio)
                .fullName("BS Nguyễn Văn A")
                .consultationFee(new BigDecimal("300000.00"))
                .status("ACTIVE")
                .build());

        doctorA2 = doctorRepository.save(Doctor.builder()
                .clinic(clinicA)
                .specialty(specialtyDental)
                .fullName("BS Trần Thị B")
                .consultationFee(new BigDecimal("500000.00"))
                .status("ACTIVE")
                .build());

        // Doctor for Clinic B
        doctorB1 = doctorRepository.save(Doctor.builder()
                .clinic(clinicB)
                .specialty(specialtyDental)
                .fullName("BS Lê Văn C")
                .consultationFee(new BigDecimal("400000.00"))
                .status("ACTIVE")
                .build());
    }

    // ==========================================
    // T-131 & T-132 Regression Tests
    // ==========================================

    @Test
    @DisplayName("T131-01: Search clinics by keyword in name")
    void testSearchByKeywordInName() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Hoàn Mỹ", null, null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(c -> c.getName().contains("Hoàn Mỹ"));
        assertThat(result.getContent()).noneMatch(c -> c.getStatus().equals("INACTIVE"));
    }

    @Test
    @DisplayName("T131-02: Search clinics by keyword in address")
    void testSearchByKeywordInAddress() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Kim Mã", null, null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(clinicB.getId());
    }

    @Test
    @DisplayName("T131-03: Filter clinics by specialtyId")
    void testFilterBySpecialtyId() {
        PageResponse<ClinicResponse> result = clinicService.getClinics(null, specialtyCardio.getId(), null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(clinicA.getId());

        PageResponse<ClinicResponse> dentalResult = clinicService.getClinics(null, specialtyDental.getId(), null, null, null, "DEFAULT", PageRequest.of(0, 10));
        List<Long> clinicIds = dentalResult.getContent().stream().map(ClinicResponse::getId).toList();
        assertThat(clinicIds).contains(clinicA.getId(), clinicB.getId());
    }

    @Test
    @DisplayName("T131-04: Filter clinics by location parameter")
    void testFilterByLocation() {
        PageResponse<ClinicResponse> result = clinicService.getClinics(null, null, "Cầu Giấy", null, null, "DEFAULT", PageRequest.of(0, 10));

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
                null, null, "DEFAULT",
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(clinicA.getId());
    }

    @Test
    @DisplayName("T131-06: Non-matching filter returns empty list and correct pagination")
    void testNonMatchingFilterReturnsEmpty() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Không Tồn Tại XYZ", null, null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("T131-07: Inactive clinics are never returned")
    void testInactiveClinicsAreNeverReturned() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Tạm Dừng", null, null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    // ==========================================
    // T-134 New Tests: Distance, Earliest Slot, Fees
    // ==========================================

    @Test
    @DisplayName("T134-01: Consultation fee min and max calculation on Clinic Card")
    void testConsultationFeeMinMaxCalculation() {
        PageResponse<ClinicResponse> result = clinicService.getClinics("Hoàn Mỹ", null, null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        ClinicResponse resA = result.getContent().get(0);
        assertThat(resA.getMinConsultationFee()).isEqualByComparingTo("300000.00");
        assertThat(resA.getMaxConsultationFee()).isEqualByComparingTo("500000.00");
        assertThat(resA.getSpecialtyNames()).contains("Tim mạch T131", "Nha khoa T131");
    }

    @Test
    @DisplayName("T134-02: Sort by distance with valid user coordinates")
    void testSortByDistanceSuccess() {
        // User is near Cau Giay: Lat 21.033500, Lng 105.790500 (very close to Clinic A)
        BigDecimal userLat = new BigDecimal("21.033500");
        BigDecimal userLng = new BigDecimal("105.790500");

        PageResponse<ClinicResponse> result = clinicService.getClinics(null, null, null, userLat, userLng, "DISTANCE_ASC", PageRequest.of(0, 10));

        assertThat(result.getContent().size()).isGreaterThanOrEqualTo(2);
        ClinicResponse first = result.getContent().get(0);
        ClinicResponse second = result.getContent().get(1);

        // Clinic A should be first because it is much closer than Clinic B
        assertThat(first.getId()).isEqualTo(clinicA.getId());
        assertThat(first.getDistanceKm()).isNotNull();
        assertThat(second.getDistanceKm()).isNotNull();
        assertThat(first.getDistanceKm()).isLessThan(second.getDistanceKm());
    }

    @Test
    @DisplayName("T134-03: Sort by distance missing userLat throws 400 INVALID_COORDINATES")
    void testSortByDistanceMissingUserLatThrowsException() {
        BigDecimal userLng = new BigDecimal("105.790500");

        assertThatThrownBy(() -> clinicService.getClinics(null, null, null, null, userLng, "DISTANCE_ASC", PageRequest.of(0, 10)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_COORDINATES));
    }

    @Test
    @DisplayName("T134-04: Sort by distance missing userLng throws 400 INVALID_COORDINATES")
    void testSortByDistanceMissingUserLngThrowsException() {
        BigDecimal userLat = new BigDecimal("21.033500");

        assertThatThrownBy(() -> clinicService.getClinics(null, null, null, userLat, null, "DISTANCE_ASC", PageRequest.of(0, 10)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_COORDINATES));
    }

    @Test
    @DisplayName("T134-05: Clinic without coordinates appears last when sorting by distance")
    void testClinicWithoutCoordinatesNullsLast() {
        // Create Clinic D without coordinates
        Clinic clinicDNoCoords = clinicRepository.save(Clinic.builder()
                .name("Phòng khám Không Tọa Độ")
                .address("Hà Nội")
                .status("ACTIVE")
                .build());

        BigDecimal userLat = new BigDecimal("21.033500");
        BigDecimal userLng = new BigDecimal("105.790500");

        PageResponse<ClinicResponse> result = clinicService.getClinics(null, null, null, userLat, userLng, "DISTANCE_ASC", PageRequest.of(0, 10));

        List<ClinicResponse> list = result.getContent();
        ClinicResponse last = list.get(list.size() - 1);
        assertThat(last.getId()).isEqualTo(clinicDNoCoords.getId());
        assertThat(last.getDistanceKm()).isNull();
    }

    @Test
    @DisplayName("T134-06: Sort by earliest available slot (earlier slot appears first)")
    void testSortByEarliestSlotSuccess() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1);
        LocalDate dayAfterTomorrow = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(2);

        // Clinic B has slot tomorrow at 08:00
        appointmentSlotRepository.save(AppointmentSlot.builder()
                .doctor(doctorB1)
                .appointmentDate(tomorrow)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .build());

        // Clinic A has slot day after tomorrow at 08:00
        appointmentSlotRepository.save(AppointmentSlot.builder()
                .doctor(doctorA1)
                .appointmentDate(dayAfterTomorrow)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .build());

        PageResponse<ClinicResponse> result = clinicService.getClinics(null, null, null, null, null, "EARLIEST_SLOT", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        // Clinic B should be before Clinic A
        ClinicResponse first = result.getContent().get(0);
        ClinicResponse second = result.getContent().get(1);
        assertThat(first.getId()).isEqualTo(clinicB.getId());
        assertThat(second.getId()).isEqualTo(clinicA.getId());
        assertThat(first.getEarliestAvailableSlot()).isBefore(second.getEarliestAvailableSlot());
    }

    @Test
    @DisplayName("T134-07: Earliest slot ignores past slots today and non-available slots")
    void testEarliestSlotIgnoresPastOrNonAvailableSlots() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Slot earlier today (already passed) -> Should be ignored
        if (now.isAfter(LocalTime.of(1, 0))) {
            appointmentSlotRepository.save(AppointmentSlot.builder()
                    .doctor(doctorA1)
                    .appointmentDate(today)
                    .startTime(LocalTime.MIN)
                    .endTime(LocalTime.MIN.plusMinutes(30))
                    .status(SlotStatus.AVAILABLE)
                    .build());
        }

        // Slot booked tomorrow -> Should be ignored
        appointmentSlotRepository.save(AppointmentSlot.builder()
                .doctor(doctorA1)
                .appointmentDate(today.plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.BOOKED)
                .build());

        PageResponse<ClinicResponse> result = clinicService.getClinics("Hoàn Mỹ", null, null, null, null, "DEFAULT", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEarliestAvailableSlot()).isNull();
    }

    @Test
    @DisplayName("T134-08: Clinic with no future available slots appears last in EARLIEST_SLOT sort")
    void testClinicWithNoSlotsNullsLast() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1);

        // Only Clinic A has slot
        appointmentSlotRepository.save(AppointmentSlot.builder()
                .doctor(doctorA1)
                .appointmentDate(tomorrow)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .build());

        PageResponse<ClinicResponse> result = clinicService.getClinics(null, null, null, null, null, "EARLIEST_SLOT", PageRequest.of(0, 10));

        List<ClinicResponse> list = result.getContent();
        assertThat(list.get(0).getId()).isEqualTo(clinicA.getId());
        assertThat(list.get(0).getEarliestAvailableSlot()).isNotNull();

        // Clinic B has no slots -> appears after Clinic A and earliestAvailableSlot is null
        ClinicResponse clinicBRes = list.stream().filter(c -> c.getId().equals(clinicB.getId())).findFirst().orElseThrow();
        assertThat(clinicBRes.getEarliestAvailableSlot()).isNull();
    }
}
