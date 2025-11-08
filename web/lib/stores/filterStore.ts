/**
 * Filter store for managing gallery filters and search state.
 * 
 * Manages state for:
 * - Selected tags for filtering
 * - Search query string
 * - Sort order
 * 
 * Actions:
 * - setSelectedTags: Set selected tags for filtering
 * - addTag: Add a tag to selected tags
 * - removeTag: Remove a tag from selected tags
 * - setSearchQuery: Set search query string
 * - setSortBy: Set sort order
 * - clearFilters: Clear all filters and search
 */

import { create } from 'zustand';

/**
 * Filter store state and actions.
 */
interface FilterStore {
  selectedTags: string[];
  searchQuery: string;
  sortBy: string;

  // Actions
  setSelectedTags: (tags: string[]) => void;
  addTag: (tag: string) => void;
  removeTag: (tag: string) => void;
  setSearchQuery: (query: string) => void;
  setSortBy: (sortBy: string) => void;
  clearFilters: () => void;
}

/**
 * Filter store instance.
 */
export const useFilterStore = create<FilterStore>((set) => ({
  selectedTags: [],
  searchQuery: '',
  sortBy: 'uploadDate',

  setSelectedTags: (tags) =>
    set({
      selectedTags: tags,
    }),

  addTag: (tag) =>
    set((state) => ({
      selectedTags: state.selectedTags.includes(tag)
        ? state.selectedTags
        : [...state.selectedTags, tag],
    })),

  removeTag: (tag) =>
    set((state) => ({
      selectedTags: state.selectedTags.filter((t) => t !== tag),
    })),

  setSearchQuery: (query) =>
    set({
      searchQuery: query,
    }),

  setSortBy: (sortBy) =>
    set({
      sortBy,
    }),

  clearFilters: () =>
    set({
      selectedTags: [],
      searchQuery: '',
      sortBy: 'uploadDate',
    }),
}));

