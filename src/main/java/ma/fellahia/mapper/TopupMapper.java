package ma.fellahia.mapper;

import ma.fellahia.domain.TokenTransaction;
import ma.fellahia.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TopupMapper {

    public TransactionResponse toResponse(TokenTransaction tx) {
        return TransactionResponse.builder().id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}