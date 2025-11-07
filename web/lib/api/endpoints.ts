/**
 * API endpoint functions for authentication.
 * 
 * Provides typed functions for all authentication-related API calls:
 * - Login
 * - Signup
 * - Token validation
 * - Logout
 */

import { apiClient } from './client';
import type {
  LoginRequest,
  LoginResponse,
  SignupRequest,
  ValidateTokenRequest,
  ValidateTokenResponse,
} from './types';

/**
 * Login endpoint.
 * 
 * @param email User email address
 * @param password User password
 * @returns Promise resolving to LoginResponse with token and user info
 */
export async function login(
  email: string,
  password: string
): Promise<LoginResponse> {
  const request: LoginRequest = { email, password };
  const response = await apiClient.post<LoginResponse>(
    '/api/auth/login',
    request
  );
  return response.data;
}

/**
 * Signup endpoint.
 * 
 * @param email User email address
 * @param password User password
 * @returns Promise resolving to SignupResponse with token and user info
 */
export async function signup(
  email: string,
  password: string
): Promise<LoginResponse> {
  const request: SignupRequest = { email, password };
  const response = await apiClient.post<LoginResponse>(
    '/api/auth/signup',
    request
  );
  return response.data;
}

/**
 * Validate token endpoint.
 * 
 * @param token Authentication token to validate
 * @returns Promise resolving to ValidateTokenResponse with validation result
 */
export async function validateToken(
  token: string
): Promise<ValidateTokenResponse> {
  const request: ValidateTokenRequest = { token };
  const response = await apiClient.post<ValidateTokenResponse>(
    '/api/auth/validate',
    request
  );
  return response.data;
}

/**
 * Logout endpoint.
 * 
 * @param token Authentication token to invalidate
 * @returns Promise resolving when logout is complete
 */
export async function logout(token: string): Promise<void> {
  const request: ValidateTokenRequest = { token };
  await apiClient.post('/api/auth/logout', request);
}

