package ma.fellahia.dto.response;

import lombok.Builder;
import lombok.Data;
import ma.fellahia.domain.TokenTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDateTime createdAt;

    public static TransactionResponse from(TokenTransaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
