package ma.fellahia.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fellahia.domain.OtpCode;
import ma.fellahia.exception.CustomExceptions.InvalidOtpException;
import ma.fellahia.repository.OtpCodeRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendOtp(UUID userId, String destination) {
        String code = String.format("%06d", random.nextInt(999_999));

        OtpCode otp = OtpCode.builder()
                .userId(userId)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        otpCodeRepository.save(otp);

        // In production, replace with SMS gateway (e.g. Twilio, Orange SMS)
        sendByEmail(destination, code);

        log.info("OTP sent to {} for userId {}", destination, userId);
    }

    @Transactional
    public void verifyOtp(UUID userId, String code) {
        OtpCode otp = otpCodeRepository
                .findTopByUserIdAndCodeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        userId, code, LocalDateTime.now())
                .orElseThrow(InvalidOtpException::new);

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }

    private void sendByEmail(String email, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("FellahIA – رمز التحقق");
            msg.setText("رمز التحقق الخاص بك هو: " + code +
                    "\nصالح لمدة " + OTP_EXPIRY_MINUTES + " دقائق.");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
        }
    }

    // Cleanup expired OTPs every hour
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgeExpiredOtps() {
        otpCodeRepository.deleteExpired(LocalDateTime.now().minusMinutes(5));
    }
}
