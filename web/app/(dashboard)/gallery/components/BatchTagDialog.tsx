/**
 * BatchTagDialog Component
 * 
 * Modal dialog for batch tagging multiple photos.
 * Allows adding tags to all selected photos with progress indication.
 * 
 * @module app/(dashboard)/gallery/components
 */

'use client';

import { useState } from 'react';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { useAddPhotoTags } from '@/lib/queries/photoQueries';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';

interface BatchTagDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function BatchTagDialog({ open, onOpenChange }: BatchTagDialogProps) {
  const selectedPhotos = useUploadStore((state) => state.selectedPhotos);
  const [tagInput, setTagInput] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const addPhotoTagsMutation = useAddPhotoTags();

  const handleAddTag = () => {
    const trimmedTag = tagInput.trim();
    if (trimmedTag && !tags.includes(trimmedTag)) {
      setTags([...tags, trimmedTag]);
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter((tag) => tag !== tagToRemove));
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddTag();
    }
  };

  const handleApplyTags = async () => {
    if (tags.length === 0) {
      return;
    }

    const photoIds = Array.from(selectedPhotos);
    
    try {
      // Apply tags to all selected photos
      await Promise.all(
        photoIds.map((photoId) =>
          addPhotoTagsMutation.mutateAsync({
            photoId,
            tags,
          })
        )
      );
      
      // Clear selection and close dialog
      setTags([]);
      setTagInput('');
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to apply tags:', error);
    }
  };

  const selectedCount = selectedPhotos.size;
  const isProcessing = addPhotoTagsMutation.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add Tags to {selectedCount} Photos</DialogTitle>
          <DialogDescription>
            Add tags to all selected photos. Tags will be applied to each photo.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Tag Input */}
          <div className="flex gap-2">
            <Input
              type="text"
              placeholder="Enter a tag and press Enter"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isProcessing}
            />
            <Button onClick={handleAddTag} disabled={isProcessing}>
              Add
            </Button>
          </div>

          {/* Tags Display */}
          {tags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {tags.map((tag) => (
                <Badge
                  key={tag}
                  variant="secondary"
                  className="flex items-center gap-1 pr-1"
                >
                  {tag}
                  <button
                    onClick={() => handleRemoveTag(tag)}
                    className="ml-1 rounded-full hover:bg-gray-200"
                    disabled={isProcessing}
                  >
                    <svg
                      className="h-3 w-3"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M6 18L18 6M6 6l12 12"
                      />
                    </svg>
                  </button>
                </Badge>
              ))}
            </div>
          )}

          {/* Processing Indicator */}
          {isProcessing && (
            <div className="text-sm text-gray-600">
              Applying tags to {selectedCount} photos...
            </div>
          )}

          {/* Actions */}
          <div className="flex justify-end gap-2">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={handleApplyTags}
              disabled={tags.length === 0 || isProcessing}
            >
              {isProcessing ? 'Applying...' : 'Apply Tags'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

