/**
 * Upload store for managing upload queue and progress.
 * 
 * Manages state for:
 * - Upload queue (array of uploads in progress)
 * - Batch tags (tags applied to all uploads)
 * - Selected photos (Set of photo IDs)
 * 
 * Actions:
 * - addFiles: Add multiple files to the queue
 * - addUpload: Add a new upload to the queue
 * - removeUpload: Remove an upload from the queue
 * - updateUploadProgress: Update progress for a specific upload
 * - updateUploadStatus: Update status and metadata for a specific upload
 * - clearQueue: Clear all uploads from the queue
 * - clearCompleted: Clear completed uploads from the queue
 * - setBatchTags: Set batch tags for all uploads
 */

import { create } from 'zustand';

/**
 * Upload status type.
 */
export type UploadStatus = 'pending' | 'uploading' | 'completed' | 'failed';

/**
 * Upload item in the queue.
 */
export interface UploadItem {
  id: string;              // Local file ID (generated client-side)
  file: File;
  photoId?: string;       // Backend photo ID (from presigned URL response)
  presignedUrl?: string;   // S3 presigned URL
  s3Key?: string;          // S3 storage key
  progress: number;        // Upload progress (0-100)
  status: UploadStatus;
  error?: string;          // Error message if upload failed
}

/**
 * Upload store state and actions.
 */
interface UploadStore {
  uploadQueue: UploadItem[];
  batchTags: string[];
  selectedPhotos: Set<string>;

  // Actions
  addFiles: (files: File[]) => void;
  addUpload: (upload: UploadItem) => void;
  removeUpload: (id: string) => void;
  updateUploadProgress: (id: string, progress: number, status?: UploadStatus) => void;
  updateUploadStatus: (id: string, updates: Partial<UploadItem>) => void;
  clearQueue: () => void;
  clearCompleted: () => void;
  setBatchTags: (tags: string[]) => void;
  setSelectedPhotos: (photos: Set<string>) => void;
  togglePhotoSelection: (photoId: string) => void;
  clearSelection: () => void;

  // Selectors
  getPendingUploads: () => UploadItem[];
  getActiveUploads: () => UploadItem[];
  getCompletedUploads: () => UploadItem[];
  getFailedUploads: () => UploadItem[];
  getTotalProgress: () => number;
}

/**
 * Generate a unique ID for a file upload.
 */
function generateFileId(): string {
  return `file-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * Upload store instance.
 */
export const useUploadStore = create<UploadStore>((set, get) => ({
  uploadQueue: [],
  batchTags: [],
  selectedPhotos: new Set(),

  addFiles: (files) =>
    set((state) => {
      const newUploads: UploadItem[] = files.map((file) => ({
        id: generateFileId(),
        file,
        progress: 0,
        status: 'pending' as UploadStatus,
      }));
      return {
        uploadQueue: [...state.uploadQueue, ...newUploads],
      };
    }),

  addUpload: (upload) =>
    set((state) => ({
      uploadQueue: [...state.uploadQueue, upload],
    })),

  removeUpload: (id) =>
    set((state) => ({
      uploadQueue: state.uploadQueue.filter((upload) => upload.id !== id),
    })),

  updateUploadProgress: (id, progress, status) =>
    set((state) => ({
      uploadQueue: state.uploadQueue.map((upload) =>
        upload.id === id
          ? { ...upload, progress, status: status || upload.status }
          : upload
      ),
    })),

  updateUploadStatus: (id, updates) =>
    set((state) => ({
      uploadQueue: state.uploadQueue.map((upload) =>
        upload.id === id ? { ...upload, ...updates } : upload
      ),
    })),

  clearQueue: () =>
    set({
      uploadQueue: [],
    }),

  clearCompleted: () =>
    set((state) => ({
      uploadQueue: state.uploadQueue.filter(
        (upload) => upload.status !== 'completed'
      ),
    })),

  setBatchTags: (tags) =>
    set({
      batchTags: tags,
    }),

  setSelectedPhotos: (photos) =>
    set({
      selectedPhotos: photos,
    }),

  togglePhotoSelection: (photoId) =>
    set((state) => {
      const newSelection = new Set(state.selectedPhotos);
      if (newSelection.has(photoId)) {
        newSelection.delete(photoId);
      } else {
        newSelection.add(photoId);
      }
      return { selectedPhotos: newSelection };
    }),

  clearSelection: () =>
    set({
      selectedPhotos: new Set(),
    }),

  // Selectors
  getPendingUploads: () => {
    return get().uploadQueue.filter((upload) => upload.status === 'pending');
  },

  getActiveUploads: () => {
    return get().uploadQueue.filter(
      (upload) => upload.status === 'pending' || upload.status === 'uploading'
    );
  },

  getCompletedUploads: () => {
    return get().uploadQueue.filter((upload) => upload.status === 'completed');
  },

  getFailedUploads: () => {
    return get().uploadQueue.filter((upload) => upload.status === 'failed');
  },

  getTotalProgress: () => {
    const queue = get().uploadQueue;
    if (queue.length === 0) return 0;
    const totalProgress = queue.reduce((sum, upload) => sum + upload.progress, 0);
    return Math.round(totalProgress / queue.length);
  },
}));

