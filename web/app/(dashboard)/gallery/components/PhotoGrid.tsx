/**
 * PhotoGrid Component
 * 
 * Displays a responsive grid of photo thumbnails with:
 * - Infinite scroll loading using Intersection Observer
 * - Multi-select support
 * - Filter integration
 * - Empty state handling
 * - Loading skeletons
 * 
 * @module app/(dashboard)/gallery/components
 */

'use client';

import { useEffect, useRef } from 'react';
import { usePhotos } from '@/lib/queries/photoQueries';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { PhotoCard } from './PhotoCard';

interface PhotoGridProps {
  userId: string | null;
  filters?: { tags?: string[]; search?: string };
}

/**
 * Loading skeleton for photo cards.
 */
function PhotoCardSkeleton() {
  return (
    <div className="aspect-square animate-pulse rounded-lg border bg-gray-200" />
  );
}

export function PhotoGrid({ userId, filters }: PhotoGridProps) {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError,
  } = usePhotos(userId, filters);

  const selectedPhotos = useUploadStore((state) => state.selectedPhotos);
  const isSelectMode = useUploadStore((state) => state.isSelectMode);
  const togglePhotoSelection = useUploadStore(
    (state) => state.togglePhotoSelection
  );

  // Intersection Observer for infinite scroll
  const loadMoreRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 }
    );

    const currentRef = loadMoreRef.current;
    if (currentRef) {
      observer.observe(currentRef);
    }

    return () => {
      if (currentRef) {
        observer.unobserve(currentRef);
      }
    };
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  // Flatten all pages into single array
  const photos = data?.pages.flatMap((page) => page.photos) ?? [];

  // Loading state
  if (isLoading) {
    return (
      <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
        {Array.from({ length: 12 }).map((_, i) => (
          <PhotoCardSkeleton key={i} />
        ))}
      </div>
    );
  }

  // Error state
  if (isError) {
    return (
      <div className="flex min-h-[400px] items-center justify-center rounded-lg border bg-gray-50">
        <div className="text-center">
          <p className="text-lg font-semibold text-gray-900">
            Failed to load photos
          </p>
          <p className="mt-2 text-sm text-gray-600">
            Please try refreshing the page
          </p>
        </div>
      </div>
    );
  }

  // Empty state
  if (photos.length === 0) {
    return (
      <div className="flex min-h-[400px] items-center justify-center rounded-lg border bg-gray-50">
        <div className="text-center">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
          <p className="mt-4 text-lg font-semibold text-gray-900">
            No photos found
          </p>
          <p className="mt-2 text-sm text-gray-600">
            {filters?.tags || filters?.search
              ? 'Try adjusting your filters or search'
              : 'Upload your first photo to get started'}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
        {photos.map((photo) => (
          <PhotoCard
            key={photo.id}
            photo={photo}
            isSelected={selectedPhotos.has(photo.id)}
            isSelectMode={isSelectMode}
            onSelect={togglePhotoSelection}
          />
        ))}
      </div>

      {/* Infinite scroll trigger */}
      {hasNextPage && (
        <div ref={loadMoreRef} className="mt-8 flex justify-center">
          {isFetchingNextPage && (
            <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4 w-full">
              {Array.from({ length: 4 }).map((_, i) => (
                <PhotoCardSkeleton key={i} />
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

