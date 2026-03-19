package ma.fellahia.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LawyerDashboardResponse {
    private String fullName;
    private long totalCases;
    private long pendingCases;
    private long acceptedCases;
    private long closedCases;
    private BigDecimal rating;
}
