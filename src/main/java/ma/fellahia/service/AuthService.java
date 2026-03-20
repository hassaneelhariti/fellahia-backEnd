package ma.fellahia.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fellahia.domain.*;
import ma.fellahia.dto.request.LoginRequest;
import ma.fellahia.dto.request.OtpVerifyRequest;
import ma.fellahia.dto.request.RegisterRequest;
import ma.fellahia.dto.response.AuthResponse;
import ma.fellahia.exception.CustomExceptions.PhoneAlreadyExistsException;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.FellahProfileRepository;
import ma.fellahia.repository.LawyerProfileRepository;
import ma.fellahia.repository.UserRepository;
import ma.fellahia.security.JwtTokenProvider;
import ma.fellahia.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FellahProfileRepository fellahProfileRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new PhoneAlreadyExistsException(req.getPhone());
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .verified(false)
                .build();
        user = userRepository.save(user);

        // Create role-specific profile
        if (req.getRole() == UserRole.FELLAH) {
            fellahProfileRepository.save(
                    FellahProfile.builder().user(user).build());
        } else {
            lawyerProfileRepository.save(
                    LawyerProfile.builder().user(user).build());
        }

        String token = jwtTokenProvider.generateTokenForUser(
                user.getId(), user.getPhone(), user.getRole().name());

        return buildAuthResponse(user, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getPhone(), req.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtTokenProvider.generateToken(auth);
        return buildAuthResponse(user, token);
    }

    @Transactional
    public void verifyOtp(OtpVerifyRequest req) {
        User user = userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        otpService.verifyOtp(user.getId(), req.getCode());

        user.setVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void resendOtp(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));
        otpService.sendOtp(user.getId(), phone);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .verified(user.isVerified())
                .build();
    }
}
