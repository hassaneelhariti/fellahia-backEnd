package ma.fellahia.service;

import ma.fellahia.domain.OtpCode;
import ma.fellahia.exception.CustomExceptions.InvalidOtpException;
import ma.fellahia.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock OtpCodeRepository otpCodeRepository;
    @Mock JavaMailSender mailSender;

    @InjectMocks OtpService otpService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // ── sendOtp ───────────────────────────────────────────────────────────────

    @Test
    void sendOtp_shouldSaveOtpCode_andSendEmail() {
        OtpCode savedOtp = OtpCode.builder()
                .userId(userId).code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(otpCodeRepository.save(any(OtpCode.class))).thenReturn(savedOtp);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        otpService.sendOtp(userId, "test@email.com");

        verify(otpCodeRepository).save(any(OtpCode.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtp_shouldNotThrow_whenEmailFails() {
        when(otpCodeRepository.save(any(OtpCode.class)))
                .thenReturn(OtpCode.builder().build());
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatNoException().isThrownBy(() ->
                otpService.sendOtp(userId, "test@email.com"));
    }

    // ── verifyOtp ─────────────────────────────────────────────────────────────

    @Test
    void verifyOtp_shouldMarkOtpAsUsed_whenValid() {
        OtpCode otp = OtpCode.builder()
                .userId(userId).code("123456").used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpCodeRepository
                .findTopByUserIdAndCodeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        eq(userId), eq("123456"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(otp));
        when(otpCodeRepository.save(any(OtpCode.class))).thenReturn(otp);

        otpService.verifyOtp(userId, "123456");

        assertThat(otp.isUsed()).isTrue();
        verify(otpCodeRepository).save(otp);
    }

    @Test
    void verifyOtp_shouldThrow_whenOtpNotFound() {
        when(otpCodeRepository
                .findTopByUserIdAndCodeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.verifyOtp(userId, "000000"))
                .isInstanceOf(InvalidOtpException.class);
    }

    // ── purgeExpiredOtps ──────────────────────────────────────────────────────

    @Test
    void purgeExpiredOtps_shouldCallDeleteExpired() {
        doNothing().when(otpCodeRepository).deleteExpired(any(LocalDateTime.class));

        otpService.purgeExpiredOtps();

        verify(otpCodeRepository).deleteExpired(any(LocalDateTime.class));
    }
}
