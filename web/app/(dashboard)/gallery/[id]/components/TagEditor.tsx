/**
 * TagEditor Component
 * 
 * Inline tag editor for adding and removing tags from a photo.
 * Features:
 * - Display current tags as removable chips
 * - Add new tags with autocomplete suggestions
 * - Remove tags with optimistic updates
 * - Auto-save on changes
 * 
 * @module app/(dashboard)/gallery/[id]/components
 */

'use client';

import { useState } from 'react';
import { useAddPhotoTags, useRemovePhotoTags } from '@/lib/queries/photoQueries';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

interface TagEditorProps {
  photoId: string;
  initialTags: string[];
}

export function TagEditor({ photoId, initialTags }: TagEditorProps) {
  const [tags, setTags] = useState<string[]>(initialTags);
  const [tagInput, setTagInput] = useState('');
  const [suggestions, setSuggestions] = useState<string[]>([]);
  
  const addPhotoTagsMutation = useAddPhotoTags();
  const removePhotoTagsMutation = useRemovePhotoTags();

  // TODO: Fetch available tags for autocomplete from all photos
  // For now, suggestions are empty
  const availableTags: string[] = [];

  const handleAddTag = async () => {
    const trimmedTag = tagInput.trim().toLowerCase();
    if (!trimmedTag || tags.includes(trimmedTag)) {
      setTagInput('');
      return;
    }

    // Optimistic update
    const newTags = [...tags, trimmedTag];
    setTags(newTags);
    setTagInput('');

    try {
      await addPhotoTagsMutation.mutateAsync({
        photoId,
        tags: [trimmedTag],
      });
    } catch (error) {
      // Rollback on error
      setTags(tags);
      console.error('Failed to add tag:', error);
    }
  };

  const handleRemoveTag = async (tagToRemove: string) => {
    // Optimistic update
    const newTags = tags.filter((tag) => tag !== tagToRemove);
    setTags(newTags);

    try {
      await removePhotoTagsMutation.mutateAsync({
        photoId,
        tags: [tagToRemove],
      });
    } catch (error) {
      // Rollback on error
      setTags(tags);
      console.error('Failed to remove tag:', error);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddTag();
    } else if (e.key === 'Escape') {
      setTagInput('');
      setSuggestions([]);
    }
  };

  const handleInputChange = (value: string) => {
    setTagInput(value);
    
    // Simple autocomplete - filter available tags
    if (value.trim()) {
      const filtered = availableTags.filter(
        (tag) =>
          tag.toLowerCase().includes(value.toLowerCase()) &&
          !tags.includes(tag)
      );
      setSuggestions(filtered.slice(0, 5));
    } else {
      setSuggestions([]);
    }
  };

  const handleSuggestionClick = (suggestion: string) => {
    setTagInput(suggestion);
    setSuggestions([]);
  };

  const isProcessing =
    addPhotoTagsMutation.isPending || removePhotoTagsMutation.isPending;

  return (
    <div className="space-y-3">
      {/* Current Tags */}
      {tags.length > 0 ? (
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
                aria-label={`Remove ${tag} tag`}
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
      ) : (
        <p className="text-sm text-gray-500">No tags yet. Add tags to organize your photos.</p>
      )}

      {/* Tag Input */}
      <div className="relative">
        <div className="flex gap-2">
          <Input
            type="text"
            placeholder="Add a tag..."
            value={tagInput}
            onChange={(e) => handleInputChange(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={isProcessing}
            className="flex-1"
          />
          <Button onClick={handleAddTag} disabled={isProcessing || !tagInput.trim()}>
            Add
          </Button>
        </div>

        {/* Autocomplete Suggestions */}
        {suggestions.length > 0 && (
          <div className="absolute left-0 top-full z-10 mt-1 w-full rounded-md border bg-white shadow-lg">
            {suggestions.map((suggestion) => (
              <button
                key={suggestion}
                onClick={() => handleSuggestionClick(suggestion)}
                className="w-full px-3 py-2 text-left text-sm hover:bg-gray-100"
              >
                {suggestion}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Processing Indicator */}
      {isProcessing && (
        <p className="text-xs text-gray-500">Updating tags...</p>
      )}
    </div>
  );
}

