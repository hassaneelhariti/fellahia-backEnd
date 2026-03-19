package ma.fellahia.controller;

import lombok.RequiredArgsConstructor;
import ma.fellahia.dto.response.LawyerDashboardResponse;
import ma.fellahia.dto.response.LegalCaseResponse;
import ma.fellahia.security.UserDetailsImpl;
import ma.fellahia.service.LegalCaseService;
import ma.fellahia.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lawyer")
@RequiredArgsConstructor
public class LawyerController {

    private final LegalCaseService legalCaseService;
    private final UserService userService;

    /**
     * GET /api/lawyer/dashboard
     * Returns stats for the authenticated lawyer.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<LawyerDashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getLawyerDashboard(userDetails.getId()));
    }

    /**
     * GET /api/lawyer/cases/pending?page=0&size=10
     * Returns paginated list of all PENDING cases (available to accept).
     */
    @GetMapping("/cases/pending")
    public ResponseEntity<Page<LegalCaseResponse>> getPendingCases(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(legalCaseService.getPendingCases(pageable));
    }

    /**
     * GET /api/lawyer/cases/my?page=0&size=10
     * Returns cases the lawyer has accepted.
     */
    @GetMapping("/cases/my")
    public ResponseEntity<Page<LegalCaseResponse>> getMyCases(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
                legalCaseService.getLawyerCases(userDetails.getId(), pageable));
    }

    /**
     * POST /api/lawyer/cases/{id}/accept
     * Accept a pending case.
     */
    @PostMapping("/cases/{id}/accept")
    public ResponseEntity<LegalCaseResponse> acceptCase(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(legalCaseService.acceptCase(userDetails.getId(), id));
    }
}
