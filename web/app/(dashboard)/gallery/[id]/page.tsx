/**
 * Photo detail page component.
 * 
 * Dynamic route for displaying individual photo details with full metadata.
 * 
 * @module app/(dashboard)/gallery/[id]
 */

'use client';

import { use } from 'react';
import { usePhoto } from '@/lib/queries/photoQueries';
import { PhotoDetailView } from './components/PhotoDetailView';

interface PhotoDetailPageProps {
  params: Promise<{ id: string }>;
}

export default function PhotoDetailPage({ params }: PhotoDetailPageProps) {
  const { id } = use(params);
  const { data: photo, isLoading, isError } = usePhoto(id);

  if (isLoading) {
    return (
      <div className="flex min-h-[600px] items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-gray-200 border-t-primary" />
          <p className="mt-4 text-gray-600">Loading photo...</p>
        </div>
      </div>
    );
  }

  if (isError || !photo) {
    return (
      <div className="flex min-h-[600px] items-center justify-center">
        <div className="text-center">
          <p className="text-lg font-semibold text-gray-900">
            Photo not found
          </p>
          <p className="mt-2 text-sm text-gray-600">
            The photo you're looking for doesn't exist or has been deleted.
          </p>
        </div>
      </div>
    );
  }

  return <PhotoDetailView photo={photo} />;
}

