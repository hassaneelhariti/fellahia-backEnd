package ma.fellahia.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TopupRequest(
        @NotNull
        @DecimalMin(value = "1.00", message = "المبلغ يجب أن يكون أكبر من 0")
        BigDecimal amount,

        @NotNull
        String method
) {}