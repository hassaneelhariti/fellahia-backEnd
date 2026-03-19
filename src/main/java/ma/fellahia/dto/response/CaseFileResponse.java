package ma.fellahia.dto.response;

import lombok.Builder;
import lombok.Data;
import ma.fellahia.domain.CaseFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CaseFileResponse {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long sizeBytes;
    private String downloadUrl;
    private LocalDateTime uploadedAt;

    public static CaseFileResponse from(CaseFile f) {
        return CaseFileResponse.builder()
                .id(f.getId())
                .fileName(f.getFileName())
                .fileType(f.getFileType())
                .sizeBytes(f.getSizeBytes())
                .uploadedAt(f.getUploadedAt())
                .build();
    }
}
