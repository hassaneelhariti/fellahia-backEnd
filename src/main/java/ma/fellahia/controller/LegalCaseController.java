package ma.fellahia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fellahia.dto.request.CaseSubmitRequest;
import ma.fellahia.dto.response.LegalCaseResponse;
import ma.fellahia.security.UserDetailsImpl;
import ma.fellahia.service.LegalCaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fellah/cases")
@RequiredArgsConstructor
public class LegalCaseController {

    private final LegalCaseService legalCaseService;

    /**
     * POST /api/fellah/cases
     * Submit a new legal case with optional file attachments.
     * Content-Type: multipart/form-data
     * Parts: "data" (JSON CaseSubmitRequest) + "files" (optional, up to 5 files)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LegalCaseResponse> submit(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("data") @Valid CaseSubmitRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        LegalCaseResponse response = legalCaseService.submit(
                userDetails.getId(), request, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/fellah/cases?page=0&size=10&sort=createdAt,desc
     * Returns paginated list of cases for the authenticated Fellah.
     */
    @GetMapping
    public ResponseEntity<Page<LegalCaseResponse>> getMyCases(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                legalCaseService.getFellahCases(userDetails.getId(), pageable));
    }

    /**
     * GET /api/fellah/cases/{id}
     * Get details of a specific case (must belong to the Fellah).
     */
    @GetMapping("/{id}")
    public ResponseEntity<LegalCaseResponse> getCase(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                legalCaseService.getCaseForFellah(userDetails.getId(), id));
    }
}
