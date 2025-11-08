/**
 * API client configuration and axios instance setup.
 * 
 * Provides configured axios instance with:
 * - Base URL from environment variable
 * - Request interceptor to add Authorization header and refresh tokens proactively
 * - Response interceptor to handle 401 errors with token refresh and request retry
 * - Concurrent request handling during token refresh
 */

import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { refreshAccessToken } from '@/lib/utils/tokenRefresh';
import { shouldRefreshToken, isValidTokenFormat } from '@/lib/utils/token';
import { STORAGE_KEYS } from '@/lib/utils/constants';
import { API_ENDPOINTS } from '@/lib/utils/constants';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Public endpoints that don't require authentication.
 */
const PUBLIC_ENDPOINTS = [
  API_ENDPOINTS.AUTH.LOGIN,
  API_ENDPOINTS.AUTH.SIGNUP,
  API_ENDPOINTS.AUTH.REFRESH,
];

/**
 * Check if endpoint is public (doesn't require auth).
 */
function isPublicEndpoint(url: string | undefined): boolean {
  if (!url) {
    return false;
  }
  return PUBLIC_ENDPOINTS.some(endpoint => url.includes(endpoint));
}

/**
 * Get access token from localStorage.
 */
function getAuthToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
}

/**
 * Token refresh state management for concurrent requests.
 */
let isRefreshing = false;
let refreshSubscribers: Array<(token: string) => void> = [];

/**
 * Subscribe to token refresh completion.
 * 
 * @param cb Callback to execute when token is refreshed
 */
function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

/**
 * Notify all subscribers that token has been refreshed.
 * 
 * @param token New access token
 */
function onRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token));
  refreshSubscribers = [];
}

/**
 * Handle failed request with token refresh.
 * 
 * @param error Axios error
 * @param originalRequest Original request config
 * @returns Promise that resolves when request is retried or rejects if refresh fails
 */
async function handleTokenRefresh(
  error: AxiosError,
  originalRequest: InternalAxiosRequestConfig
): Promise<unknown> {
  // If already refreshing, queue this request
  if (isRefreshing) {
    return new Promise((resolve) => {
      subscribeTokenRefresh((token: string) => {
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        resolve(apiClient.request(originalRequest));
      });
    });
  }

  // Start refresh process
  isRefreshing = true;

  try {
    const newToken = await refreshAccessToken();

    if (!newToken) {
      // Refresh failed - clear tokens and redirect to login
      if (typeof window !== 'undefined') {
        localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRATION);
        window.location.href = '/login';
      }
      isRefreshing = false;
      return Promise.reject(error);
    }

    // Notify all queued requests
    onRefreshed(newToken);

    // Retry original request with new token
    if (originalRequest.headers) {
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
    }
    isRefreshing = false;
    return apiClient.request(originalRequest);
  } catch (refreshError) {
    // Refresh failed - clear tokens and redirect to login
    if (typeof window !== 'undefined') {
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRATION);
      window.location.href = '/login';
    }
    isRefreshing = false;
    return Promise.reject(refreshError);
  }
}

/**
 * Configured axios instance for API requests.
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor: Add Authorization header and refresh token proactively.
 */
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    // Skip auth for public endpoints
    if (isPublicEndpoint(config.url)) {
      return config;
    }

    let token = getAuthToken();

    // If no token or invalid format, proceed without auth (will fail with 401)
    if (!token || !isValidTokenFormat(token)) {
      return config;
    }

    // Check if token should be refreshed proactively
    if (shouldRefreshToken(token)) {
      try {
        const newToken = await refreshAccessToken();
        if (newToken) {
          token = newToken;
        }
      } catch (error) {
        // Refresh failed, proceed with existing token (will fail with 401 and trigger reactive refresh)
        // Don't log error to avoid exposing tokens
      }
    }

    // Add Authorization header
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor: Handle 401 errors with token refresh and request retry.
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Skip refresh for public endpoints
    if (isPublicEndpoint(originalRequest?.url)) {
      return Promise.reject(error);
    }

    // Handle 401 Unauthorized errors
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;

      // Attempt token refresh and retry request
      return handleTokenRefresh(error, originalRequest);
    }

    // For other errors, reject normally
    return Promise.reject(error);
  }
);

