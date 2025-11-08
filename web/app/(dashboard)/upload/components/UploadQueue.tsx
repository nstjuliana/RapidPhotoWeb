/**
 * UploadQueue Component
 * 
 * Displays all uploads in the queue with individual progress indicators.
 * 
 * Features:
 * - List all uploads with individual progress
 * - Batch progress indicator (total percentage)
 * - Upload counts (completed/failed/uploading)
 * - Clear completed button
 */

'use client';

import { useState, useEffect } from 'react';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { UploadProgress } from './UploadProgress';
import { Progress } from '@/components/ui/progress';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { usePhotoUpload } from '@/lib/hooks/usePhotoUpload';

/**
 * UploadQueue component.
 */
export function UploadQueue() {
  const [isMounted, setIsMounted] = useState(false);
  const {
    uploadQueue,
    getTotalProgress,
    getCompletedUploads,
    getFailedUploads,
    getActiveUploads,
    clearCompleted,
  } = useUploadStore();
  const { retryUpload } = usePhotoUpload();

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted) {
    return null;
  }

  const totalProgress = getTotalProgress();
  const completedCount = getCompletedUploads().length;
  const failedCount = getFailedUploads().length;
  const activeCount = getActiveUploads().length;
  const totalCount = uploadQueue.length;

  if (uploadQueue.length === 0) {
    return null;
  }

  return (
    <div className="space-y-4">
      {/* Batch Progress Summary */}
      <Card className="p-4">
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <div className="font-medium">Overall Progress</div>
            <div className="text-sm text-gray-500">{totalProgress}%</div>
          </div>
          <Progress value={totalProgress} className="h-3" />
          <div className="flex items-center justify-between text-xs text-gray-500">
            <div>
              {completedCount} completed • {failedCount} failed • {activeCount} active
            </div>
            <div>{totalCount} total</div>
          </div>
        </div>
      </Card>

      {/* Upload List */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-medium">Upload Queue</h3>
          {completedCount > 0 && (
            <Button variant="outline" size="sm" onClick={clearCompleted}>
              Clear Completed
            </Button>
          )}
        </div>
        <div className="space-y-2">
          {uploadQueue.map((upload) => (
            <UploadProgress
              key={upload.id}
              upload={upload}
              onRetry={upload.status === 'failed' ? retryUpload : undefined}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

