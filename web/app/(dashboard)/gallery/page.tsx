/**
 * Gallery page component for RapidPhotoUpload web application.
 * 
 * Main gallery page displaying photo grid with filters, search, and multi-select.
 * 
 * Features:
 * - Photo grid with infinite scroll
 * - Tag filtering
 * - Search functionality
 * - Multi-select mode for batch operations
 * - Selection toolbar for batch actions
 */

'use client';

import { useEffect, useState } from 'react';
import { useFilterStore } from '@/lib/stores/filterStore';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { PhotoGrid } from './components/PhotoGrid';
import { FilterBar } from './components/FilterBar';
import { SearchBar } from './components/SearchBar';
import { SelectionToolbar } from './components/SelectionToolbar';

export default function GalleryPage() {
  const [userId, setUserId] = useState<string | null>(null);
  const selectedTags = useFilterStore((state) => state.selectedTags);
  const searchQuery = useFilterStore((state) => state.searchQuery);
  const isSelectMode = useUploadStore((state) => state.isSelectMode);
  const selectedPhotos = useUploadStore((state) => state.selectedPhotos);
  const toggleSelectMode = useUploadStore((state) => state.toggleSelectMode);

  // Read userId from localStorage only on client side to avoid hydration mismatch
  useEffect(() => {
    const storedUserId = localStorage.getItem('user_id');
    setUserId(storedUserId);
  }, []);

  // Prepare filters for photo query
  const filters = {
    tags: selectedTags.length > 0 ? selectedTags : undefined,
    search: searchQuery.trim() || undefined,
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Gallery</h1>
          <p className="mt-2 text-gray-600">
            View and manage your uploaded photos
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={toggleSelectMode}
            className={`rounded-md px-4 py-2 text-sm font-medium transition-colors ${
              isSelectMode
                ? 'bg-primary text-primary-foreground'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            {isSelectMode ? 'Cancel Selection' : 'Select Photos'}
          </button>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <FilterBar userId={userId} />
        <SearchBar />
      </div>

      {/* Photo Grid */}
      <PhotoGrid userId={userId} filters={filters} />

      {/* Selection Toolbar */}
      {isSelectMode && selectedPhotos.size > 0 && (
        <SelectionToolbar />
      )}
    </div>
  );
}
