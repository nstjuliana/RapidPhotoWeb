/**
 * UploadProgress Component
 * 
 * Displays progress for a single file upload.
 * 
 * Features:
 * - Progress bar
 * - Status badge (pending/uploading/completed/failed)
 * - File name, size, and thumbnail
 * - Error message display for failed uploads
 * - Retry button for failures
 */

'use client';

import { useUploadStore, type UploadItem } from '@/lib/stores/uploadStore';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { RefreshCw, CheckCircle2, XCircle, Clock } from 'lucide-react';
import { useState, useEffect } from 'react';

/**
 * Format file size for display.
 */
function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

interface UploadProgressProps {
  upload: UploadItem;
  onRetry?: (uploadId: string) => void;
}

/**
 * UploadProgress component.
 */
export function UploadProgress({ upload, onRetry }: UploadProgressProps) {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  // Create preview URL for image files
  useEffect(() => {
    if (upload.file.type.startsWith('image/')) {
      const url = URL.createObjectURL(upload.file);
      setPreviewUrl(url);
      return () => {
        URL.revokeObjectURL(url);
      };
    }
  }, [upload.file]);

  const getStatusBadge = () => {
    switch (upload.status) {
      case 'pending':
        return (
          <Badge variant="outline" className="gap-1">
            <Clock className="h-3 w-3" />
            Pending
          </Badge>
        );
      case 'uploading':
        return (
          <Badge variant="default" className="gap-1">
            <RefreshCw className="h-3 w-3 animate-spin" />
            Uploading
          </Badge>
        );
      case 'completed':
        return (
          <Badge variant="default" className="bg-green-500 gap-1">
            <CheckCircle2 className="h-3 w-3" />
            Completed
          </Badge>
        );
      case 'failed':
        return (
          <Badge variant="destructive" className="gap-1">
            <XCircle className="h-3 w-3" />
            Failed
          </Badge>
        );
    }
  };

  return (
    <Card className="p-4">
      <div className="flex gap-4">
        {/* Thumbnail */}
        <div className="w-16 h-16 flex-shrink-0 rounded overflow-hidden bg-gray-100">
          {previewUrl ? (
            <img
              src={previewUrl}
              alt={upload.file.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-400">
              📄
            </div>
          )}
        </div>

        {/* File Info and Progress */}
        <div className="flex-1 min-w-0 space-y-2">
          <div className="flex items-start justify-between gap-2">
            <div className="flex-1 min-w-0">
              <div className="font-medium text-sm truncate" title={upload.file.name}>
                {upload.file.name}
              </div>
              <div className="text-xs text-gray-500">
                {formatFileSize(upload.file.size)}
              </div>
            </div>
            {getStatusBadge()}
          </div>

          {/* Progress Bar */}
          {upload.status === 'uploading' && (
            <Progress value={upload.progress} className="h-2" />
          )}

          {/* Error Message */}
          {upload.status === 'failed' && upload.error && (
            <div className="text-sm text-red-600">{upload.error}</div>
          )}

          {/* Retry Button */}
          {upload.status === 'failed' && onRetry && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onRetry(upload.id)}
              className="gap-1"
            >
              <RefreshCw className="h-3 w-3" />
              Retry
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}

