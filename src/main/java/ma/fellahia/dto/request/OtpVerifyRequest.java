package ma.fellahia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotBlank(message = "رقم الهاتف مطلوب")
    private String phone;

    @NotBlank(message = "الرمز مطلوب")
    @Size(min = 6, max = 6, message = "الرمز يجب أن يكون 6 أرقام")
    @Pattern(regexp = "\\d{6}", message = "الرمز يجب أن يحتوي على أرقام فقط")
    private String code;
}
