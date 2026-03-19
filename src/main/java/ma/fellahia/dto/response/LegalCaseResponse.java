package ma.fellahia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ma.fellahia.domain.CaseStatus;
import ma.fellahia.domain.CaseUrgency;
import ma.fellahia.domain.LegalCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegalCaseResponse {

    private UUID id;
    private String reference;
    private String description;
    private CaseUrgency urgency;
    private CaseStatus status;
    private BigDecimal cost;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String fellahName;
    private String lawyerName;

    private List<CaseFileResponse> files;

    public static LegalCaseResponse from(LegalCase c) {
        return LegalCaseResponse.builder()
                .id(c.getId())
                .reference(c.getReference())
                .description(c.getDescription())
                .urgency(c.getUrgency())
                .status(c.getStatus())
                .cost(c.getCost())
                .region(c.getRegion())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .fellahName(c.getFellah() != null ? c.getFellah().getFullName() : null)
                .lawyerName(c.getLawyer() != null ? c.getLawyer().getFullName() : null)
                .files(c.getFiles() != null
                        ? c.getFiles().stream().map(CaseFileResponse::from).toList()
                        : List.of())
                .build();
    }
}
