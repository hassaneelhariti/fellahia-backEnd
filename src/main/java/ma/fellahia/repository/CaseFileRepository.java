package ma.fellahia.repository;

import ma.fellahia.domain.CaseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseFileRepository extends JpaRepository<CaseFile, UUID> {

    List<CaseFile> findByLegalCaseId(UUID caseId);
}
