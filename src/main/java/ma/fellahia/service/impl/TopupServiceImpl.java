package ma.fellahia.service.impl;

import ma.fellahia.domain.FellahProfile;
import ma.fellahia.domain.RechargeCode;
import ma.fellahia.domain.TokenTransaction;
import ma.fellahia.dto.request.TopupCodeRequest;
import ma.fellahia.dto.request.TopupRequest;
import ma.fellahia.dto.response.BalanceResponse;
import ma.fellahia.dto.response.TransactionResponse;
import ma.fellahia.exception.CustomExceptions;
import ma.fellahia.exception.ResourceNotFoundException;
import ma.fellahia.mapper.TopupMapper;
import ma.fellahia.repository.CodeRepository;
import ma.fellahia.repository.TokenTransactionRepository;
import ma.fellahia.repository.TopupRepository;
import lombok.RequiredArgsConstructor;
import ma.fellahia.service.TopupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopupServiceImpl implements TopupService {

    private final TopupRepository topupRepository;
    private final TokenTransactionRepository transactionRepository;
    private final TopupMapper topupMapper;
    private final CodeRepository codeRepository;
    @Transactional(readOnly = true)
    @Override
    public BalanceResponse getBalance(UUID userId) {
        FellahProfile profile = findProfile(userId);
        return new BalanceResponse(profile.getBalance());
    }
    @Transactional
    @Override
    public BalanceResponse topup(UUID userId, TopupRequest request) {
        FellahProfile profile = findProfile(userId);
        profile.setBalance(profile.getBalance().add(request.amount()));
        topupRepository.save(profile);

        TokenTransaction tx = TokenTransaction.builder()
                .userId(userId)
                .amount(request.amount())
                .type("CREDIT")
                .description("شحن رصيد عبر " + request.method())
                .build();

        transactionRepository.save(tx);
        return new BalanceResponse(profile.getBalance());
    }
    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponse> getTransactions(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(topupMapper::toResponse)
                .toList();
    }
    @Transactional
    @Override
    public BalanceResponse topupcode(UUID id, TopupCodeRequest request) {
        String code = request.code();
        RechargeCode rechargeCode=codeRepository.findByCode(code);

        if(rechargeCode!=null){
            FellahProfile profile = findProfile(id);
            profile.setBalance(profile.getBalance().add(rechargeCode.getAmount()));
            topupRepository.save(profile);
            TokenTransaction tx = TokenTransaction.builder()
                    .userId(id)
                    .amount(rechargeCode.getAmount())
                    .type("CREDIT")
                    .description("شحن رصيد عبر " + request.method()+ " "+ rechargeCode.getAmount())
                    .build();
            transactionRepository.save(tx);
            return new BalanceResponse(profile.getBalance());
        }
        throw new CustomExceptions.BusinessException("Invalid Code");


    }

    private FellahProfile findProfile(UUID userId) {
        return topupRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
    }
}
