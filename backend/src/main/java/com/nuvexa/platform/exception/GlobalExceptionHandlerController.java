package com.nuvexa.platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class GlobalExceptionHandlerController {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final MessageSource messageSource;

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ApiErro> handleCustomException(NegocioException ex, HttpServletRequest request) {
        log.warn("NegocioException: {}", ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErro> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation error: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErro> handleUnreadableBody(HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, messageSource.getMessage("erro.corpoInvalido", null, MESSAGE_LOCALE), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErro> handleTypeMismatch(HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, messageSource.getMessage("erro.parametroInvalido", null, MESSAGE_LOCALE), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErro> handleNoResourceFound(HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, messageSource.getMessage("erro.naoEncontrado", null, MESSAGE_LOCALE), request);
    }

    /**
     * Negativa vinda de @PreAuthorize. Diferente da negativa do filtro de segurança (tratada pelo
     * JwtAccessDeniedHandler), esta é lançada dentro do controller e chegaria ao handler genérico
     * como 500 se não fosse interceptada aqui.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErro> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acesso negado em {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, messageSource.getMessage("erro.acessoNegado", null, MESSAGE_LOCALE), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErro> handleUnexpectedError(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage("erro.inesperado", null, MESSAGE_LOCALE), request);
    }

    private ResponseEntity<ApiErro> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ApiErro error = new ApiErro(LocalDateTime.now(), status.value(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
