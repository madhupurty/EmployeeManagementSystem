package com.ems.exception;

import com.ems.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * =============================================================================
 * GLOBAL EXCEPTION HANDLER - Centralized Error Handling
 * =============================================================================
 * 
 * This class handles ALL exceptions thrown by controllers and converts them
 * into consistent, user-friendly error responses.
 * 
 * WHY CENTRALIZED EXCEPTION HANDLING?
 * ------------------------------------
 * 1. CONSISTENCY: All errors follow the same format
 * 2. DRY: No duplicate try-catch blocks in every controller
 * 3. SEPARATION: Error handling logic separated from business logic
 * 4. LOGGING: Central place to log all errors
 * 5. SECURITY: Control what error details are exposed to clients
 * 
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 
 * @ControllerAdvice: Global exception handler for all controllers
 * @ResponseBody: Return values are serialized to JSON
 * 
 * INTERVIEW QUESTION: How does @RestControllerAdvice work?
 * ANSWER: Spring registers this class to intercept exceptions from all
 *         controllers. When an exception is thrown, Spring finds a matching
 *         @ExceptionHandler method based on exception type and invokes it.
 *         The method's return value becomes the HTTP response body.
 * 
 * =============================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==========================================================================
    // CUSTOM BUSINESS EXCEPTIONS
    // ==========================================================================

    /**
     * Handles ResourceNotFoundException (404 Not Found)
     * 
     * Thrown when requested entity doesn't exist in database.
     * Example: GET /api/employees/999 where ID 999 doesn't exist
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(), 
                ex.getMessage(), 
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles BadRequestException (400 Bad Request)
     * 
     * Thrown for business rule violations.
     * Example: Trying to create employee with duplicate email
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequest(
            BadRequestException ex, 
            WebRequest request) {
        
        logger.warn("Bad request: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(), 
                ex.getMessage(), 
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ==========================================================================
    // SECURITY/AUTHENTICATION EXCEPTIONS
    // ==========================================================================

    /**
     * Handles AccessDeniedException (403 Forbidden)
     * 
     * Thrown when user is authenticated but lacks permission for the resource.
     * Example: EMPLOYEE trying to delete another employee
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Access Denied: You don't have permission to access this resource",
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles AuthenticationException (401 Unauthorized)
     * 
     * Thrown when authentication fails.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {
        
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Authentication failed: " + ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles BadCredentialsException (401 Unauthorized)
     * 
     * Thrown when login credentials are incorrect.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {
        
        logger.warn("Bad credentials: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Invalid username/email or password",
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles ExpiredJwtException (401 Unauthorized)
     * 
     * Thrown when JWT token has expired.
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDetails> handleExpiredJwt(
            ExpiredJwtException ex,
            WebRequest request) {
        
        logger.warn("JWT token expired: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "JWT token has expired. Please login again.",
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles MalformedJwtException and SignatureException (401 Unauthorized)
     * 
     * Thrown when JWT token is invalid or tampered with.
     */
    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorDetails> handleInvalidJwt(
            Exception ex,
            WebRequest request) {
        
        logger.warn("Invalid JWT token: {}", ex.getMessage());
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Invalid JWT token",
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ==========================================================================
    // VALIDATION EXCEPTIONS
    // ==========================================================================

    /**
     * Handles MethodArgumentNotValidException (400 Bad Request)
     * 
     * WHEN IS THIS THROWN?
     * When @Valid annotation is used on @RequestBody and validation fails.
     * 
     * EXAMPLE:
     * If EmployeeRequestDTO.firstName is null but @NotBlank is present,
     * this exception is thrown with field-level error details.
     * 
     * RESPONSE FORMAT:
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "message": "Validation Failed",
     *   "details": "Invalid input data",
     *   "validationErrors": {
     *     "firstName": "First name is required",
     *     "email": "Please provide a valid email address"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {} errors", ex.getBindingResult().getErrorCount());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            String msg = err.getDefaultMessage();
            errors.put(field, msg);
        });
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(), 
                "Validation Failed", 
                "Invalid input data", 
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MissingServletRequestParameterException (400 Bad Request)
     * 
     * Thrown when a required @RequestParam is missing.
     * Example: GET /api/employees/search (missing required 'keyword' param)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParams(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        
        logger.warn("Missing request parameter: {}", ex.getParameterName());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MethodArgumentTypeMismatchException (400 Bad Request)
     * 
     * Thrown when path variable or request param can't be converted.
     * Example: GET /api/employees/abc (expecting Long, got String)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        
        logger.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getValue());
        
        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ==========================================================================
    // HTTP/REQUEST EXCEPTIONS
    // ==========================================================================

    /**
     * Handles HttpMessageNotReadableException (400 Bad Request)
     * 
     * Thrown when request body can't be deserialized (malformed JSON).
     * Also thrown when enum value is invalid.
     * 
     * Example: {"status": "INVALID_STATUS"} when EmployeeStatus enum
     * doesn't have INVALID_STATUS value
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {
        
        logger.warn("Malformed request body: {}", ex.getMessage());
        
        String message = "Malformed request body. Please check the JSON format.";
        
        // Check if it's an enum parsing error
        if (ex.getMessage() != null && ex.getMessage().contains("Cannot deserialize value of type")) {
            message = "Invalid value provided. Please check allowed values for enum fields.";
        }
        
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles HttpRequestMethodNotSupportedException (405 Method Not Allowed)
     * 
     * Thrown when HTTP method is not supported for an endpoint.
     * Example: DELETE /api/employees (only GET, POST are supported)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDetails> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            WebRequest request) {
        
        logger.warn("Method not supported: {}", ex.getMethod());
        
        String message = String.format(
                "HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(),
                ex.getSupportedHttpMethods()
        );
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles HttpMediaTypeNotSupportedException (415 Unsupported Media Type)
     * 
     * Thrown when Content-Type header is not supported.
     * Example: Sending XML when endpoint only accepts JSON
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorDetails> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            WebRequest request) {
        
        logger.warn("Media type not supported: {}", ex.getContentType());
        
        String message = String.format(
                "Media type '%s' is not supported. Supported types: %s",
                ex.getContentType(),
                ex.getSupportedMediaTypes()
        );
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles NoHandlerFoundException (404 Not Found)
     * 
     * Thrown when no handler is found for the request URL.
     * Example: GET /api/unknown-endpoint
     * 
     * NOTE: Requires spring.mvc.throw-exception-if-no-handler-found=true
     * and spring.web.resources.add-mappings=false in application.properties
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoHandlerFound(
            NoHandlerFoundException ex,
            WebRequest request) {
        
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        String message = String.format(
                "No endpoint found for %s %s",
                ex.getHttpMethod(),
                ex.getRequestURL()
        );
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ==========================================================================
    // CATCH-ALL EXCEPTION HANDLER
    // ==========================================================================

    /**
     * Handles all unhandled exceptions (500 Internal Server Error)
     * 
     * This is the fallback handler for any exception not caught by
     * specific handlers above.
     * 
     * SECURITY NOTE: In production, don't expose exception details!
     * Log the full error but return a generic message to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobal(Exception ex, WebRequest request) {
        // Log the full stack trace for debugging
        logger.error("Unexpected error occurred: ", ex);
        
        // Return generic message to client (don't expose internal details)
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(), 
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
