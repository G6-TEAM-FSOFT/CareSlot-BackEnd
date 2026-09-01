package com.org.care_slot;

import com.org.care_slot.dto.request.PatientProfileCreateRequest;
import com.org.care_slot.dto.response.PatientProfileResponse;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.entity.User;
import com.org.care_slot.enums.ProfileType;
import com.org.care_slot.enums.RoleType;
import com.org.care_slot.repository.PatientProfileRepository;
import com.org.care_slot.repository.UserRepository;
import com.org.care_slot.service.PatientProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PatientProfileConstraintTest {

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientProfileService patientProfileService;

    private User getOrCreateTestUser(String email, String fullName, String phone) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .fullName(fullName)
                        .phone(phone)
                        .passwordHash("$2a$10$dummyHashForTestingConstraints")
                        .role(RoleType.PATIENT)
                        .status("ACTIVE")
                        .build()));
    }

    @Test
    @Order(1)
    @DisplayName("1. Migration V3: Seed data in DB is migrated correctly with PRIMARY and FAMILY types")
    void testExistingSeedDataMigratedCorrectly() {
        // User 3 (id=1 is SELF -> PRIMARY, id=2 is CHILD -> FAMILY, id=3 is MOTHER -> FAMILY)
        Optional<PatientProfile> profile1 = patientProfileRepository.findById(1L);
        assertThat(profile1).isPresent();
        assertThat(profile1.get().getProfileType()).isEqualTo(ProfileType.PRIMARY);
        assertThat(profile1.get().getRelationship()).isEqualTo("SELF");
        assertThat(profile1.get().getUser().getId()).isEqualTo(3L);

        Optional<PatientProfile> profile2 = patientProfileRepository.findById(2L);
        assertThat(profile2).isPresent();
        assertThat(profile2.get().getProfileType()).isEqualTo(ProfileType.FAMILY);
        assertThat(profile2.get().getRelationship()).isEqualTo("CHILD");

        Optional<PatientProfile> profile3 = patientProfileRepository.findById(3L);
        assertThat(profile3).isPresent();
        assertThat(profile3.get().getProfileType()).isEqualTo(ProfileType.FAMILY);
        assertThat(profile3.get().getRelationship()).isEqualTo("MOTHER");

        // User 4 (id=4 is SELF -> PRIMARY)
        Optional<PatientProfile> profile4 = patientProfileRepository.findById(4L);
        assertThat(profile4).isPresent();
        assertThat(profile4.get().getProfileType()).isEqualTo(ProfileType.PRIMARY);
        assertThat(profile4.get().getRelationship()).isEqualTo("SELF");
        assertThat(profile4.get().getUser().getId()).isEqualTo(4L);
    }

    @Test
    @Order(2)
    @DisplayName("2. A user can create a single PRIMARY profile")
    @Transactional
    void testUserCanCreateOnePrimaryProfile() {
        User user = getOrCreateTestUser("test_patient_1@careslot.local", "Bệnh Nhân Test 1", "0911000001");

        PatientProfile primaryProfile = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.PRIMARY)
                .fullName("Bệnh Nhân Test 1 (Chính)")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .phone("0911000001")
                .relationship("SELF")
                .status("ACTIVE")
                .build();

        PatientProfile saved = patientProfileRepository.saveAndFlush(primaryProfile);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProfileType()).isEqualTo(ProfileType.PRIMARY);
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @Order(3)
    @DisplayName("3. Database constraint rejects a second PRIMARY profile for the same user")
    @Transactional
    void testUserCannotHaveTwoPrimaryProfiles() {
        User user = getOrCreateTestUser("test_patient_duplicate@careslot.local", "Bệnh Nhân Trùng", "0911000002");

        // First PRIMARY profile
        PatientProfile primary1 = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.PRIMARY)
                .fullName("Bệnh Nhân Trùng 1")
                .relationship("SELF")
                .status("ACTIVE")
                .build();
        patientProfileRepository.saveAndFlush(primary1);

        // Second PRIMARY profile for the SAME user -> Must fail at DB level due to uq_patient_profiles_user_primary
        PatientProfile primary2 = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.PRIMARY)
                .fullName("Bệnh Nhân Trùng 2")
                .relationship("SELF")
                .status("ACTIVE")
                .build();

        assertThatThrownBy(() -> {
            patientProfileRepository.saveAndFlush(primary2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Order(4)
    @DisplayName("4. A user can have multiple FAMILY profiles")
    @Transactional
    void testUserCanHaveMultipleFamilyProfiles() {
        User user = getOrCreateTestUser("test_patient_family@careslot.local", "Bệnh Nhân Gia Đình", "0911000003");

        // Primary profile
        PatientProfile primary = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.PRIMARY)
                .fullName("Chủ Hộ")
                .relationship("SELF")
                .status("ACTIVE")
                .build();
        patientProfileRepository.saveAndFlush(primary);

        // Multiple FAMILY profiles
        PatientProfile child = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.FAMILY)
                .fullName("Con Gái")
                .relationship("CHILD")
                .status("ACTIVE")
                .build();

        PatientProfile mother = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.FAMILY)
                .fullName("Mẹ Đẻ")
                .relationship("MOTHER")
                .status("ACTIVE")
                .build();

        PatientProfile spouse = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.FAMILY)
                .fullName("Vợ")
                .relationship("SPOUSE")
                .status("ACTIVE")
                .build();

        PatientProfile savedChild = patientProfileRepository.saveAndFlush(child);
        PatientProfile savedMother = patientProfileRepository.saveAndFlush(mother);
        PatientProfile savedSpouse = patientProfileRepository.saveAndFlush(spouse);

        assertThat(savedChild.getId()).isNotNull();
        assertThat(savedChild.getProfileType()).isEqualTo(ProfileType.FAMILY);
        assertThat(savedMother.getId()).isNotNull();
        assertThat(savedMother.getProfileType()).isEqualTo(ProfileType.FAMILY);
        assertThat(savedSpouse.getId()).isNotNull();
        assertThat(savedSpouse.getProfileType()).isEqualTo(ProfileType.FAMILY);
    }

    @Test
    @Order(5)
    @DisplayName("5. Two different users can each have their own PRIMARY profile")
    @Transactional
    void testTwoDifferentUsersCanHavePrimaryProfiles() {
        User userA = getOrCreateTestUser("user_a@careslot.local", "User A", "0911000004");
        User userB = getOrCreateTestUser("user_b@careslot.local", "User B", "0911000005");

        PatientProfile primaryA = PatientProfile.builder()
                .user(userA)
                .profileType(ProfileType.PRIMARY)
                .fullName("User A Profile")
                .relationship("SELF")
                .status("ACTIVE")
                .build();

        PatientProfile primaryB = PatientProfile.builder()
                .user(userB)
                .profileType(ProfileType.PRIMARY)
                .fullName("User B Profile")
                .relationship("SELF")
                .status("ACTIVE")
                .build();

        PatientProfile savedA = patientProfileRepository.saveAndFlush(primaryA);
        PatientProfile savedB = patientProfileRepository.saveAndFlush(primaryB);

        assertThat(savedA.getId()).isNotNull();
        assertThat(savedA.getProfileType()).isEqualTo(ProfileType.PRIMARY);
        assertThat(savedA.getUser().getId()).isEqualTo(userA.getId());

        assertThat(savedB.getId()).isNotNull();
        assertThat(savedB.getProfileType()).isEqualTo(ProfileType.PRIMARY);
        assertThat(savedB.getUser().getId()).isEqualTo(userB.getId());
    }

    @Test
    @Order(6)
    @DisplayName("6. Profile ownership is strictly controlled by user_id")
    @Transactional
    void testProfileOwnershipControlledByUserId() {
        User user = getOrCreateTestUser("user_ownership@careslot.local", "User Ownership", "0911000006");

        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .profileType(ProfileType.PRIMARY)
                .fullName("Tên Bất Kỳ Không Phải Tên User")
                .phone("0999888777")
                .relationship("SELF")
                .status("ACTIVE")
                .build();

        PatientProfile saved = patientProfileRepository.saveAndFlush(profile);

        // Verification: Profile is bound by user.id, not fullName or phone
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        List<PatientProfile> userProfiles = patientProfileRepository.findByUserIdAndKeyword(user.getId(), null);
        assertThat(userProfiles).extracting(PatientProfile::getId).contains(saved.getId());

        // Check repository finder by profile type
        Optional<PatientProfile> primaryFound = patientProfileRepository.findByUserIdAndProfileTypeAndStatus(
                user.getId(), ProfileType.PRIMARY, "ACTIVE"
        );
        assertThat(primaryFound).isPresent();
        assertThat(primaryFound.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @Order(7)
    @DisplayName("7. PatientProfileService creates PRIMARY for SELF and FAMILY for other relationships")
    @Transactional
    void testPatientProfileServiceCreateAndResponse() {
        User user = getOrCreateTestUser("user_service_test@careslot.local", "User Service", "0911000007");

        // 1. Create with relationship = SELF
        PatientProfileCreateRequest primaryReq = PatientProfileCreateRequest.builder()
                .fullName("Hồ Sơ Chính Service")
                .dateOfBirth(LocalDate.of(1992, 5, 20))
                .gender("FEMALE")
                .phone("0911000007")
                .relationship("SELF")
                .build();

        PatientProfileResponse primaryRes = patientProfileService.createPatientProfile(user.getId(), primaryReq);
        assertThat(primaryRes.getId()).isNotNull();
        assertThat(primaryRes.getUserId()).isEqualTo(user.getId());
        assertThat(primaryRes.getProfileType()).isEqualTo(ProfileType.PRIMARY);
        assertThat(primaryRes.getRelationship()).isEqualTo("SELF");

        // 2. Create with relationship = CHILD
        PatientProfileCreateRequest familyReq = PatientProfileCreateRequest.builder()
                .fullName("Con Của Service")
                .dateOfBirth(LocalDate.of(2020, 8, 15))
                .gender("MALE")
                .relationship("CHILD")
                .build();

        PatientProfileResponse familyRes = patientProfileService.createPatientProfile(user.getId(), familyReq);
        assertThat(familyRes.getId()).isNotNull();
        assertThat(familyRes.getUserId()).isEqualTo(user.getId());
        assertThat(familyRes.getProfileType()).isEqualTo(ProfileType.FAMILY);
        assertThat(familyRes.getRelationship()).isEqualTo("CHILD");
    }
}
