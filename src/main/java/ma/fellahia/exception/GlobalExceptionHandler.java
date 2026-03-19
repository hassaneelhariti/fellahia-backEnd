package ma.fellahia.exception;

import lombok.extern.slf4j.Slf4j;
import ma.fellahia.exception.CustomExceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    // ── 402 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return error(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_BALANCE", ex.getMessage());
    }

    // ── 409 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler({BusinessException.class, PhoneAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleBusiness(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, "BUSINESS_ERROR", ex.getMessage());
    }

    // ── 400 (OTP) ────────────────────────────────────────────────────────────
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage());
    }

    // ── 400 (Validation) ─────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("بيانات غير صحيحة")
                .fields(fields)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    // ── 401 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "رقم الهاتف أو كلمة السر غلط");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return error(HttpStatus.UNAUTHORIZED, "ACCOUNT_NOT_VERIFIED",
                "الحساب غير مفعل. رجاءً تحقق من رمز OTP");
    }

    // ── 403 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    // ── 413 (file too large) ─────────────────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "حجم الملف كبير جداً. الحد الأقصى هو 20MB");
    }

    // ── 500 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException ex) {
        log.error("Storage error: ", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR",
                "خطأ في رفع الملف. حاول مرة أخرى");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "خطأ غير متوقع. حاول مرة أخرى");
    }
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleOllamaTimeout(ResourceAccessException ex) {
        return ResponseEntity.status(503)
                .body("AI service timeout. Please try again in a moment.");
    }
    // ── Helper ───────────────────────────────────────────────────────────────
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(code)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
