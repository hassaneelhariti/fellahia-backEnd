package ma.fellahia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ma.fellahia.domain.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String email;
    private UserRole role;
    private boolean verified;
    private LocalDateTime createdAt;

    // Fellah-specific
    private BigDecimal balance;
    private String rib;

    // Lawyer-specific
    private String barNumber;
    private String specialization;
    private String region;
    private BigDecimal rating;
    private Integer totalCases;
}
