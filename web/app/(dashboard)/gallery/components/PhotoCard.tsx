/**
 * PhotoCard Component
 * 
 * Displays a single photo thumbnail in the gallery grid with:
 * - Image preview using Next.js Image component
 * - Upload date and tags overlay
 * - Click handler for navigation to detail view
 * - Selection checkbox for multi-select mode
 * - Loading skeleton state
 * 
 * @module app/(dashboard)/gallery/components
 */

'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { Badge } from '@/components/ui/badge';
import { useUploadStore } from '@/lib/stores/uploadStore';
import type { PhotoDto } from '@/lib/api/types';

interface PhotoCardProps {
  photo: PhotoDto;
  onSelect?: (photoId: string) => void;
  isSelected?: boolean;
  isSelectMode?: boolean;
}

/**
 * Format date to readable string.
 */
function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

export function PhotoCard({
  photo,
  onSelect,
  isSelected = false,
  isSelectMode = false,
}: PhotoCardProps) {
  const router = useRouter();
  const togglePhotoSelection = useUploadStore((state) => state.togglePhotoSelection);

  const handleClick = (e: React.MouseEvent) => {
    // If in select mode, toggle selection instead of navigating
    if (isSelectMode) {
      e.preventDefault();
      e.stopPropagation();
      if (onSelect) {
        onSelect(photo.id);
      } else {
        togglePhotoSelection(photo.id);
      }
    } else {
      router.push(`/gallery/${photo.id}`);
    }
  };

  const handleCheckboxClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (onSelect) {
      onSelect(photo.id);
    } else {
      togglePhotoSelection(photo.id);
    }
  };

  const uploadDate = new Date(photo.uploadDate);
  const formattedDate = formatDate(photo.uploadDate);

  return (
    <div
      className="group relative aspect-square cursor-pointer overflow-hidden rounded-lg border bg-gray-100 transition-all hover:shadow-lg"
      onClick={handleClick}
    >
      {/* Selection Checkbox */}
      {isSelectMode && (
        <div
          className="absolute left-2 top-2 z-10"
          onClick={handleCheckboxClick}
        >
          <div
            className={`flex h-6 w-6 items-center justify-center rounded border-2 bg-white transition-all ${
              isSelected
                ? 'border-primary bg-primary'
                : 'border-gray-300 hover:border-primary'
            }`}
          >
            {isSelected && (
              <svg
                className="h-4 w-4 text-white"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
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
        </div>
      )}

      {/* Photo Image */}
      <div className="relative h-full w-full">
        <Image
          src={photo.downloadUrl}
          alt={photo.filename}
          fill
          className="object-cover transition-transform group-hover:scale-105"
          sizes="(max-width: 768px) 50vw, (max-width: 1200px) 33vw, 25vw"
          unoptimized // Using presigned URLs, optimization handled by S3
        />
      </div>

      {/* Overlay with metadata */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-black/0 opacity-0 transition-opacity group-hover:opacity-100">
        <div className="absolute bottom-0 left-0 right-0 p-3 text-white">
          {/* Tags */}
          {photo.tags && photo.tags.length > 0 && (
            <div className="mb-2 flex flex-wrap gap-1">
              {photo.tags.slice(0, 3).map((tag) => (
                <Badge
                  key={tag}
                  variant="secondary"
                  className="bg-white/20 text-white hover:bg-white/30"
                >
                  {tag}
                </Badge>
              ))}
              {photo.tags.length > 3 && (
                <Badge
                  variant="secondary"
                  className="bg-white/20 text-white hover:bg-white/30"
                >
                  +{photo.tags.length - 3}
                </Badge>
              )}
            </div>
          )}

          {/* Upload Date */}
          <p className="text-xs text-white/90">{formattedDate}</p>
        </div>
      </div>

      {/* Selected indicator */}
      {isSelected && !isSelectMode && (
        <div className="absolute inset-0 border-4 border-primary" />
      )}
    </div>
  );
}

