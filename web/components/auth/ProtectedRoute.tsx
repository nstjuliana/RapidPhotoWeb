/**
 * ProtectedRoute component for client-side route protection.
 * 
 * Checks authentication status and:
 * - Validates token format and expiration
 * - Attempts token refresh if expired
 * - Shows loading state while checking
 * - Stores intended destination for deep linking
 * - Redirects to /login if not authenticated
 * - Renders children if authenticated
 */

'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth, setIntendedDestination } from '@/lib/hooks/useAuth';
import { refreshAccessToken } from '@/lib/utils/tokenRefresh';
import { isTokenExpired, isValidTokenFormat } from '@/lib/utils/token';
import { STORAGE_KEYS } from '@/lib/utils/constants';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [isChecking, setIsChecking] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      // Get access token
      const token = typeof window !== 'undefined' 
        ? localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
        : null;

      // If no token, store intended destination and redirect to login
      if (!token) {
        if (typeof window !== 'undefined' && pathname) {
          setIntendedDestination(pathname);
        }
        router.push('/login');
        return;
      }

      // Validate token format
      if (!isValidTokenFormat(token)) {
        // Invalid token format - clear tokens and redirect
        if (typeof window !== 'undefined') {
          localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRATION);
          setIntendedDestination(pathname || '/gallery');
        }
        router.push('/login');
        return;
      }

      // Check if token is expired
      if (isTokenExpired(token)) {
        setIsRefreshing(true);
        
        // Attempt token refresh
        const refreshTokenValue = typeof window !== 'undefined'
          ? localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
          : null;

        if (refreshTokenValue && isValidTokenFormat(refreshTokenValue)) {
          const newToken = await refreshAccessToken();
          
          if (newToken) {
            // Refresh succeeded - check authentication again
            setIsRefreshing(false);
            setIsChecking(false);
            return;
          }
        }

        // Refresh failed - store intended destination and redirect to login
        setIsRefreshing(false);
        if (typeof window !== 'undefined') {
          localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRATION);
          setIntendedDestination(pathname || '/gallery');
        }
        router.push('/login');
        return;
      }

      // Token is valid - allow access
      setIsChecking(false);
    };

    checkAuth();
  }, [router, pathname]);

  // Show loading state while checking or refreshing
  if (isChecking || isRefreshing) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent"></div>
          <p className="mt-4 text-gray-600">
            {isRefreshing ? 'Refreshing session...' : 'Loading...'}
          </p>
        </div>
      </div>
    );
  }

  // Double-check authentication before rendering
  if (!isAuthenticated()) {
    return null;
  }

  return <>{children}</>;
}

