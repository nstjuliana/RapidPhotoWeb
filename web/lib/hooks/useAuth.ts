/**
 * Authentication hook for managing user authentication state.
 * 
 * Provides:
 * - login(email, password) - Authenticate user and store token
 * - signup(email, password) - Create new user and store token
 * - logout() - Clear token and redirect to login
 * - isAuthenticated() - Check if user is authenticated (token exists and not expired)
 * - getToken() - Get current authentication token
 * 
 * Uses TanStack Query mutations for login/signup operations.
 * Stores access and refresh tokens in localStorage with expiration tracking.
 */

'use client';

import { useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { login as loginApi, signup as signupApi, logout as logoutApi } from '@/lib/api/endpoints';
import type { LoginResponse } from '@/lib/api/types';
import { STORAGE_KEYS } from '@/lib/utils/constants';
import { refreshAccessToken } from '@/lib/utils/tokenRefresh';
import { isTokenExpired, isValidTokenFormat } from '@/lib/utils/token';

/**
 * Get access token from localStorage.
 */
function getAccessToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
}

/**
 * Get refresh token from localStorage.
 */
function getRefreshToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
}

/**
 * Set access and refresh tokens in localStorage with expiration tracking.
 */
function setTokens(accessToken: string, refreshToken: string, expiresIn?: number): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
    
    // Store expiration timestamp if provided
    if (expiresIn) {
      const expirationTimestamp = Date.now() + (expiresIn * 1000);
      localStorage.setItem(STORAGE_KEYS.TOKEN_EXPIRATION, expirationTimestamp.toString());
    }
  }
}

/**
 * Remove authentication tokens from localStorage.
 */
function removeTokens(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRATION);
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
  }
}

/**
 * Set user ID in localStorage.
 */
function setUserId(userId: string): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEYS.USER_ID, userId);
  }
}

/**
 * Remove user ID from localStorage.
 */
function removeUserId(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
  }
}

/**
 * Get intended destination from localStorage.
 */
function getIntendedDestination(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem(STORAGE_KEYS.INTENDED_DESTINATION);
}

/**
 * Set intended destination in localStorage.
 */
function setIntendedDestination(path: string): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEYS.INTENDED_DESTINATION, path);
  }
}

/**
 * Clear intended destination from localStorage.
 */
function clearIntendedDestination(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(STORAGE_KEYS.INTENDED_DESTINATION);
  }
}

/**
 * Authentication hook.
 */
export function useAuth() {
  const router = useRouter();

  /**
   * Check token expiration and attempt refresh if needed.
   */
  useEffect(() => {
    const checkAndRefreshToken = async () => {
      const token = getAccessToken();
      
      // If no token, nothing to check
      if (!token) {
        return;
      }

      // If token is invalid format, clear it
      if (!isValidTokenFormat(token)) {
        removeTokens();
        return;
      }

      // If token is expired, try to refresh
      if (isTokenExpired(token)) {
        const refreshTokenValue = getRefreshToken();
        
        if (refreshTokenValue && isValidTokenFormat(refreshTokenValue)) {
          // Attempt refresh
          const newToken = await refreshAccessToken();
          
          if (!newToken) {
            // Refresh failed - clear tokens and redirect to login
            removeTokens();
            router.push('/login');
          }
        } else {
          // No valid refresh token - clear tokens
          removeTokens();
        }
      }
    };

    checkAndRefreshToken();
  }, [router]);

  /**
   * Login mutation.
   */
  const loginMutation = useMutation({
    mutationFn: async ({ email, password }: { email: string; password: string }) => {
      return await loginApi(email, password);
    },
    onSuccess: (data: LoginResponse) => {
      setTokens(data.accessToken, data.refreshToken, data.expiresIn);
      setUserId(data.userId);
      
      // Check for intended destination
      const intendedDestination = getIntendedDestination();
      if (intendedDestination) {
        clearIntendedDestination();
        router.push(intendedDestination);
      } else {
        router.push('/gallery');
      }
    },
  });

  /**
   * Signup mutation.
   */
  const signupMutation = useMutation({
    mutationFn: async ({ email, password }: { email: string; password: string }) => {
      return await signupApi(email, password);
    },
    onSuccess: (data: LoginResponse) => {
      setTokens(data.accessToken, data.refreshToken, data.expiresIn);
      setUserId(data.userId);
      
      // Check for intended destination
      const intendedDestination = getIntendedDestination();
      if (intendedDestination) {
        clearIntendedDestination();
        router.push(intendedDestination);
      } else {
        router.push('/gallery');
      }
    },
  });

  /**
   * Login function.
   * 
   * @param email User email address
   * @param password User password
   */
  const login = async (email: string, password: string) => {
    await loginMutation.mutateAsync({ email, password });
  };

  /**
   * Signup function.
   * 
   * @param email User email address
   * @param password User password
   */
  const signup = async (email: string, password: string) => {
    await signupMutation.mutateAsync({ email, password });
  };

  /**
   * Logout function.
   * Clears tokens and redirects to login page.
   */
  const logout = async () => {
    const accessToken = getAccessToken();
    if (accessToken) {
      try {
        await logoutApi(accessToken);
      } catch (error) {
        // Ignore logout API errors, still clear tokens locally
        // Don't log error to avoid exposing tokens
      }
    }
    removeTokens();
    router.push('/login');
  };

  /**
   * Check if user is authenticated.
   * 
   * Checks if access token exists, is valid format, and not expired.
   * 
   * @returns true if user is authenticated, false otherwise
   */
  const isAuthenticated = (): boolean => {
    const token = getAccessToken();
    
    if (!token) {
      return false;
    }

    // Check token format
    if (!isValidTokenFormat(token)) {
      return false;
    }

    // Check token expiration
    if (isTokenExpired(token)) {
      return false;
    }

    return true;
  };

  /**
   * Get current access token.
   * 
   * @returns Access token string or null if not authenticated
   */
  const getToken = (): string | null => {
    return getAccessToken();
  };

  return {
    login,
    signup,
    logout,
    isAuthenticated,
    getToken,
    isLoading: loginMutation.isPending || signupMutation.isPending,
    error: loginMutation.error || signupMutation.error,
  };
}

// Export helper functions for use in other components
export { setIntendedDestination };

