package ma.fellahia.dto.request;

import jakarta.validation.constraints.NotNull;


public record TopupCodeRequest(@NotNull
                               @NotNull(message = "المبلغ يجب أن يكون أكبر من 7")
                               String code,

                               @NotNull
                               String method) {
}
