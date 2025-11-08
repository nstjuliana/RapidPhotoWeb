/**
 * JWT token utilities for parsing and validation.
 * 
 * Provides functions for:
 * - Decoding JWT tokens (client-side, no signature verification)
 * - Extracting token expiration
 * - Checking token expiration status
 * - Validating token format
 * 
 * Note: These utilities parse JWTs on the client side to read expiration times.
 * Token signature verification is handled by the backend.
 */

/**
 * JWT payload structure (decoded from token).
 */
interface JwtPayload {
  exp?: number; // Expiration timestamp (Unix epoch in seconds)
  iat?: number; // Issued at timestamp
  sub?: string; // Subject (user ID)
  [key: string]: unknown; // Other claims
}

/**
 * Decode JWT token and return payload.
 * 
 * Parses a JWT token without verifying the signature (client-side only).
 * Returns null if token is malformed or cannot be decoded.
 * 
 * @param token JWT token string
 * @returns Decoded payload object or null if invalid
 */
export function decodeJwt(token: string): JwtPayload | null {
  if (!token || typeof token !== 'string') {
    return null;
  }

  try {
    // JWT format: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    // Decode payload (second part)
    const payload = parts[1];
    
    // Add padding if needed for base64 decoding
    const paddedPayload = payload + '='.repeat((4 - (payload.length % 4)) % 4);
    
    // Decode base64
    const decoded = atob(paddedPayload.replace(/-/g, '+').replace(/_/g, '/'));
    
    // Parse JSON
    return JSON.parse(decoded) as JwtPayload;
  } catch (error) {
    // Token is malformed
    return null;
  }
}

/**
 * Get token expiration date.
 * 
 * Extracts the expiration timestamp from a JWT token and returns it as a Date object.
 * 
 * @param token JWT token string
 * @returns Expiration Date or null if token is invalid or has no expiration
 */
export function getTokenExpiration(token: string): Date | null {
  const payload = decodeJwt(token);
  if (!payload || !payload.exp) {
    return null;
  }

  // exp is Unix timestamp in seconds, convert to milliseconds
  return new Date(payload.exp * 1000);
}

/**
 * Check if token is expired.
 * 
 * @param token JWT token string
 * @returns true if token is expired or invalid, false otherwise
 */
export function isTokenExpired(token: string): boolean {
  if (!token) {
    return true;
  }

  const expiration = getTokenExpiration(token);
  if (!expiration) {
    return true; // Invalid token is considered expired
  }

  // Add 5 second buffer to account for clock skew
  const now = Date.now();
  const expirationTime = expiration.getTime();
  return expirationTime <= now + 5000;
}

/**
 * Check if token should be refreshed.
 * 
 * Returns true if token expires within the next 60 seconds.
 * This allows proactive token refresh before expiration.
 * 
 * @param token JWT token string
 * @returns true if token should be refreshed, false otherwise
 */
export function shouldRefreshToken(token: string): boolean {
  if (!token) {
    return false;
  }

  const expiration = getTokenExpiration(token);
  if (!expiration) {
    return false; // Invalid token, can't refresh
  }

  // Check if token expires within 60 seconds
  const now = Date.now();
  const expirationTime = expiration.getTime();
  const timeUntilExpiration = expirationTime - now;
  
  return timeUntilExpiration <= 60000; // 60 seconds
}

/**
 * Validate JWT token format.
 * 
 * Checks if token has the correct JWT structure (3 parts separated by dots).
 * Does not verify signature or expiration.
 * 
 * @param token Token string to validate
 * @returns true if token has valid JWT format, false otherwise
 */
export function isValidTokenFormat(token: string): boolean {
  if (!token || typeof token !== 'string') {
    return false;
  }

  const parts = token.split('.');
  return parts.length === 3 && parts.every(part => part.length > 0);
}

