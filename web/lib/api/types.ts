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
  token: string;
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

