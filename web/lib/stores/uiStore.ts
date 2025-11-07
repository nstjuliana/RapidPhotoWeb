/**
 * UI store for managing UI state.
 * 
 * Manages state for:
 * - Select mode (multi-select mode for photos)
 * - Filters (tag filters, search filters)
 * - Sidebar open state
 * 
 * Actions:
 * - toggleSelectMode: Toggle multi-select mode on/off
 * - setFilters: Set filter values
 * - toggleSidebar: Toggle sidebar open/closed
 */

import { create } from 'zustand';

/**
 * Filter state for photo gallery.
 */
export interface FilterState {
  tags?: string[];
  search?: string;
  dateRange?: {
    start?: Date;
    end?: Date;
  };
}

/**
 * UI store state and actions.
 */
interface UIStore {
  isSelectMode: boolean;
  filters: FilterState;
  sidebarOpen: boolean;

  // Actions
  toggleSelectMode: () => void;
  setFilters: (filters: FilterState) => void;
  toggleSidebar: () => void;
  resetFilters: () => void;
}

/**
 * UI store instance.
 */
export const useUIStore = create<UIStore>((set) => ({
  isSelectMode: false,
  filters: {},
  sidebarOpen: false,

  toggleSelectMode: () =>
    set((state) => ({
      isSelectMode: !state.isSelectMode,
    })),

  setFilters: (filters) =>
    set({
      filters,
    }),

  toggleSidebar: () =>
    set((state) => ({
      sidebarOpen: !state.sidebarOpen,
    })),

  resetFilters: () =>
    set({
      filters: {},
    }),
}));

