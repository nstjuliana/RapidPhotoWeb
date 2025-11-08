/**
 * Photo upload hook for managing the complete upload flow.
 * 
 * Orchestrates the upload process:
 * 1. Request presigned URL from backend
 * 2. Upload file directly to S3 with progress tracking
 * 3. Report completion to backend
 * 4. Update upload store throughout the process
 * 
 * Handles errors and provides retry functionality for failed uploads.
 */

'use client';

import { useUploadStore } from '@/lib/stores/uploadStore';
import { requestPresignedUrl, reportUploadComplete } from '@/lib/api/endpoints';
import { uploadToS3 } from '@/lib/utils/s3Upload';
import { useAuth } from '@/lib/hooks/useAuth';

/**
 * Get user ID from localStorage.
 * Currently stored when user logs in (from LoginResponse).
 */
function getUserId(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem('user_id');
}

/**
 * Photo upload hook.
 * 
 * @returns Object with upload functions and state
 */
export function usePhotoUpload() {
  const {
    updateUploadProgress,
    updateUploadStatus,
  } = useUploadStore();
  const { getToken } = useAuth();

  /**
   * Upload a single file through the complete flow.
   * 
   * @param uploadId Local upload ID from upload store
   * @param tags Tags to apply to the photo
   */
  const uploadSingleFile = async (
    uploadId: string,
    tags: string[]
  ): Promise<void> => {
    // Get current queue to ensure we have the latest state
    const { uploadQueue: currentQueue } = useUploadStore.getState();
    const upload = currentQueue.find((u) => u.id === uploadId);
    if (!upload) {
      throw new Error(`Upload not found: ${uploadId}`);
    }

    const userId = getUserId();
    if (!userId) {
      throw new Error('User ID not found. Please log in again.');
    }

    const { file } = upload;

    try {
      // Step 1: Request presigned URL from backend
      updateUploadStatus(uploadId, { status: 'pending' });
      
      const uploadRequest = {
        filename: file.name,
        contentType: file.type,
        fileSize: file.size,
        tags: tags.length > 0 ? tags : undefined,
      };

      const response = await requestPresignedUrl(uploadRequest, userId);

      // Step 2: Update store with presigned URL and photo ID
      updateUploadStatus(uploadId, {
        photoId: response.photoId,
        presignedUrl: response.presignedUrl,
        s3Key: response.s3Key,
        status: 'uploading',
      });

      // Step 3: Upload file to S3 with progress tracking
      await uploadToS3(file, response.presignedUrl, (progress) => {
        updateUploadProgress(uploadId, progress, 'uploading');
      });

      // Step 4: Report completion to backend
      await reportUploadComplete(response.photoId);

      // Step 5: Mark as completed
      updateUploadStatus(uploadId, {
        status: 'completed',
        progress: 100,
      });
    } catch (error) {
      // Handle errors
      const errorMessage =
        error instanceof Error ? error.message : 'Upload failed';
      updateUploadStatus(uploadId, {
        status: 'failed',
        error: errorMessage,
      });
      throw error;
    }
  };

  /**
   * Upload multiple files concurrently.
   * 
   * @param files Array of files to upload (these should already be in the queue)
   * @param tags Batch tags to apply to all photos
   */
  const uploadFiles = async (
    files: File[],
    tags: string[]
  ): Promise<void> => {
    // Get current queue state to ensure we have the latest
    const { uploadQueue: currentQueue } = useUploadStore.getState();
    
    // Find uploads matching the provided files
    const uploadsToProcess = currentQueue.filter((upload) =>
      files.includes(upload.file)
    );

    if (uploadsToProcess.length === 0) {
      throw new Error('No matching uploads found in queue');
    }

    // Upload all files concurrently
    const uploadPromises = uploadsToProcess.map((upload) =>
      uploadSingleFile(upload.id, tags).catch((error) => {
        // Errors are already handled in uploadSingleFile
        console.error(`Upload failed for ${upload.file.name}:`, error);
      })
    );

    await Promise.all(uploadPromises);
  };

  /**
   * Retry a failed upload.
   * 
   * @param uploadId Local upload ID to retry
   */
  const retryUpload = async (uploadId: string): Promise<void> => {
    // Get current queue to ensure we have the latest state
    const { uploadQueue: currentQueue } = useUploadStore.getState();
    const upload = currentQueue.find((u) => u.id === uploadId);
    if (!upload) {
      throw new Error(`Upload not found: ${uploadId}`);
    }

    if (upload.status !== 'failed') {
      throw new Error('Can only retry failed uploads');
    }

    // Get batch tags from store
    const { batchTags } = useUploadStore.getState();
    
    // Reset upload state and retry
    updateUploadStatus(uploadId, {
      status: 'pending',
      progress: 0,
      error: undefined,
    });

    await uploadSingleFile(uploadId, batchTags);
  };

  return {
    uploadFiles,
    retryUpload,
  };
}

