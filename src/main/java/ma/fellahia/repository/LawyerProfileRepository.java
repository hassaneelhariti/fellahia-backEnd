package ma.fellahia.repository;

import ma.fellahia.domain.LawyerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LawyerProfileRepository extends JpaRepository<LawyerProfile, UUID> {

    Optional<LawyerProfile> findByUserId(UUID userId);
}
