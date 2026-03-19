package ma.fellahia.repository;

import ma.fellahia.domain.FellahProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FellahProfileRepository extends JpaRepository<FellahProfile, UUID> {

    Optional<FellahProfile> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE FellahProfile fp SET fp.balance = fp.balance - :amount WHERE fp.user.id = :userId AND fp.balance >= :amount")
    int deductBalance(UUID userId, BigDecimal amount);
}
