/**
 * Query key factories for TanStack Query.
 * 
 * Provides type-safe, hierarchical query keys for consistent
 * cache management across the application.
 * 
 * Usage:
 * - photoKeys.all - All photos
 * - photoKeys.detail(id) - Single photo by ID
 * - photoKeys.list(filters) - Filtered photo list
 */

/**
 * Photo query keys factory.
 */
export const photoKeys = {
  /**
   * Base key for all photo queries.
   */
  all: ['photos'] as const,

  /**
   * Key for a single photo detail query.
   * 
   * @param id Photo ID
   */
  detail: (id: string) => [...photoKeys.all, 'detail', id] as const,

  /**
   * Key for a filtered photo list query.
   * 
   * @param filters Filter parameters (tags, date range, etc.)
   */
  list: (filters?: { tags?: string[]; search?: string }) =>
    [...photoKeys.all, 'list', filters] as const,
};

/**
 * Tag query keys factory.
 */
export const tagKeys = {
  /**
   * Base key for all tag queries.
   */
  all: ['tags'] as const,

  /**
   * Key for all tags list query.
   */
  list: () => [...tagKeys.all, 'list'] as const,
};

/**
 * Auth query keys factory.
 */
export const authKeys = {
  /**
   * Base key for all auth queries.
   */
  all: ['auth'] as const,

  /**
   * Key for current user query.
   */
  user: () => [...authKeys.all, 'user'] as const,
};

