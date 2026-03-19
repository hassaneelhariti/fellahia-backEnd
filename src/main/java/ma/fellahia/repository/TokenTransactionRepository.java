package ma.fellahia.repository;

import ma.fellahia.domain.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, UUID> {

    List<TokenTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
