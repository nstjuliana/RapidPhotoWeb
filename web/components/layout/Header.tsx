/**
 * Header component for dashboard pages.
 * 
 * Provides:
 * - Logo/brand name
 * - Navigation menu
 * - User menu with logout button
 * - Responsive design
 */

'use client';

import Link from 'next/link';
import { useAuth } from '@/lib/hooks/useAuth';
import { Navigation } from './Navigation';
import { Button } from '@/components/ui/button';

export function Header() {
  const { logout, isLoading } = useAuth();

  const handleLogout = async () => {
    await logout();
  };

  return (
    <header className="border-b bg-white">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <div className="flex items-center space-x-8">
          <Link href="/gallery" className="text-xl font-bold">
            RapidPhotoUpload
          </Link>
          <Navigation />
        </div>

        <div className="flex items-center space-x-4">
          <Button
            variant="outline"
            onClick={handleLogout}
            disabled={isLoading}
          >
            {isLoading ? 'Logging out...' : 'Logout'}
          </Button>
        </div>
      </div>
    </header>
  );
}

