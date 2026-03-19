package ma.fellahia.dto.response;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private java.util.List<String> authorities;
}
