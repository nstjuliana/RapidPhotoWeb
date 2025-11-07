/**
 * Authentication hook for managing user authentication state.
 * 
 * Provides:
 * - login(email, password) - Authenticate user and store token
 * - signup(email, password) - Create new user and store token
 * - logout() - Clear token and redirect to login
 * - isAuthenticated() - Check if user is authenticated
 * - getToken() - Get current authentication token
 * 
 * Uses TanStack Query mutations for login/signup operations.
 * Stores token in localStorage with key 'auth_token'.
 */

'use client';

import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { login as loginApi, signup as signupApi, logout as logoutApi } from '@/lib/api/endpoints';
import type { LoginResponse } from '@/lib/api/types';

const AUTH_TOKEN_KEY = 'auth_token';

/**
 * Get authentication token from localStorage.
 */
function getToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

/**
 * Set authentication token in localStorage.
 */
function setToken(token: string): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
  }
}

/**
 * Remove authentication token from localStorage.
 */
function removeToken(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(AUTH_TOKEN_KEY);
  }
}

/**
 * Authentication hook.
 */
export function useAuth() {
  const router = useRouter();

  /**
   * Login mutation.
   */
  const loginMutation = useMutation({
    mutationFn: async ({ email, password }: { email: string; password: string }) => {
      return await loginApi(email, password);
    },
    onSuccess: (data: LoginResponse) => {
      setToken(data.token);
      router.push('/gallery');
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
      setToken(data.token);
      router.push('/gallery');
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
   * Clears token and redirects to login page.
   */
  const logout = async () => {
    const token = getToken();
    if (token) {
      try {
        await logoutApi(token);
      } catch (error) {
        // Ignore logout API errors, still clear token locally
        console.error('Logout API error:', error);
      }
    }
    removeToken();
    router.push('/login');
  };

  /**
   * Check if user is authenticated.
   * 
   * @returns true if token exists, false otherwise
   */
  const isAuthenticated = (): boolean => {
    return getToken() !== null;
  };

  /**
   * Get current authentication token.
   * 
   * @returns Token string or null if not authenticated
   */
  const getAuthToken = (): string | null => {
    return getToken();
  };

  return {
    login,
    signup,
    logout,
    isAuthenticated,
    getToken: getAuthToken,
    isLoading: loginMutation.isPending || signupMutation.isPending,
    error: loginMutation.error || signupMutation.error,
  };
}

