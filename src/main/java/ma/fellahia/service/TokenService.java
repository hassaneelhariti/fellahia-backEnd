package ma.fellahia.service;

import lombok.RequiredArgsConstructor;
import ma.fellahia.domain.FellahProfile;
import ma.fellahia.domain.TokenTransaction;
import ma.fellahia.dto.response.BalanceResponse;
import ma.fellahia.dto.response.TransactionResponse;
import ma.fellahia.exception.CustomExceptions.InsufficientBalanceException;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.FellahProfileRepository;
import ma.fellahia.repository.TokenTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final FellahProfileRepository profileRepository;
    private final TokenTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID userId) {
        FellahProfile profile = getProfile(userId);
        return new BalanceResponse(profile.getBalance());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * Deducts tokens from a Fellah's balance atomically.
     * Throws InsufficientBalanceException if balance is too low.
     */
    @Transactional
    public void debit(UUID userId, BigDecimal amount, String description) {
        int updated = profileRepository.deductBalance(userId, amount);
        if (updated == 0) {
            FellahProfile profile = getProfile(userId);
            throw new InsufficientBalanceException(
                    "رصيدك غير كافي. الرصيد الحالي: " + profile.getBalance() +
                    " درهم، المطلوب: " + amount + " درهم");
        }

        transactionRepository.save(TokenTransaction.builder()
                .userId(userId)
                .amount(amount.negate())
                .type("DEBIT")
                .description(description)
                .build());
    }

    /**
     * Adds tokens to a Fellah's balance (e.g., recharge).
     */
    @Transactional
    public void credit(UUID userId, BigDecimal amount, String description) {
        FellahProfile profile = getProfile(userId);
        profile.setBalance(profile.getBalance().add(amount));
        profileRepository.save(profile);

        transactionRepository.save(TokenTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type("CREDIT")
                .description(description)
                .build());
    }

    private FellahProfile getProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ملف الفلاح غير موجود للمستخدم: " + userId));
    }
}
