package ma.fellahia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "رقم الهاتف مطلوب")
    private String phone;

    @NotBlank(message = "كلمة السر مطلوبة")
    private String password;
}
