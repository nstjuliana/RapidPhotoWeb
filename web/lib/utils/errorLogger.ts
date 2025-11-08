/**
 * Safe error logging utility.
 * 
 * Provides secure error logging that:
 * - Strips sensitive data (tokens, passwords) from error messages
 * - Prevents token leakage in console logs
 * - Sanitizes error objects before logging
 */

/**
 * Patterns to match sensitive data that should be stripped.
 */
const SENSITIVE_PATTERNS = [
  /access[_-]?token/gi,
  /refresh[_-]?token/gi,
  /authorization/gi,
  /bearer\s+[\w\-._~+/]+/gi,
  /password/gi,
  /secret/gi,
  /api[_-]?key/gi,
];

/**
 * Strip sensitive data from a string.
 * 
 * @param text Text to sanitize
 * @returns Sanitized text with sensitive data replaced
 */
function stripSensitiveData(text: string): string {
  let sanitized = text;

  // Replace JWT tokens (base64-like strings)
  sanitized = sanitized.replace(/[\w\-._~+/]{20,}/g, (match) => {
    // Check if it looks like a JWT (has dots separating parts)
    if (match.includes('.') && match.split('.').length === 3) {
      return '[REDACTED_TOKEN]';
    }
    return match;
  });

  // Replace sensitive patterns
  SENSITIVE_PATTERNS.forEach((pattern) => {
    sanitized = sanitized.replace(pattern, (match) => {
      if (match.toLowerCase().includes('bearer')) {
        return 'Bearer [REDACTED]';
      }
      return match.replace(/[=:]\s*[\w\-._~+/]+/gi, '=[REDACTED]');
    });
  });

  return sanitized;
}

/**
 * Sanitize an error object for safe logging.
 * 
 * @param error Error object or string to sanitize
 * @returns Sanitized error representation
 */
function sanitizeError(error: unknown): string {
  if (error instanceof Error) {
    const sanitizedMessage = stripSensitiveData(error.message);
    const sanitizedStack = error.stack ? stripSensitiveData(error.stack) : undefined;
    
    return sanitizedStack 
      ? `${error.name}: ${sanitizedMessage}\n${sanitizedStack}`
      : `${error.name}: ${sanitizedMessage}`;
  }

  if (typeof error === 'string') {
    return stripSensitiveData(error);
  }

  if (typeof error === 'object' && error !== null) {
    try {
      const jsonString = JSON.stringify(error);
      return stripSensitiveData(jsonString);
    } catch {
      return '[Non-serializable error object]';
    }
  }

  return String(error);
}

/**
 * Safely log an error without exposing sensitive data.
 * 
 * @param error Error to log
 * @param context Optional context message
 */
export function logError(error: unknown, context?: string): void {
  const sanitizedError = sanitizeError(error);
  const message = context 
    ? `${context}: ${sanitizedError}`
    : sanitizedError;
  
  console.error(message);
}

/**
 * Safely log a warning without exposing sensitive data.
 * 
 * @param message Warning message
 * @param data Optional data to log (will be sanitized)
 */
export function logWarning(message: string, data?: unknown): void {
  const sanitizedMessage = stripSensitiveData(message);
  
  if (data !== undefined) {
    const sanitizedData = sanitizeError(data);
    console.warn(sanitizedMessage, sanitizedData);
  } else {
    console.warn(sanitizedMessage);
  }
}

