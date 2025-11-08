/**
 * TypeScript types for API requests and responses.
 * 
 * These types match the backend DTOs exactly to ensure type safety
 * across the frontend-backend boundary.
 */

/**
 * Request DTO for login endpoint.
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Response DTO for login and signup endpoints.
 */
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number; // seconds until access token expires
  userId: string;
  email: string;
}

/**
 * Request DTO for signup endpoint.
 */
export interface SignupRequest {
  email: string;
  password: string;
}

/**
 * Request DTO for token validation and logout endpoints.
 */
export interface ValidateTokenRequest {
  token: string;
}

/**
 * Response DTO for token validation endpoint.
 */
export interface ValidateTokenResponse {
  valid: boolean;
  userId: string | null;
}

/**
 * Request DTO for token refresh endpoint.
 */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/**
 * Response DTO for token refresh endpoint.
 */
export interface RefreshTokenResponse {
  accessToken: string;
  expiresIn: number; // seconds until access token expires
}

/**
 * User information type.
 */
export interface User {
  id: string;
  email: string;
}

/**
 * API error response type.
 */
export interface ApiError {
  message: string;
  statusCode?: number;
}

/**
 * Request DTO for upload endpoint.
 */
export interface UploadRequestDto {
  filename: string;
  contentType: string;
  fileSize: number;
  tags?: string[];
}

/**
 * Response DTO for upload endpoint.
 */
export interface UploadResponseDto {
  photoId: string;
  presignedUrl: string;
  s3Key: string;
  expirationTime: number;
}

/**
 * Response DTO for upload status endpoint.
 */
export interface UploadStatusDto {
  photoId: string;
  status: 'PENDING' | 'UPLOADING' | 'COMPLETED' | 'FAILED';
  uploadDate: string;
  errorMessage?: string;
}

/**
 * Photo DTO matching backend PhotoDto.
 */
export interface PhotoDto {
  id: string;
  filename: string;
  s3Key: string;
  uploadDate: string; // ISO 8601 date string
  tags: string[];
  status: string;
  downloadUrl: string;
}

/**
 * Parameters for listing photos.
 */
export interface ListPhotosParams {
  userId: string;
  page?: number;
  size?: number;
  sortBy?: string;
  tags?: string[]; // Will be converted to comma-separated string
}

/**
 * Request DTO for tag operations.
 */
export interface TagOperationRequest {
  tags: string[];
}

/**
 * Response DTO for download URL endpoint.
 */
export interface DownloadUrlResponse {
  downloadUrl: string;
  expirationTime: number;
}

