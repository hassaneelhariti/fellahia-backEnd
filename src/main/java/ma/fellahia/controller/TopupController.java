package ma.fellahia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fellahia.dto.request.TopupCodeRequest;
import ma.fellahia.dto.request.TopupRequest;
import ma.fellahia.dto.response.BalanceResponse;
import ma.fellahia.dto.response.TransactionResponse;
import ma.fellahia.security.UserDetailsImpl;
import ma.fellahia.service.TopupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topup")
@RequiredArgsConstructor
public class TopupController {

    private final TopupService topupService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(topupService.getBalance(userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<BalanceResponse> topup(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TopupRequest request
    ) {
        return ResponseEntity.ok(topupService.topup(userDetails.getId(), request));
    }

    @PostMapping("/code")
    public ResponseEntity<BalanceResponse> topupCode(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TopupCodeRequest request
    ) {
        return ResponseEntity.ok(topupService.topupcode(userDetails.getId(), request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(topupService.getTransactions(userDetails.getId()));
    }
}