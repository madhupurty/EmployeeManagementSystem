package com.ems.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * =============================================================================
 * JWT UTILITY CLASS - Token Generation and Validation
 * =============================================================================
 * 
 * WHAT IS JWT (JSON Web Token)?
 * -----------------------------
 * JWT is a compact, URL-safe way to securely transmit information between parties.
 * It consists of three parts separated by dots:
 *   1. Header: Algorithm and token type (e.g., HS256, JWT)
 *   2. Payload: Claims (data like username, roles, expiration)
 *   3. Signature: Verifies the token hasn't been tampered with
 * 
 * Example: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.xyz
 *          |---Header----|.|-Payload-|.|-Signature-|
 * 
 * WHY USE JWT FOR AUTHENTICATION?
 * --------------------------------
 * 1. Stateless: Server doesn't need to store session data
 * 2. Scalable: Works well with microservices and load balancers
 * 3. Self-contained: Token carries all necessary information
 * 4. Cross-domain: Can be used across different domains
 * 
 * INTERVIEW QUESTION: How is JWT different from Session-based authentication?
 * ANSWER:
 *   Session: Server stores session data, sends session ID in cookie
 *            Stateful, requires server memory, hard to scale
 *   JWT: Token contains all info, server just validates signature
 *        Stateless, no server memory needed, easy to scale
 * 
 * INTERVIEW QUESTION: Is JWT encrypted?
 * ANSWER: No! JWT is ENCODED (Base64), not encrypted. Anyone can decode and
 *         read the payload. Never store sensitive data in JWT.
 *         The signature only ensures the token hasn't been modified.
 * 
 * =============================================================================
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /*
     * Secret key for signing JWTs - MUST be kept secret!
     * In production, use a strong random key stored securely.
     * 
     * The key should be at least 256 bits (32 characters) for HS256 algorithm.
     * We use Base64 encoded key from application.properties
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /*
     * Token expiration time in milliseconds
     * Default: 24 hours (86400000 ms)
     * 
     * Short expiration = More secure but user must re-login frequently
     * Long expiration = More convenient but higher risk if token is stolen
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ==========================================================================
    // TOKEN GENERATION
    // ==========================================================================

    /**
     * Generate a JWT token for a user.
     * 
     * FLOW:
     * 1. Create claims (payload data)
     * 2. Set subject (username) and timestamps
     * 3. Sign with secret key
     * 4. Return compact token string
     * 
     * @param userDetails Spring Security UserDetails object
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add custom claims (optional)
        // We add roles to the token so we don't need to query DB for authorization
        claims.put("roles", userDetails.getAuthorities().toString());
        
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate token with extra claims.
     * Use this when you need to add custom data to the token.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Internal method to create the JWT token.
     * 
     * @param claims Payload data
     * @param subject Username (the "sub" claim)
     * @return Signed JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        logger.debug("Creating JWT token for user: {}", subject);
        logger.debug("Token will expire at: {}", expiry);

        return Jwts.builder()
                .claims(claims)                          // Set custom claims
                .subject(subject)                        // Set username
                .issuedAt(now)                          // Set creation time
                .expiration(expiry)                      // Set expiration time
                .signWith(getSigningKey())               // Sign with secret key
                .compact();                              // Build the token
    }

    // ==========================================================================
    // TOKEN VALIDATION
    // ==========================================================================

    /**
     * Validate if a token is valid for a given user.
     * 
     * VALIDATION CHECKS:
     * 1. Username in token matches the UserDetails
     * 2. Token is not expired
     * 3. Signature is valid (implicit - extractClaims would fail otherwise)
     * 
     * @param token JWT token to validate
     * @param userDetails User to validate against
     * @return true if token is valid
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            if (isValid) {
                logger.debug("Token validation successful for user: {}", username);
            } else {
                logger.warn("Token validation failed for user: {}", username);
            }
            
            return isValid;
        } catch (JwtException e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    // ==========================================================================
    // CLAIM EXTRACTION
    // ==========================================================================

    /**
     * Extract username from token.
     * The username is stored in the "subject" (sub) claim.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from the token.
     * 
     * Uses Java functional interface to allow flexible claim extraction.
     * 
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the token.
     * 
     * This parses and verifies the token:
     * 1. Decode the token
     * 2. Verify signature using secret key
     * 3. Return payload (claims)
     * 
     * If signature is invalid or token is malformed, throws JwtException.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())     // Set the key to verify signature
                .build()
                .parseSignedClaims(token)        // Parse and verify
                .getPayload();                   // Get the claims
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    /**
     * Get the signing key for JWT operations.
     * 
     * We use HMAC-SHA256 (HS256) algorithm which requires a secret key.
     * The key is Base64 encoded in application.properties.
     * 
     * INTERVIEW QUESTION: What's the difference between HS256 and RS256?
     * ANSWER:
     *   HS256: Symmetric key (same key to sign and verify)
     *          Simpler, faster, but key must be shared between services
     *   RS256: Asymmetric key (private key signs, public key verifies)
     *          More complex but public key can be safely shared
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
