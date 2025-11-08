/**
 * UploadSummary Component
 * 
 * Displays summary when uploads complete.
 * 
 * Features:
 * - Success/failure counts
 * - Action buttons (view gallery, upload more)
 * - Display after all uploads complete
 */

'use client';

import { useState, useEffect } from 'react';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { CheckCircle2, XCircle, Upload } from 'lucide-react';
import { useRouter } from 'next/navigation';

/**
 * UploadSummary component.
 */
export function UploadSummary() {
  const [isMounted, setIsMounted] = useState(false);
  const { uploadQueue, getCompletedUploads, getFailedUploads, clearQueue } =
    useUploadStore();
  const router = useRouter();

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted) {
    return null;
  }

  const completedCount = getCompletedUploads().length;
  const failedCount = getFailedUploads().length;
  const totalCount = uploadQueue.length;

  // Only show summary if all uploads are complete (no pending or uploading)
  const allComplete =
    uploadQueue.length > 0 &&
    uploadQueue.every((upload) => upload.status === 'completed' || upload.status === 'failed');

  if (!allComplete) {
    return null;
  }

  const handleViewGallery = () => {
    clearQueue();
    router.push('/gallery');
  };

  const handleUploadMore = () => {
    // Clear completed uploads but keep failed ones for retry
    const { clearCompleted } = useUploadStore.getState();
    clearCompleted();
  };

  return (
    <Card className="p-6">
      <div className="space-y-4">
        <div className="text-center">
          <h3 className="text-lg font-semibold mb-2">Upload Complete</h3>
          <div className="flex items-center justify-center gap-6 text-sm">
            <div className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              <span>{completedCount} successful</span>
            </div>
            {failedCount > 0 && (
              <div className="flex items-center gap-2 text-red-600">
                <XCircle className="h-5 w-5" />
                <span>{failedCount} failed</span>
              </div>
            )}
          </div>
        </div>

        <div className="flex gap-2 justify-center">
          <Button onClick={handleViewGallery} variant="default">
            View Gallery
          </Button>
          <Button onClick={handleUploadMore} variant="outline" className="gap-2">
            <Upload className="h-4 w-4" />
            Upload More
          </Button>
        </div>
      </div>
    </Card>
  );
}

