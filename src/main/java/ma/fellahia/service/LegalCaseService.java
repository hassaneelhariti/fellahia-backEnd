package ma.fellahia.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fellahia.domain.*;
import ma.fellahia.dto.request.CaseSubmitRequest;
import ma.fellahia.dto.response.LegalCaseResponse;
import ma.fellahia.exception.CustomExceptions.AccessDeniedException;
import ma.fellahia.exception.CustomExceptions.BusinessException;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.CaseFileRepository;
import ma.fellahia.repository.LegalCaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalCaseService {

    private final LegalCaseRepository caseRepository;
    private final CaseFileRepository fileRepository;
    private final TokenService tokenService;
    private final StorageService storageService;

    private static final Map<CaseUrgency, BigDecimal> PRICES = Map.of(
            CaseUrgency.NORMAL,      BigDecimal.valueOf(150),
            CaseUrgency.URGENT,      BigDecimal.valueOf(350),
            CaseUrgency.VERY_URGENT, BigDecimal.valueOf(500)
    );

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp",
            "audio/webm", "audio/mpeg", "audio/wav",
            "application/pdf"
    );

    /**
     * Submits a new legal case.
     * Atomically deducts tokens and saves the case.
     * If either fails, the whole transaction rolls back.
     */
    @Transactional
    public LegalCaseResponse submit(UUID fellahId,
                                    CaseSubmitRequest req,
                                    List<MultipartFile> files) {
        BigDecimal cost = PRICES.get(req.getUrgency());

        // Deduct balance first — throws InsufficientBalanceException if not enough
        tokenService.debit(fellahId, cost,
                "طلب قانوني – " + req.getUrgency().name());

        String reference = generateReference();

        LegalCase legalCase = LegalCase.builder()
                .reference(reference)
                .fellah(userRef(fellahId))
                .description(req.getDescription())
                .urgency(req.getUrgency())
                .cost(cost)
                .region(req.getRegion())
                .status(CaseStatus.PENDING)
                .build();

        legalCase = caseRepository.save(legalCase);

        // Upload attachments
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                validateFile(file);
                String key = storageService.upload(
                        file, "cases/" + legalCase.getId());
                CaseFile cf = CaseFile.builder()
                        .legalCase(legalCase)
                        .fileName(file.getOriginalFilename())
                        .fileType(detectType(file))
                        .storageKey(key)
                        .sizeBytes(file.getSize())
                        .build();
                fileRepository.save(cf);
            }
        }

        log.info("Legal case {} created by fellah {}", reference, fellahId);
        return LegalCaseResponse.from(caseRepository.findById(legalCase.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<LegalCaseResponse> getFellahCases(UUID fellahId, Pageable pageable) {
        return caseRepository.findByFellahId(fellahId, pageable)
                .map(LegalCaseResponse::from);
    }

    @Transactional(readOnly = true)
    public LegalCaseResponse getCaseForFellah(UUID fellahId, UUID caseId) {
        LegalCase legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("القضية غير موجودة"));
        if (!legalCase.getFellah().getId().equals(fellahId)) {
            throw new AccessDeniedException("ليس لديك صلاحية لهذه القضية");
        }
        return LegalCaseResponse.from(legalCase);
    }

    @Transactional(readOnly = true)
    public Page<LegalCaseResponse> getPendingCases(Pageable pageable) {
        return caseRepository.findByStatus(CaseStatus.PENDING, pageable)
                .map(LegalCaseResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<LegalCaseResponse> getLawyerCases(UUID lawyerId, Pageable pageable) {
        return caseRepository.findByLawyerId(lawyerId, pageable)
                .map(LegalCaseResponse::from);
    }

    @Transactional
    public LegalCaseResponse acceptCase(UUID lawyerId, UUID caseId) {
        LegalCase legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("القضية غير موجودة"));

        if (legalCase.getStatus() != CaseStatus.PENDING) {
            throw new BusinessException("هذه القضية لم تعد متاحة للقبول");
        }

        legalCase.setLawyer(userRef(lawyerId));
        legalCase.setStatus(CaseStatus.ACCEPTED);

        log.info("Case {} accepted by lawyer {}", caseId, lawyerId);
        return LegalCaseResponse.from(caseRepository.save(legalCase));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateReference() {
        String ref;
        do {
            ref = "FL-" + Year.now().getValue() + "-"
                    + (1000 + new Random().nextInt(8999));
        } while (caseRepository.findByReference(ref).isPresent());
        return ref;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new BusinessException("حجم الملف يتجاوز 20MB: " + file.getOriginalFilename());
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("نوع الملف غير مدعوم: " + file.getContentType());
        }
    }

    private String detectType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null)            return "document";
        if (ct.startsWith("image/")) return "image";
        if (ct.startsWith("audio/")) return "audio";
        return "document";
    }

    private User userRef(UUID id) {
        return User.builder().id(id).build();
    }
}
