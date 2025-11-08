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
  UploadRequestDto,
  UploadResponseDto,
  UploadStatusDto,
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

/**
 * Request presigned URL for photo upload.
 * 
 * @param request Upload request containing file metadata
 * @param userId User ID for authentication (currently using x-user-id header)
 * @returns Promise resolving to UploadResponseDto with presigned URL
 */
export async function requestPresignedUrl(
  request: UploadRequestDto,
  userId: string
): Promise<UploadResponseDto> {
  const response = await apiClient.post<UploadResponseDto>(
    '/api/uploads',
    request,
    {
      headers: {
        'x-user-id': userId,
      },
    }
  );
  return response.data;
}

/**
 * Report upload completion to backend.
 * 
 * @param photoId Photo ID to mark as completed
 * @returns Promise resolving when completion is reported
 */
export async function reportUploadComplete(photoId: string): Promise<void> {
  await apiClient.post(`/api/uploads/${photoId}/complete`);
}

/**
 * Get upload status for a photo.
 * 
 * @param photoId Photo ID to check status for
 * @returns Promise resolving to UploadStatusDto with current status
 */
export async function getUploadStatus(photoId: string): Promise<UploadStatusDto> {
  const response = await apiClient.get<UploadStatusDto>(
    `/api/uploads/${photoId}/status`
  );
  return response.data;
}

