/**
 * Dashboard layout component for RapidPhotoUpload web application.
 * 
 * Wraps all authenticated pages with:
 * - Header navigation
 * - Route protection
 * - Consistent layout structure
 */

'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { Header } from '@/components/layout/Header';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isChecking, setIsChecking] = useState(true);
  const router = useRouter();
  const { isAuthenticated: checkAuth } = useAuth();

  // Check authentication only on client side to avoid hydration mismatch
  useEffect(() => {
    const authStatus = checkAuth();
    setIsAuthenticated(authStatus);
    setIsChecking(false);

    if (!authStatus) {
      router.push('/login');
    }
  }, [checkAuth, router]);

  // Show loading state during initial check (matches server render)
  if (isChecking) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  // If not authenticated, show loading (redirect will happen)
  if (!isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">{children}</main>
    </div>
  );
}

