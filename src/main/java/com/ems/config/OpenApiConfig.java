package com.ems.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * =============================================================================
 * OPENAPI/SWAGGER CONFIGURATION
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Configures Swagger/OpenAPI for API documentation.
 * Includes JWT Bearer token authentication support.
 * 
 * ACCESS SWAGGER UI:
 * ------------------
 * http://localhost:8080/swagger-ui.html
 * 
 * HOW TO USE JWT IN SWAGGER:
 * --------------------------
 * 1. Call POST /api/auth/login to get JWT token
 * 2. Click "Authorize" button in Swagger UI
 * 3. Enter token: Bearer <your-token>
 * 4. Now all requests will include the JWT token
 * 
 * =============================================================================
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Employee Management System API",
        version = "1.0",
        description = "REST API for Employee Management System with JWT Authentication",
        contact = @Contact(
            name = "EMS Support",
            email = "support@ems.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server")
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    description = "Enter JWT Bearer token. Get token from /api/auth/login endpoint",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration is annotation-based, no code needed
}
