package ma.fellahia.controller;

import lombok.RequiredArgsConstructor;
import ma.fellahia.dto.response.BalanceResponse;
import ma.fellahia.dto.response.TransactionResponse;
import ma.fellahia.dto.response.UserProfileResponse;
import ma.fellahia.security.UserDetailsImpl;
import ma.fellahia.service.TokenService;
import ma.fellahia.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * GET /api/users/me
     * Returns the authenticated user's full profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    /**
     * GET /api/users/me/balance
     * Returns the Fellah's token balance (Fellah role only in practice).
     */
    @GetMapping("/me/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(tokenService.getBalance(userDetails.getId()));
    }

    /**
     * GET /api/users/me/transactions
     * Returns the Fellah's token transaction history.
     */
    @GetMapping("/me/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(tokenService.getTransactions(userDetails.getId()));
    }
}
