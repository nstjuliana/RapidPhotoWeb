/**
 * Standalone token refresh function.
 * 
 * This function handles token refresh independently to avoid circular dependencies
 * between the API client and auth hook. It uses fetch directly instead of the
 * API client to prevent triggering interceptors.
 * 
 * This function:
 * 1. Gets refresh token from localStorage
 * 2. Calls refresh endpoint directly
 * 3. Updates localStorage with new access token and expiration
 * 4. Returns new access token or null if refresh failed
 */

import { refreshToken } from '@/lib/api/endpoints';
import { STORAGE_KEYS } from '@/lib/utils/constants';
import { isValidTokenFormat } from './token';

/**
 * Refresh access token using refresh token.
 * 
 * @returns New access token string, or null if refresh failed
 */
export async function refreshAccessToken(): Promise<string | null> {
  if (typeof window === 'undefined') {
    return null;
  }

  // Get refresh token from localStorage
  const refreshTokenValue = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
  
  if (!refreshTokenValue) {
    return null;
  }

  // Validate refresh token format
  if (!isValidTokenFormat(refreshTokenValue)) {
    return null;
  }

  try {
    // Call refresh endpoint
    const response = await refreshToken(refreshTokenValue);

    // Validate response
    if (!response.accessToken || !isValidTokenFormat(response.accessToken)) {
      return null;
    }

    // Update localStorage with new access token
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken);

    // Calculate and store expiration timestamp
    if (response.expiresIn) {
      const expirationTimestamp = Date.now() + (response.expiresIn * 1000);
      localStorage.setItem(STORAGE_KEYS.TOKEN_EXPIRATION, expirationTimestamp.toString());
    }

    return response.accessToken;
  } catch (error) {
    // Refresh failed - clear tokens
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRATION);
    
    return null;
  }
}

