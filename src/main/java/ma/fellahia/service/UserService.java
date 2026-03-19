package ma.fellahia.service;

import lombok.RequiredArgsConstructor;
import ma.fellahia.domain.User;
import ma.fellahia.domain.UserRole;
import ma.fellahia.dto.response.LawyerDashboardResponse;
import ma.fellahia.dto.response.UserProfileResponse;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.FellahProfileRepository;
import ma.fellahia.repository.LawyerProfileRepository;
import ma.fellahia.repository.LegalCaseRepository;
import ma.fellahia.repository.UserRepository;
import ma.fellahia.domain.CaseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FellahProfileRepository fellahProfileRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final LegalCaseRepository legalCaseRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .verified(user.isVerified())
                .createdAt(user.getCreatedAt());

        if (user.getRole() == UserRole.FELLAH) {
            fellahProfileRepository.findByUserId(userId).ifPresent(p -> {
                builder.balance(p.getBalance());
                builder.rib(p.getRib());
            });
        } else {
            lawyerProfileRepository.findByUserId(userId).ifPresent(p -> {
                builder.barNumber(p.getBarNumber());
                builder.specialization(p.getSpecialization());
                builder.region(p.getRegion());
                builder.rating(p.getRating());
                builder.totalCases(p.getTotalCases());
            });
        }

        return builder.build();
    }

    @Transactional(readOnly = true)
    public LawyerDashboardResponse getLawyerDashboard(UUID lawyerId) {
        User user = userRepository.findById(lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        long total    = legalCaseRepository.countByLawyerId(lawyerId);
        long pending  = legalCaseRepository.countByLawyerIdAndStatus(lawyerId, CaseStatus.PENDING);
        long accepted = legalCaseRepository.countByLawyerIdAndStatus(lawyerId, CaseStatus.ACCEPTED);
        long closed   = legalCaseRepository.countByLawyerIdAndStatus(lawyerId, CaseStatus.CLOSED);

        return LawyerDashboardResponse.builder()
                .fullName(user.getFullName())
                .totalCases(total)
                .pendingCases(pending)
                .acceptedCases(accepted)
                .closedCases(closed)
                .rating(lawyerProfileRepository.findByUserId(lawyerId)
                        .map(p -> p.getRating())
                        .orElse(null))
                .build();
    }
}
