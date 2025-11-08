/**
 * Upload page component for RapidPhotoUpload web application.
 * 
 * Complete upload page with:
 * - File dropzone for drag-and-drop selection
 * - Batch tagging interface
 * - Upload queue with progress tracking
 * - Upload summary on completion
 */

'use client';

import { useState, useEffect } from 'react';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { usePhotoUpload } from '@/lib/hooks/usePhotoUpload';
import { UploadDropzone } from './components/UploadDropzone';
import { BatchTagInput } from './components/BatchTagInput';
import { UploadQueue } from './components/UploadQueue';
import { UploadSummary } from './components/UploadSummary';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Upload } from 'lucide-react';

export default function UploadPage() {
  const { uploadQueue, batchTags, getActiveUploads } = useUploadStore();
  const { uploadFiles } = usePhotoUpload();
  const [isUploading, setIsUploading] = useState(false);
  const [isMounted, setIsMounted] = useState(false);

  // Prevent hydration mismatch by only rendering after mount
  useEffect(() => {
    setIsMounted(true);
  }, []);

  /**
   * Handle start upload button click.
   */
  const handleStartUpload = async () => {
    if (uploadQueue.length === 0) {
      alert('Please select at least one file to upload');
      return;
    }

    // Get pending files (files that haven't been uploaded yet)
    const pendingFiles = uploadQueue
      .filter((upload) => upload.status === 'pending')
      .map((upload) => upload.file);

    if (pendingFiles.length === 0) {
      alert('No new files to upload');
      return;
    }

    setIsUploading(true);
    try {
      await uploadFiles(pendingFiles, batchTags);
    } catch (error) {
      console.error('Upload error:', error);
      alert('Some uploads failed. Please check the upload queue.');
    } finally {
      setIsUploading(false);
    }
  };

  const hasPendingFiles = uploadQueue.some((upload) => upload.status === 'pending');
  // Only check for actively uploading files, not pending ones
  const hasActiveUploads = uploadQueue.some((upload) => upload.status === 'uploading');

  // Prevent hydration mismatch - only render content after mount
  if (!isMounted) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Upload Photos</h1>
          <p className="text-gray-600 mt-2">
            Upload and manage your photos with batch tagging
          </p>
        </div>
        <div className="flex items-center justify-center py-12">
          <div className="text-gray-500">Loading...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">Upload Photos</h1>
        <p className="text-gray-600 mt-2">
          Upload and manage your photos with batch tagging
        </p>
      </div>

      {/* Dropzone */}
      <UploadDropzone />

      {/* Batch Tags */}
      {uploadQueue.length > 0 && (
        <Card className="p-4">
          <BatchTagInput />
        </Card>
      )}

      {/* Start Upload Button */}
      {uploadQueue.length > 0 && hasPendingFiles && !hasActiveUploads && (
        <Card className="p-4 bg-blue-50 border-blue-200">
          <div className="flex flex-col items-center gap-2">
            <Button
              onClick={handleStartUpload}
              disabled={isUploading}
              size="lg"
              className="gap-2 min-w-[200px]"
            >
              <Upload className="h-5 w-5" />
              {isUploading ? 'Uploading...' : 'Start Upload'}
            </Button>
            <p className="text-xs text-gray-500 text-center">
              {uploadQueue.filter((u) => u.status === 'pending').length} file(s) ready to upload
            </p>
          </div>
        </Card>
      )}

      {/* Upload Queue */}
      {uploadQueue.length > 0 && <UploadQueue />}

      {/* Upload Summary */}
      <UploadSummary />
    </div>
  );
}
