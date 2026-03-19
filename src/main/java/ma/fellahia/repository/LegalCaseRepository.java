package ma.fellahia.repository;

import ma.fellahia.domain.CaseStatus;
import ma.fellahia.domain.LegalCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, UUID> {

    Page<LegalCase> findByFellahId(UUID fellahId, Pageable pageable);

    Page<LegalCase> findByStatus(CaseStatus status, Pageable pageable);

    Page<LegalCase> findByLawyerId(UUID lawyerId, Pageable pageable);

    Optional<LegalCase> findByReference(String reference);

    long countByLawyerIdAndStatus(UUID lawyerId, CaseStatus status);

    @Query("SELECT COUNT(c) FROM LegalCase c WHERE c.lawyer.id = :lawyerId")
    long countByLawyerId(UUID lawyerId);
}
