package ma.fellahia.service;

import ma.fellahia.domain.*;
import ma.fellahia.dto.request.CaseSubmitRequest;
import ma.fellahia.dto.response.LegalCaseResponse;
import ma.fellahia.exception.CustomExceptions.AccessDeniedException;
import ma.fellahia.exception.CustomExceptions.BusinessException;
import ma.fellahia.exception.CustomExceptions.ResourceNotFoundException;
import ma.fellahia.repository.CaseFileRepository;
import ma.fellahia.repository.LegalCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegalCaseServiceTest {

    @Mock LegalCaseRepository caseRepository;
    @Mock CaseFileRepository fileRepository;
    @Mock TokenService tokenService;
    @Mock StorageService storageService;
    @InjectMocks LegalCaseService legalCaseService;

    private UUID fellahId;
    private UUID lawyerId;
    private UUID caseId;
    private LegalCase mockCase;

    @BeforeEach
    void setUp() {
        fellahId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        caseId   = UUID.randomUUID();
        User fellah = User.builder().id(fellahId).build();
        mockCase = LegalCase.builder()
                .id(caseId).reference("FL-2026-1234").fellah(fellah)
                .description("قضية زراعية").urgency(CaseUrgency.NORMAL)
                .cost(BigDecimal.valueOf(150)).status(CaseStatus.PENDING).build();
    }

    @Test
    void submit_shouldCreateCase_andDeductTokens() {
        CaseSubmitRequest req = new CaseSubmitRequest();
        req.setDescription("قضية زراعية");
        req.setUrgency(CaseUrgency.NORMAL);
        req.setRegion("Casablanca");

        doNothing().when(tokenService).debit(eq(fellahId), eq(BigDecimal.valueOf(150)), anyString());
        when(caseRepository.findByReference(anyString())).thenReturn(Optional.empty());
        when(caseRepository.save(any(LegalCase.class))).thenReturn(mockCase);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));

        LegalCaseResponse response = legalCaseService.submit(fellahId, req, null);

        assertThat(response).isNotNull();
        verify(tokenService).debit(eq(fellahId), eq(BigDecimal.valueOf(150)), anyString());
    }

    @Test
    void submit_shouldUploadFiles_whenFilesProvided() {
        CaseSubmitRequest req = new CaseSubmitRequest();
        req.setDescription("قضية زراعية");
        req.setUrgency(CaseUrgency.NORMAL);
        req.setRegion("Casablanca");
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[1024]);

        doNothing().when(tokenService).debit(any(), any(), any());
        when(caseRepository.findByReference(anyString())).thenReturn(Optional.empty());
        when(caseRepository.save(any(LegalCase.class))).thenReturn(mockCase);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        when(storageService.upload(any(MultipartFile.class), anyString())).thenReturn("storage-key");

        legalCaseService.submit(fellahId, req, List.of(file));

        verify(storageService).upload(any(), anyString());
        verify(fileRepository).save(any(CaseFile.class));
    }

    @Test
    void submit_shouldThrow_whenFileTooLarge() {
        CaseSubmitRequest req = new CaseSubmitRequest();
        req.setDescription("test");
        req.setUrgency(CaseUrgency.NORMAL);
        byte[] largeContent = new byte[21 * 1024 * 1024];
        MockMultipartFile bigFile = new MockMultipartFile("file", "big.pdf", "application/pdf", largeContent);

        doNothing().when(tokenService).debit(any(), any(), any());
        when(caseRepository.findByReference(anyString())).thenReturn(Optional.empty());
        when(caseRepository.save(any(LegalCase.class))).thenReturn(mockCase);

        Throwable thrown = catchThrowable(() ->
                legalCaseService.submit(fellahId, req, List.of(bigFile)));
        assertThat(thrown).isInstanceOf(BusinessException.class);
    }

    @Test
    void submit_shouldThrow_whenFileTypeNotAllowed() {
        CaseSubmitRequest req = new CaseSubmitRequest();
        req.setDescription("test");
        req.setUrgency(CaseUrgency.NORMAL);
        MockMultipartFile badFile = new MockMultipartFile("file", "script.exe", "application/exe", new byte[100]);

        doNothing().when(tokenService).debit(any(), any(), any());
        when(caseRepository.findByReference(anyString())).thenReturn(Optional.empty());
        when(caseRepository.save(any(LegalCase.class))).thenReturn(mockCase);

        assertThatThrownBy(() -> legalCaseService.submit(fellahId, req, List.of(badFile)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getCaseForFellah_shouldReturnCase_whenOwner() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        LegalCaseResponse response = legalCaseService.getCaseForFellah(fellahId, caseId);
        assertThat(response).isNotNull();
    }

    @Test
    void getCaseForFellah_shouldThrow_whenNotOwner() {
        UUID otherFellahId = UUID.randomUUID();
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        assertThatThrownBy(() -> legalCaseService.getCaseForFellah(otherFellahId, caseId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCaseForFellah_shouldThrow_whenCaseNotFound() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> legalCaseService.getCaseForFellah(fellahId, caseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void acceptCase_shouldSetLawyerAndAcceptedStatus() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        when(caseRepository.save(any(LegalCase.class))).thenReturn(mockCase);
        legalCaseService.acceptCase(lawyerId, caseId);
        assertThat(mockCase.getStatus()).isEqualTo(CaseStatus.ACCEPTED);
        assertThat(mockCase.getLawyer().getId()).isEqualTo(lawyerId);
    }

    @Test
    void acceptCase_shouldThrow_whenCaseNotPending() {
        mockCase.setStatus(CaseStatus.ACCEPTED);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        assertThatThrownBy(() -> legalCaseService.acceptCase(lawyerId, caseId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void acceptCase_shouldThrow_whenCaseNotFound() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> legalCaseService.acceptCase(lawyerId, caseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
