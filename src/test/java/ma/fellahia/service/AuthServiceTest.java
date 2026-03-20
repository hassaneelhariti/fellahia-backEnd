package ma.fellahia.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock FellahProfileRepository fellahProfileRepository;
    @Mock LawyerProfileRepository lawyerProfileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock AuthenticationManager authenticationManager;
    @Mock OtpService otpService;
    @InjectMocks AuthService authService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId).fullName("Hassan El Hariti")
                .phone("0612345678").password("encodedPassword")
                .role(UserRole.FELLAH).verified(false).build();
    }

    @Test
    void register_shouldCreateFellahUser_andReturnToken() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Hassan El Hariti");
        req.setPhone("0612345678");
        req.setPassword("password123");
        req.setRole(UserRole.FELLAH);

        when(userRepository.existsByPhone(req.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtTokenProvider.generateTokenForUser(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(UserRole.FELLAH);
        verify(fellahProfileRepository).save(any(FellahProfile.class));
        verify(lawyerProfileRepository, never()).save(any());
    }

    @Test
    void register_shouldThrow_whenPhoneAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setPhone("0612345678");
        when(userRepository.existsByPhone(req.getPhone())).thenReturn(true);
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(PhoneAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequest req = new LoginRequest();
        req.setPhone("0612345678");
        req.setPassword("password123");

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(userId);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setPhone("0612345678");
        req.setPassword("password123");

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(userId);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void verifyOtp_shouldMarkUserAsVerified() {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setPhone("0612345678");
        req.setCode("123456");
        when(userRepository.findByPhone("0612345678")).thenReturn(Optional.of(mockUser));
        doNothing().when(otpService).verifyOtp(userId, "123456");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        authService.verifyOtp(req);

        assertThat(mockUser.isVerified()).isTrue();
        verify(userRepository).save(mockUser);
    }

    @Test
    void verifyOtp_shouldThrow_whenUserNotFound() {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setPhone("0000000000");
        req.setCode("123456");
        when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.verifyOtp(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resendOtp_shouldCallOtpService() {
        when(userRepository.findByPhone("0612345678")).thenReturn(Optional.of(mockUser));
        authService.resendOtp("0612345678");
        verify(otpService).sendOtp(userId, "0612345678");
    }

    @Test
    void resendOtp_shouldThrow_whenUserNotFound() {
        when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.resendOtp("0000000000"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
