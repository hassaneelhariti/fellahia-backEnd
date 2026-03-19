package ma.fellahia.repository;

import ma.fellahia.domain.RechargeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepository extends JpaRepository<RechargeCode,Long> {
    RechargeCode findByCode(String code);
}
