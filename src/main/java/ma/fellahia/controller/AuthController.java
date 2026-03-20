package ma.fellahia.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fellahia.dto.request.LoginRequest;
import ma.fellahia.dto.request.OtpVerifyRequest;
import ma.fellahia.dto.request.RegisterRequest;
import ma.fellahia.dto.response.AuthResponse;
import ma.fellahia.dto.response.TokenValidationResponse;
import ma.fellahia.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Authenticate and receive a JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/verify-otp
     * Verify the 6-digit OTP to activate the account.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/auth/resend-otp?phone=0600000000
     * Resend a fresh OTP to the given phone number.
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(
            @RequestParam @NotBlank String phone) {
        authService.resendOtp(phone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Token validation request from user: {}", authentication.getName());

        // Just return 200 OK to indicate token is valid
        return ResponseEntity.ok().build();
    }


    @GetMapping("/validate/details")
    public ResponseEntity<TokenValidationResponse> validateTokenWithDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            TokenValidationResponse response = TokenValidationResponse.builder()
                    .valid(true)
                    .username(userDetails.getUsername())
                    .authorities(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .build();

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).build();
    }
}
