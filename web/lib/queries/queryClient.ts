/**
 * TanStack Query client configuration.
 * 
 * Configures QueryClient with default options:
 * - Stale time: 5 minutes
 * - Cache time: 10 minutes
 * - Retry logic: 2 retries
 */

import { QueryClient } from '@tanstack/react-query';

/**
 * Configured QueryClient instance.
 * 
 * Default options:
 * - staleTime: 5 minutes (data considered fresh for 5 minutes)
 * - gcTime: 10 minutes (formerly cacheTime, unused data garbage collected after 10 minutes)
 * - retry: 2 retries on failure
 * - refetchOnWindowFocus: false (don't refetch when window regains focus)
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      retry: 2,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 1,
    },
  },
});

