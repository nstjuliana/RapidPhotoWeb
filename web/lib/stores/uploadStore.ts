/**
 * Upload store for managing upload queue and progress.
 * 
 * Manages state for:
 * - Upload queue (array of uploads in progress)
 * - Selected photos (Set of photo IDs)
 * 
 * Actions:
 * - addUpload: Add a new upload to the queue
 * - removeUpload: Remove an upload from the queue
 * - updateUploadProgress: Update progress for a specific upload
 * - clearQueue: Clear all uploads from the queue
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
  id: string;
  file: File;
  progress: number;
  status: UploadStatus;
  error?: string;
}

/**
 * Upload store state and actions.
 */
interface UploadStore {
  uploadQueue: UploadItem[];
  selectedPhotos: Set<string>;

  // Actions
  addUpload: (upload: UploadItem) => void;
  removeUpload: (id: string) => void;
  updateUploadProgress: (id: string, progress: number, status?: UploadStatus) => void;
  clearQueue: () => void;
  setSelectedPhotos: (photos: Set<string>) => void;
  togglePhotoSelection: (photoId: string) => void;
  clearSelection: () => void;
}

/**
 * Upload store instance.
 */
export const useUploadStore = create<UploadStore>((set) => ({
  uploadQueue: [],
  selectedPhotos: new Set(),

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

  clearQueue: () =>
    set({
      uploadQueue: [],
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
}));

