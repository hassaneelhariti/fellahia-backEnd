package ma.fellahia.repository;

import ma.fellahia.domain.FellahProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TopupRepository extends JpaRepository<FellahProfile, UUID> {
    Optional<FellahProfile> findByUserId(UUID userId);
}