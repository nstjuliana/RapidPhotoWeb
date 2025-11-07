/**
 * User-related TypeScript types.
 * 
 * Defines types for user data and authentication state.
 */

/**
 * User information type.
 */
export interface User {
  id: string;
  email: string;
}

/**
 * Authentication state type.
 */
export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
}

