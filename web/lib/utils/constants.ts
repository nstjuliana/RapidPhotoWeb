/**
 * Constants for the application.
 * 
 * Centralized constants for:
 * - API endpoints
 * - Storage keys
 * - Default values
 */

/**
 * Storage keys for localStorage.
 */
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'auth_token',
} as const;

/**
 * API endpoint paths.
 */
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/auth/login',
    SIGNUP: '/api/auth/signup',
    VALIDATE: '/api/auth/validate',
    LOGOUT: '/api/auth/logout',
  },
} as const;

/**
 * Default values.
 */
export const DEFAULTS = {
  PAGE_SIZE: 20,
  MAX_UPLOAD_SIZE: 100 * 1024 * 1024, // 100MB
  MAX_UPLOAD_COUNT: 100,
} as const;

