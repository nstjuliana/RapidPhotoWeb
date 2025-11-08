/**
 * PhotoDetailView Component
 * 
 * Displays full-size photo with metadata and actions:
 * - Large photo display
 * - Metadata panel (filename, upload date, file size, tags)
 * - Action buttons (Edit Tags, Download, Back)
 * - Tag editor integration
 * 
 * @module app/(dashboard)/gallery/[id]/components
 */

'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { TagEditor } from './TagEditor';
import { downloadSinglePhoto } from '@/lib/utils/download';
import type { PhotoDto } from '@/lib/api/types';

interface PhotoDetailViewProps {
  photo: PhotoDto;
}

/**
 * Format date to readable string.
 */
function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

/**
 * Format file size to readable string.
 */
function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

export function PhotoDetailView({ photo }: PhotoDetailViewProps) {
  const router = useRouter();

  const handleDownload = async () => {
    try {
      await downloadSinglePhoto(photo.downloadUrl, photo.filename);
    } catch (error) {
      console.error('Download failed:', error);
      alert('Failed to download photo. Please try again.');
    }
  };

  const handleBack = () => {
    router.back();
  };

  return (
    <div className="space-y-6">
      {/* Back Button */}
      <Button variant="ghost" onClick={handleBack} className="mb-4">
        <svg
          className="mr-2 h-4 w-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 19l-7-7 7-7"
          />
        </svg>
        Back to Gallery
      </Button>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Photo Display */}
        <div className="lg:col-span-2">
          <div className="relative aspect-auto min-h-[400px] w-full overflow-hidden rounded-lg border bg-gray-100">
            <Image
              src={photo.downloadUrl}
              alt={photo.filename}
              fill
              className="object-contain"
              sizes="(max-width: 1024px) 100vw, 66vw"
              unoptimized
            />
          </div>
        </div>

        {/* Metadata Panel */}
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Photo Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Filename */}
              <div>
                <label className="text-sm font-medium text-gray-500">
                  Filename
                </label>
                <p className="mt-1 break-all text-sm">{photo.filename}</p>
              </div>

              {/* Upload Date */}
              <div>
                <label className="text-sm font-medium text-gray-500">
                  Upload Date
                </label>
                <p className="mt-1 text-sm">{formatDate(photo.uploadDate)}</p>
              </div>

              {/* Status */}
              <div>
                <label className="text-sm font-medium text-gray-500">
                  Status
                </label>
                <p className="mt-1 text-sm capitalize">{photo.status}</p>
              </div>
            </CardContent>
          </Card>

          {/* Tags */}
          <Card>
            <CardHeader>
              <CardTitle>Tags</CardTitle>
            </CardHeader>
            <CardContent>
              <TagEditor photoId={photo.id} initialTags={photo.tags} />
            </CardContent>
          </Card>

          {/* Actions */}
          <div className="flex gap-2">
            <Button onClick={handleDownload} className="flex-1">
              <svg
                className="mr-2 h-4 w-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                />
              </svg>
              Download
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

