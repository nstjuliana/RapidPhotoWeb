/**
 * FilterBar Component
 * 
 * Provides tag filtering with multi-select dropdown and selected tag chips.
 * 
 * @module app/(dashboard)/gallery/components
 */

'use client';

import { useState, useMemo } from 'react';
import { useFilterStore } from '@/lib/stores/filterStore';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { usePhotos } from '@/lib/queries/photoQueries';

export function FilterBar({ userId }: { userId: string | null }) {
  const selectedTags = useFilterStore((state) => state.selectedTags);
  const addTag = useFilterStore((state) => state.addTag);
  const removeTag = useFilterStore((state) => state.removeTag);
  const clearFilters = useFilterStore((state) => state.clearFilters);

  const [isOpen, setIsOpen] = useState(false);

  // Fetch all photos to extract available tags
  const { data } = usePhotos(userId, {}, 1000); // Large page size to get all tags
  const photos = data?.pages.flatMap((page) => page.photos) ?? [];

  // Extract unique tags from all photos
  const availableTags = useMemo(() => {
    const tagSet = new Set<string>();
    photos.forEach((photo) => {
      photo.tags?.forEach((tag) => tagSet.add(tag));
    });
    return Array.from(tagSet).sort();
  }, [photos]);

  const handleTagToggle = (tag: string) => {
    if (selectedTags.includes(tag)) {
      removeTag(tag);
    } else {
      addTag(tag);
    }
  };

  return (
    <div className="flex flex-wrap items-center gap-2">
      {/* Tag Filter Dropdown */}
      <div className="relative">
        <Button
          variant="outline"
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center gap-2"
        >
          <svg
            className="h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
            />
          </svg>
          Filter by Tags
          {selectedTags.length > 0 && (
            <span className="ml-1 rounded-full bg-primary px-2 py-0.5 text-xs text-primary-foreground">
              {selectedTags.length}
            </span>
          )}
        </Button>

        {/* Dropdown Menu */}
        {isOpen && (
          <>
            <div
              className="fixed inset-0 z-10"
              onClick={() => setIsOpen(false)}
            />
            <div className="absolute left-0 top-full z-20 mt-2 w-64 rounded-lg border bg-white shadow-lg">
              <div className="max-h-64 overflow-y-auto p-2">
                {availableTags.length === 0 ? (
                  <p className="p-2 text-sm text-gray-500">No tags available</p>
                ) : (
                  <div className="space-y-1">
                    {availableTags.map((tag) => {
                      const isSelected = selectedTags.includes(tag);
                      return (
                        <button
                          key={tag}
                          onClick={() => handleTagToggle(tag)}
                          className={`w-full rounded px-3 py-2 text-left text-sm transition-colors ${
                            isSelected
                              ? 'bg-primary text-primary-foreground'
                              : 'hover:bg-gray-100'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span>{tag}</span>
                            {isSelected && (
                              <svg
                                className="h-4 w-4"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  strokeWidth={2}
                                  d="M5 13l4 4L19 7"
                                />
                              </svg>
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>
              {selectedTags.length > 0 && (
                <div className="border-t p-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={clearFilters}
                    className="w-full"
                  >
                    Clear Filters
                  </Button>
                </div>
              )}
            </div>
          </>
        )}
      </div>

      {/* Selected Tags as Chips */}
      {selectedTags.length > 0 && (
        <div className="flex flex-wrap items-center gap-2">
          {selectedTags.map((tag) => (
            <Badge
              key={tag}
              variant="secondary"
              className="flex items-center gap-1 pr-1"
            >
              {tag}
              <button
                onClick={() => removeTag(tag)}
                className="ml-1 rounded-full hover:bg-gray-200"
                aria-label={`Remove ${tag} filter`}
              >
                <svg
                  className="h-3 w-3"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </button>
            </Badge>
          ))}
        </div>
      )}
    </div>
  );
}

