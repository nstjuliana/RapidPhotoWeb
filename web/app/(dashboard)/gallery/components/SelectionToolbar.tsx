/**
 * SelectionToolbar Component
 * 
 * Fixed position toolbar that appears when photos are selected.
 * Provides batch actions: Tag All, Download Selected, Clear Selection.
 * 
 * @module app/(dashboard)/gallery/components
 */

'use client';

import { useState } from 'react';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { Button } from '@/components/ui/button';
import { BatchTagDialog } from './BatchTagDialog';

export function SelectionToolbar() {
  const selectedPhotos = useUploadStore((state) => state.selectedPhotos);
  const clearSelection = useUploadStore((state) => state.clearSelection);
  const setSelectMode = useUploadStore((state) => state.setSelectMode);
  const [isTagDialogOpen, setIsTagDialogOpen] = useState(false);

  const selectedCount = selectedPhotos.size;

  const handleCancel = () => {
    clearSelection();
    setSelectMode(false);
  };

  const handleDownload = () => {
    // TODO: Implement batch download when backend ZIP endpoint is available
    console.log('Download selected photos:', Array.from(selectedPhotos));
    alert('Batch download will be available when backend ZIP endpoint is implemented');
  };

  if (selectedCount === 0) {
    return null;
  }

  return (
    <>
      <div className="fixed bottom-0 left-0 right-0 z-50 border-t bg-white shadow-lg">
        <div className="mx-auto max-w-7xl px-4 py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <span className="font-medium text-gray-900">
                {selectedCount} photo{selectedCount !== 1 ? 's' : ''} selected
              </span>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => setIsTagDialogOpen(true)}
              >
                Tag All
              </Button>
              <Button variant="outline" onClick={handleDownload}>
                Download
              </Button>
              <Button variant="ghost" onClick={handleCancel}>
                Cancel
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Batch Tag Dialog */}
      {isTagDialogOpen && (
        <BatchTagDialog
          open={isTagDialogOpen}
          onOpenChange={setIsTagDialogOpen}
        />
      )}
    </>
  );
}

