package ma.fellahia.service;

import ma.fellahia.domain.*;
import ma.fellahia.dto.response.LawyerDashboardResponse;
import ma.fellahia.dto.response.UserProfileResponse;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.FellahProfileRepository;
import ma.fellahia.repository.LawyerProfileRepository;
import ma.fellahia.repository.LegalCaseRepository;
import ma.fellahia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock FellahProfileRepository fellahProfileRepository;
    @Mock LawyerProfileRepository lawyerProfileRepository;
    @Mock LegalCaseRepository legalCaseRepository;

    @InjectMocks UserService userService;

    private UUID userId;
    private User fellahUser;
    private User lawyerUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fellahUser = User.builder()
                .id(userId).fullName("Hassan").phone("0612345678")
                .role(UserRole.FELLAH).verified(true).build();
        lawyerUser = User.builder()
                .id(userId).fullName("Lawyer Name").phone("0698765432")
                .role(UserRole.AVOCAT).verified(true).build();
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    void getProfile_shouldReturnFellahProfile_withBalance() {
        FellahProfile fellahProfile = FellahProfile.builder()
                .balance(BigDecimal.valueOf(300)).rib("123456").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(fellahUser));
        when(fellahProfileRepository.findByUserId(userId)).thenReturn(Optional.of(fellahProfile));

        UserProfileResponse response = userService.getProfile(userId);

        assertThat(response.getFullName()).isEqualTo("Hassan");
        assertThat(response.getRole()).isEqualTo(UserRole.FELLAH);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(300));
        verify(lawyerProfileRepository, never()).findByUserId(any());
    }

    @Test
    void getProfile_shouldReturnLawyerProfile_withDetails() {
        LawyerProfile lawyerProfile = LawyerProfile.builder()
                .barNumber("BAR123").specialization("Agriculture")
                .region("Fes").rating(BigDecimal.valueOf(4.5)).totalCases(10).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(lawyerUser));
        when(lawyerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(lawyerProfile));

        UserProfileResponse response = userService.getProfile(userId);

        assertThat(response.getRole()).isEqualTo(UserRole.AVOCAT);
        assertThat(response.getBarNumber()).isEqualTo("BAR123");
        assertThat(response.getSpecialization()).isEqualTo("Agriculture");
        verify(fellahProfileRepository, never()).findByUserId(any());
    }

    @Test
    void getProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProfile_shouldWork_whenNoFellahProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(fellahUser));
        when(fellahProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserProfileResponse response = userService.getProfile(userId);

        assertThat(response.getFullName()).isEqualTo("Hassan");
        assertThat(response.getBalance()).isNull();
    }

    // ── getLawyerDashboard ────────────────────────────────────────────────────

    @Test
    void getLawyerDashboard_shouldReturnCaseStats() {
        LawyerProfile lawyerProfile = LawyerProfile.builder().rating(BigDecimal.valueOf(4.5)).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(lawyerUser));
        when(legalCaseRepository.countByLawyerId(userId)).thenReturn(10L);
        when(legalCaseRepository.countByLawyerIdAndStatus(userId, CaseStatus.PENDING)).thenReturn(3L);
        when(legalCaseRepository.countByLawyerIdAndStatus(userId, CaseStatus.ACCEPTED)).thenReturn(5L);
        when(legalCaseRepository.countByLawyerIdAndStatus(userId, CaseStatus.CLOSED)).thenReturn(2L);
        when(lawyerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(lawyerProfile));

        LawyerDashboardResponse response = userService.getLawyerDashboard(userId);

        assertThat(response.getTotalCases()).isEqualTo(10L);
        assertThat(response.getPendingCases()).isEqualTo(3L);
        assertThat(response.getAcceptedCases()).isEqualTo(5L);
        assertThat(response.getClosedCases()).isEqualTo(2L);
        assertThat(response.getRating()).isEqualTo(BigDecimal.valueOf(4.5));
    }

    @Test
    void getLawyerDashboard_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getLawyerDashboard(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
