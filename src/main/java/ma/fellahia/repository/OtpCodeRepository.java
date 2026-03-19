package ma.fellahia.repository;

import ma.fellahia.domain.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findTopByUserIdAndCodeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            UUID userId, String code, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :threshold")
    void deleteExpired(LocalDateTime threshold);
}
