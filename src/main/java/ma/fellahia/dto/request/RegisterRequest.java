package ma.fellahia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ma.fellahia.domain.UserRole;

@Data
public class RegisterRequest {

    @NotBlank(message = "الاسم الكامل مطلوب")
    @Size(min = 3, max = 150, message = "الاسم يجب أن يكون بين 3 و 150 حرف")
    private String fullName;

    @NotBlank(message = "رقم الهاتف مطلوب")
    @Pattern(regexp = "^(\\+212|0)[5-7]\\d{8}$", message = "رقم الهاتف غير صحيح")
    private String phone;

    @NotBlank(message = "كلمة السر مطلوبة")
    @Size(min = 8, message = "كلمة السر يجب أن تكون 8 أحرف على الأقل")
    private String password;

    @NotNull(message = "نوع الحساب مطلوب")
    private UserRole role;
}
