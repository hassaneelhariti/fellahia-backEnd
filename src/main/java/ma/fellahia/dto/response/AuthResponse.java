package ma.fellahia.dto.response;

import lombok.Builder;
import lombok.Data;
import ma.fellahia.domain.UserRole;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private UUID userId;
    private String fullName;
    private String phone;
    private UserRole role;
    private boolean verified;
}
