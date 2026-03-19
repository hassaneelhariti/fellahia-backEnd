package ma.fellahia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "الرسالة مطلوبة")
    @Size(max = 2000, message = "الرسالة طويلة جداً")
    private String message;
}
