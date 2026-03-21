package ma.fellahia.service;

import ma.fellahia.domain.FellahProfile;
import ma.fellahia.domain.TokenTransaction;
import ma.fellahia.dto.response.BalanceResponse;
import ma.fellahia.exception.CustomExceptions.InsufficientBalanceException;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.FellahProfileRepository;
import ma.fellahia.repository.TokenTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock FellahProfileRepository profileRepository;
    @Mock TokenTransactionRepository transactionRepository;

    @InjectMocks TokenService tokenService;

    private UUID userId;
    private FellahProfile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profile = FellahProfile.builder()
                .balance(BigDecimal.valueOf(500))
                .build();
    }

    // ── getBalance ────────────────────────────────────────────────────────────

    @Test
    void getBalance_shouldReturnBalance_whenProfileExists() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        BalanceResponse response = tokenService.getBalance(userId);

        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void getBalance_shouldThrow_whenProfileNotFound() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.getBalance(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── debit ─────────────────────────────────────────────────────────────────

    @Test
    void debit_shouldDeductBalance_andSaveTransaction() {
        when(profileRepository.deductBalance(userId, BigDecimal.valueOf(150))).thenReturn(1);
        when(transactionRepository.save(any(TokenTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        tokenService.debit(userId, BigDecimal.valueOf(150), "طلب قانوني");

        verify(profileRepository).deductBalance(userId, BigDecimal.valueOf(150));
        verify(transactionRepository).save(any(TokenTransaction.class));
    }

    @Test
    void debit_shouldThrow_whenInsufficientBalance() {
        when(profileRepository.deductBalance(userId, BigDecimal.valueOf(150))).thenReturn(0);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> tokenService.debit(userId, BigDecimal.valueOf(150), "طلب"))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void debit_shouldThrow_whenProfileNotFound_afterFailedDeduct() {
        when(profileRepository.deductBalance(userId, BigDecimal.valueOf(150))).thenReturn(0);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.debit(userId, BigDecimal.valueOf(150), "طلب"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── credit ────────────────────────────────────────────────────────────────

    @Test
    void credit_shouldAddBalance_andSaveTransaction() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(FellahProfile.class))).thenReturn(profile);
        when(transactionRepository.save(any(TokenTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        tokenService.credit(userId, BigDecimal.valueOf(200), "شحن رصيد");

        assertThat(profile.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
        verify(profileRepository).save(profile);
        verify(transactionRepository).save(any(TokenTransaction.class));
    }

    @Test
    void credit_shouldThrow_whenProfileNotFound() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.credit(userId, BigDecimal.valueOf(200), "شحن"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getTransactions ───────────────────────────────────────────────────────

    @Test
    void getTransactions_shouldReturnList() {
        TokenTransaction tx = TokenTransaction.builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(150).negate())
                .type("DEBIT")
                .description("طلب قانوني")
                .build();

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(tx));

        var result = tokenService.getTransactions(userId);

        assertThat(result).hasSize(1);
    }
}
