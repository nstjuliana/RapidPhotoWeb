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
  RefreshTokenRequest,
  RefreshTokenResponse,
  UploadRequestDto,
  UploadResponseDto,
  UploadStatusDto,
  PhotoDto,
  ListPhotosParams,
  DownloadUrlResponse,
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
 * Refresh token endpoint.
 * 
 * Note: This function uses fetch directly instead of apiClient to avoid
 * triggering the token refresh interceptor, which could cause infinite loops.
 * 
 * @param refreshToken Refresh token string
 * @returns Promise resolving to RefreshTokenResponse with new access token
 */
export async function refreshToken(
  refreshToken: string
): Promise<RefreshTokenResponse> {
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
  const request: RefreshTokenRequest = { refreshToken };
  
  const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Refresh token expired or invalid');
    }
    throw new Error(`Token refresh failed: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Request presigned URL for photo upload.
 * 
 * User ID is extracted from JWT token in Authorization header.
 * 
 * @param request Upload request containing file metadata
 * @returns Promise resolving to UploadResponseDto with presigned URL
 */
export async function requestPresignedUrl(
  request: UploadRequestDto
): Promise<UploadResponseDto> {
  const response = await apiClient.post<UploadResponseDto>(
    '/api/uploads',
    request
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

/**
 * List photos for a user with pagination and optional tag filtering.
 * 
 * @param params ListPhotosParams containing userId, page, size, sortBy, and tags
 * @returns Promise resolving to array of PhotoDto
 */
export async function listPhotos(
  params: ListPhotosParams
): Promise<PhotoDto[]> {
  const { userId, page = 0, size = 20, sortBy, tags } = params;
  
  const queryParams: Record<string, string> = {
    userId,
    page: page.toString(),
    size: size.toString(),
  };
  
  if (sortBy) {
    queryParams.sortBy = sortBy;
  }
  
  if (tags && tags.length > 0) {
    queryParams.tags = tags.join(',');
  }
  
  const response = await apiClient.get<PhotoDto[]>('/api/photos', {
    params: queryParams,
  });
  
  return response.data;
}

/**
 * Get a single photo by ID.
 * 
 * @param photoId Photo ID to retrieve
 * @returns Promise resolving to PhotoDto
 */
export async function getPhoto(photoId: string): Promise<PhotoDto> {
  const response = await apiClient.get<PhotoDto>(`/api/photos/${photoId}`);
  return response.data;
}

/**
 * Get presigned download URL for a photo.
 * 
 * @param photoId Photo ID to get download URL for
 * @returns Promise resolving to DownloadUrlResponse
 */
export async function getPhotoDownloadUrl(
  photoId: string
): Promise<DownloadUrlResponse> {
  const response = await apiClient.get<DownloadUrlResponse>(
    `/api/photos/${photoId}/download`
  );
  return response.data;
}

/**
 * Add tags to a photo.
 * 
 * @param photoId Photo ID to add tags to
 * @param tags Array of tags to add
 * @returns Promise resolving to updated PhotoDto
 */
export async function addPhotoTags(
  photoId: string,
  tags: string[]
): Promise<PhotoDto> {
  const response = await apiClient.post<PhotoDto>(
    `/api/photos/${photoId}/tags`,
    { tags }
  );
  return response.data;
}

/**
 * Remove tags from a photo.
 * 
 * @param photoId Photo ID to remove tags from
 * @param tags Array of tags to remove
 * @returns Promise resolving to updated PhotoDto
 */
export async function removePhotoTags(
  photoId: string,
  tags: string[]
): Promise<PhotoDto> {
  const response = await apiClient.delete<PhotoDto>(
    `/api/photos/${photoId}/tags`,
    { data: { tags } }
  );
  return response.data;
}

/**
 * Replace all tags on a photo.
 * 
 * @param photoId Photo ID to replace tags on
 * @param tags Array of new tags
 * @returns Promise resolving to updated PhotoDto
 */
export async function replacePhotoTags(
  photoId: string,
  tags: string[]
): Promise<PhotoDto> {
  const response = await apiClient.put<PhotoDto>(
    `/api/photos/${photoId}/tags`,
    { tags }
  );
  return response.data;
}

