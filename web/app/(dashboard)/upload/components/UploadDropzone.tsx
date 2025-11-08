/**
 * UploadDropzone Component
 * 
 * Drag-and-drop file selection component using react-dropzone.
 * 
 * Features:
 * - Accepts image files only
 * - Max 100 files, max 50MB per file
 * - Visual feedback during drag
 * - File preview grid with thumbnails
 * - Remove button for each file before upload
 * - Memory leak prevention (revoke object URLs)
 */

'use client';

import { useCallback, useEffect, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { X } from 'lucide-react';

const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
const MAX_FILES = 100;

/**
 * Format file size for display.
 */
function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/**
 * UploadDropzone component.
 */
export function UploadDropzone() {
  const [isMounted, setIsMounted] = useState(false);
  const { addFiles, uploadQueue, removeUpload } = useUploadStore();
  const [previewUrls, setPreviewUrls] = useState<Map<string, string>>(new Map());

  useEffect(() => {
    setIsMounted(true);
  }, []);

  /**
   * Handle file drop or selection.
   */
  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      if (!isMounted) return;
      
      // Validate file count
      const currentCount = uploadQueue.length;
      const newCount = acceptedFiles.length;
      if (currentCount + newCount > MAX_FILES) {
        alert(`Maximum ${MAX_FILES} files allowed. You already have ${currentCount} files.`);
        return;
      }

      // Add files to upload queue
      addFiles(acceptedFiles);

      // Create preview URLs for images
      // Use setTimeout to ensure uploads are added to store first
      setTimeout(() => {
        const { uploadQueue: updatedQueue } = useUploadStore.getState();
        const newUploads = updatedQueue.slice(-acceptedFiles.length);
        
        newUploads.forEach((upload) => {
          if (upload.file.type.startsWith('image/')) {
            const url = URL.createObjectURL(upload.file);
            setPreviewUrls((prev) => {
              const newMap = new Map(prev);
              newMap.set(upload.id, url);
              return newMap;
            });
          }
        });
      }, 0);
    },
    [addFiles, uploadQueue, isMounted]
  );

  /**
   * Handle file rejection (validation errors).
   */
  const onDropRejected = useCallback((fileRejections: any[]) => {
    const errors = fileRejections.flatMap((rejection) =>
      rejection.errors.map((error: any) => {
        if (error.code === 'file-too-large') {
          return `File "${rejection.file.name}" exceeds ${formatFileSize(MAX_FILE_SIZE)}`;
        }
        if (error.code === 'file-invalid-type') {
          return `File "${rejection.file.name}" is not an image`;
        }
        return `File "${rejection.file.name}": ${error.message}`;
      })
    );
    alert(errors.join('\n'));
  }, []);

  /**
   * Configure dropzone options.
   */
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    onDropRejected,
    accept: {
      'image/*': [],
    },
    maxSize: MAX_FILE_SIZE,
    multiple: true,
    maxFiles: MAX_FILES,
    disabled: !isMounted,
  });

  /**
   * Clean up preview URLs when uploads are removed or component unmounts.
   */
  useEffect(() => {
    return () => {
      previewUrls.forEach((url) => {
        URL.revokeObjectURL(url);
      });
    };
  }, [previewUrls]);

  /**
   * Handle removing a file from the queue.
   */
  const handleRemoveFile = (uploadId: string) => {
    // Revoke preview URL if exists
    const previewUrl = previewUrls.get(uploadId);
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrls((prev) => {
        const newMap = new Map(prev);
        newMap.delete(uploadId);
        return newMap;
      });
    }
    removeUpload(uploadId);
  };

  if (!isMounted) {
    return (
      <Card>
        <div className="border-2 border-dashed rounded-lg p-8 text-center">
          <div className="space-y-2">
            <div className="text-4xl">📸</div>
            <div className="text-lg font-medium">Loading...</div>
          </div>
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {/* Dropzone */}
      <Card>
        <div
          {...getRootProps()}
          className={`
            border-2 border-dashed rounded-lg p-8 text-center cursor-pointer
            transition-colors
            ${
              isDragActive
                ? 'border-primary bg-primary/5'
                : 'border-gray-300 hover:border-gray-400'
            }
          `}
        >
          <input {...getInputProps()} />
          <div className="space-y-2">
            <div className="text-4xl">📸</div>
            <div className="text-lg font-medium">
              {isDragActive
                ? 'Drop files here'
                : 'Drag & drop photos here, or click to select'}
            </div>
            <div className="text-sm text-gray-500">
              Up to {MAX_FILES} files, max {formatFileSize(MAX_FILE_SIZE)} each
            </div>
            <div className="text-xs text-gray-400">
              Images only (JPEG, PNG, GIF, etc.)
            </div>
          </div>
        </div>
      </Card>

      {/* File Preview Grid */}
      {uploadQueue.length > 0 && (
        <div className="space-y-2">
          <h3 className="text-sm font-medium">
            Selected Files ({uploadQueue.length})
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {uploadQueue.map((upload) => {
              const previewUrl = previewUrls.get(upload.id);
              return (
                <Card key={upload.id} className="relative group">
                  <div className="aspect-square relative overflow-hidden rounded-t-lg">
                    {previewUrl ? (
                      <img
                        src={previewUrl}
                        alt={upload.file.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center bg-gray-100">
                        <div className="text-gray-400 text-2xl">📄</div>
                      </div>
                    )}
                    <Button
                      variant="destructive"
                      size="icon"
                      className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleRemoveFile(upload.id);
                      }}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                  <div className="p-2 space-y-1">
                    <div className="text-xs font-medium truncate" title={upload.file.name}>
                      {upload.file.name}
                    </div>
                    <div className="text-xs text-gray-500">
                      {formatFileSize(upload.file.size)}
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

