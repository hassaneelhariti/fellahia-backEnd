package ma.fellahia.service;

import jakarta.validation.Valid;
import ma.fellahia.dto.request.TopupCodeRequest;
import ma.fellahia.dto.request.TopupRequest;
import ma.fellahia.dto.response.BalanceResponse;
import ma.fellahia.dto.response.TransactionResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TopupService {
    @Transactional(readOnly = true)
    BalanceResponse getBalance(UUID userId);

    @Transactional
    BalanceResponse topup(UUID userId, TopupRequest request);

    @Transactional(readOnly = true)
    List<TransactionResponse> getTransactions(UUID userId);

    BalanceResponse topupcode(UUID id, @Valid TopupCodeRequest request);
}
