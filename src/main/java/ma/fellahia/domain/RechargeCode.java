package ma.fellahia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class RechargeCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, length = 7)
    private String code;
    private BigDecimal amount;
}
