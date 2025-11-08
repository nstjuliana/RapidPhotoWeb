/**
 * BatchTagInput Component
 * 
 * Tag input with chips display for batch tagging.
 * 
 * Features:
 * - Tag input field with Enter key support
 * - Tag chips with remove buttons
 * - Tag validation (non-empty, unique)
 * - Integration with upload store (batchTags state)
 */

'use client';

import { useState, useEffect, KeyboardEvent } from 'react';
import { useUploadStore } from '@/lib/stores/uploadStore';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { X } from 'lucide-react';

/**
 * BatchTagInput component.
 */
export function BatchTagInput() {
  const [isMounted, setIsMounted] = useState(false);
  const { batchTags, setBatchTags } = useUploadStore();
  const [inputValue, setInputValue] = useState('');

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted) {
    return (
      <div className="space-y-2">
        <label className="text-sm font-medium">Batch Tags</label>
        <Input type="text" placeholder="Add tags (press Enter)" disabled />
      </div>
    );
  }

  /**
   * Add a tag to the batch tags.
   */
  const addTag = (tag: string) => {
    const trimmedTag = tag.trim().toLowerCase();
    
    // Validate tag
    if (!trimmedTag) {
      return;
    }

    // Check for duplicates
    if (batchTags.includes(trimmedTag)) {
      setInputValue('');
      return;
    }

    // Add tag
    setBatchTags([...batchTags, trimmedTag]);
    setInputValue('');
  };

  /**
   * Remove a tag from batch tags.
   */
  const removeTag = (tagToRemove: string) => {
    setBatchTags(batchTags.filter((tag) => tag !== tagToRemove));
  };

  /**
   * Handle Enter key press in input.
   */
  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addTag(inputValue);
    }
  };

  /**
   * Handle input blur (add tag when focus leaves).
   */
  const handleBlur = () => {
    if (inputValue.trim()) {
      addTag(inputValue);
    }
  };

  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">Batch Tags</label>
      <div className="space-y-2">
        {/* Tag Input */}
        <Input
          type="text"
          placeholder="Add tags (press Enter)"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          onBlur={handleBlur}
        />

        {/* Tag Chips */}
        {batchTags.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {batchTags.map((tag) => (
              <Badge
                key={tag}
                variant="secondary"
                className="gap-1 pr-1"
              >
                {tag}
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-4 w-4 p-0 hover:bg-transparent"
                  onClick={() => removeTag(tag)}
                >
                  <X className="h-3 w-3" />
                </Button>
              </Badge>
            ))}
          </div>
        )}

        {/* Helper Text */}
        <p className="text-xs text-gray-500">
          Tags will be applied to all photos in this upload batch
        </p>
      </div>
    </div>
  );
}

