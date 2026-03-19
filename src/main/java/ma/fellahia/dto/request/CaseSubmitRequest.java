package ma.fellahia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ma.fellahia.domain.CaseUrgency;

@Data
public class CaseSubmitRequest {

    @NotBlank(message = "وصف القضية مطلوب")
    @Size(min = 20, max = 3000, message = "الوصف يجب أن يكون بين 20 و 3000 حرف")
    private String description;

    @NotNull(message = "درجة الأولوية مطلوبة")
    private CaseUrgency urgency;

    @Size(max = 100)
    private String region;
}
