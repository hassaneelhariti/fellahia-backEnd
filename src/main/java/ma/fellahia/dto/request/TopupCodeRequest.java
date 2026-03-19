package ma.fellahia.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopupCodeRequest(@NotNull
                               @NotNull(message = "المبلغ يجب أن يكون أكبر من 7")
                               String code,

                               @NotNull
                               String method) {
}
