package ma.fellahia.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks JwtTokenProvider jwtTokenProvider;
    @Mock Authentication authentication;
    @Mock UserDetailsImpl userDetails;

    private static final String SECRET =
            "ZmVsbGFoaWEtc2VjcmV0LWtleS1mb3ItZGV2LW9ubHktY2hhbmdlLWluLXByb2Q=";
    private static final long EXPIRATION = 86400000L;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", EXPIRATION);
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    void generateToken_shouldReturnValidToken() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getUsername()).thenReturn("0612345678");
        when(userDetails.getRole()).thenReturn("FELLAH");

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateTokenForUser_shouldReturnValidToken() {
        String token = jwtTokenProvider.generateTokenForUser(userId, "0612345678", "FELLAH");

        assertThat(token).isNotNull().isNotEmpty();
    }

    // ── getPhoneFromToken ─────────────────────────────────────────────────────

    @Test
    void getPhoneFromToken_shouldReturnCorrectPhone() {
        String token = jwtTokenProvider.generateTokenForUser(userId, "0612345678", "FELLAH");

        String phone = jwtTokenProvider.getPhoneFromToken(token);

        assertThat(phone).isEqualTo("0612345678");
    }

    // ── getUserIdFromToken ────────────────────────────────────────────────────

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtTokenProvider.generateTokenForUser(userId, "0612345678", "FELLAH");

        UUID extractedId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedId).isEqualTo(userId);
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtTokenProvider.generateTokenForUser(userId, "0612345678", "FELLAH");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forExpiredToken() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        String token = jwtTokenProvider.generateTokenForUser(userId, "0612345678", "FELLAH");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forMalformedToken() {
        assertThat(jwtTokenProvider.validateToken("not.a.valid.token")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forEmptyToken() {
        assertThat(jwtTokenProvider.validateToken("invalid")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forTamperedToken() {
        String token = jwtTokenProvider.generateTokenForUser(userId, "0612345678", "FELLAH");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }
}
