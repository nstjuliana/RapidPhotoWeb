/**
 * TanStack Query hooks for photo operations.
 * 
 * Provides hooks for:
 * - usePhotos: Infinite query for photo list with pagination
 * - usePhoto: Query for single photo detail
 * - usePhotoDownloadUrl: Query for download URL
 * - useAddPhotoTags: Mutation for adding tags
 * - useRemovePhotoTags: Mutation for removing tags
 * - useUpdatePhotoTags: Mutation for replacing tags
 */

'use client';

import { useInfiniteQuery, useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  listPhotos,
  getPhoto,
  getPhotoDownloadUrl,
  addPhotoTags,
  removePhotoTags,
  replacePhotoTags,
} from '@/lib/api/endpoints';
import { photoKeys } from './keys';
import type { PhotoDto, ListPhotosParams } from '@/lib/api/types';

/**
 * Hook for fetching photos with infinite scroll pagination.
 * 
 * @param userId User ID to fetch photos for
 * @param filters Filter parameters (tags, search query)
 * @param pageSize Number of photos per page (default: 20)
 * @returns Infinite query result with photos and pagination
 */
export function usePhotos(
  userId: string | null,
  filters?: { tags?: string[]; search?: string },
  pageSize: number = 20
) {
  return useInfiniteQuery({
    queryKey: photoKeys.list(filters),
    queryFn: async ({ pageParam = 0 }) => {
      if (!userId) {
        return { photos: [], nextPage: null };
      }

      const params: ListPhotosParams = {
        userId,
        page: pageParam,
        size: pageSize,
        sortBy: 'uploadDate',
        tags: filters?.tags,
      };

      const photos = await listPhotos(params);

      // Apply client-side search filtering if search query provided
      let filteredPhotos = photos;
      if (filters?.search && filters.search.trim()) {
        const searchLower = filters.search.toLowerCase();
        filteredPhotos = photos.filter(
          (photo) =>
            photo.filename.toLowerCase().includes(searchLower) ||
            photo.tags.some((tag) => tag.toLowerCase().includes(searchLower))
        );
      }

      return {
        photos: filteredPhotos,
        nextPage: filteredPhotos.length === pageSize ? pageParam + 1 : null,
      };
    },
    getNextPageParam: (lastPage) => lastPage.nextPage,
    initialPageParam: 0,
    enabled: !!userId,
  });
}

/**
 * Hook for fetching a single photo by ID.
 * 
 * @param photoId Photo ID to fetch
 * @returns Query result with photo data
 */
export function usePhoto(photoId: string | null) {
  return useQuery({
    queryKey: photoKeys.detail(photoId || ''),
    queryFn: () => getPhoto(photoId!),
    enabled: !!photoId,
  });
}

/**
 * Hook for fetching download URL for a photo.
 * 
 * @param photoId Photo ID to get download URL for
 * @returns Query result with download URL
 */
export function usePhotoDownloadUrl(photoId: string | null) {
  return useQuery({
    queryKey: [...photoKeys.detail(photoId || ''), 'download'],
    queryFn: () => getPhotoDownloadUrl(photoId!),
    enabled: !!photoId,
    staleTime: 50 * 60 * 1000, // 50 minutes (URLs expire after 60 minutes)
  });
}

/**
 * Hook for adding tags to a photo.
 * 
 * @returns Mutation for adding tags with optimistic updates
 */
export function useAddPhotoTags() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ photoId, tags }: { photoId: string; tags: string[] }) =>
      addPhotoTags(photoId, tags),
    onMutate: async ({ photoId, tags }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: photoKeys.detail(photoId) });

      // Snapshot previous value
      const previousPhoto = queryClient.getQueryData<PhotoDto>(
        photoKeys.detail(photoId)
      );

      // Optimistically update photo
      if (previousPhoto) {
        const updatedPhoto: PhotoDto = {
          ...previousPhoto,
          tags: [...new Set([...previousPhoto.tags, ...tags])],
        };
        queryClient.setQueryData(photoKeys.detail(photoId), updatedPhoto);
      }

      // Invalidate list queries to refetch
      queryClient.invalidateQueries({ queryKey: photoKeys.all });

      return { previousPhoto };
    },
    onError: (_error, _variables, context) => {
      // Rollback on error
      if (context?.previousPhoto) {
        queryClient.setQueryData(
          photoKeys.detail(_variables.photoId),
          context.previousPhoto
        );
      }
    },
    onSettled: (_data, _error, variables) => {
      // Refetch to ensure consistency
      queryClient.invalidateQueries({ queryKey: photoKeys.detail(variables.photoId) });
      queryClient.invalidateQueries({ queryKey: photoKeys.all });
    },
  });
}

/**
 * Hook for removing tags from a photo.
 * 
 * @returns Mutation for removing tags with optimistic updates
 */
export function useRemovePhotoTags() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ photoId, tags }: { photoId: string; tags: string[] }) =>
      removePhotoTags(photoId, tags),
    onMutate: async ({ photoId, tags }) => {
      await queryClient.cancelQueries({ queryKey: photoKeys.detail(photoId) });

      const previousPhoto = queryClient.getQueryData<PhotoDto>(
        photoKeys.detail(photoId)
      );

      if (previousPhoto) {
        const updatedPhoto: PhotoDto = {
          ...previousPhoto,
          tags: previousPhoto.tags.filter((tag) => !tags.includes(tag)),
        };
        queryClient.setQueryData(photoKeys.detail(photoId), updatedPhoto);
      }

      queryClient.invalidateQueries({ queryKey: photoKeys.all });

      return { previousPhoto };
    },
    onError: (_error, _variables, context) => {
      if (context?.previousPhoto) {
        queryClient.setQueryData(
          photoKeys.detail(_variables.photoId),
          context.previousPhoto
        );
      }
    },
    onSettled: (_data, _error, variables) => {
      queryClient.invalidateQueries({ queryKey: photoKeys.detail(variables.photoId) });
      queryClient.invalidateQueries({ queryKey: photoKeys.all });
    },
  });
}

/**
 * Hook for replacing all tags on a photo.
 * 
 * @returns Mutation for replacing tags with optimistic updates
 */
export function useUpdatePhotoTags() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ photoId, tags }: { photoId: string; tags: string[] }) =>
      replacePhotoTags(photoId, tags),
    onMutate: async ({ photoId, tags }) => {
      await queryClient.cancelQueries({ queryKey: photoKeys.detail(photoId) });

      const previousPhoto = queryClient.getQueryData<PhotoDto>(
        photoKeys.detail(photoId)
      );

      if (previousPhoto) {
        const updatedPhoto: PhotoDto = {
          ...previousPhoto,
          tags,
        };
        queryClient.setQueryData(photoKeys.detail(photoId), updatedPhoto);
      }

      queryClient.invalidateQueries({ queryKey: photoKeys.all });

      return { previousPhoto };
    },
    onError: (_error, _variables, context) => {
      if (context?.previousPhoto) {
        queryClient.setQueryData(
          photoKeys.detail(_variables.photoId),
          context.previousPhoto
        );
      }
    },
    onSettled: (_data, _error, variables) => {
      queryClient.invalidateQueries({ queryKey: photoKeys.detail(variables.photoId) });
      queryClient.invalidateQueries({ queryKey: photoKeys.all });
    },
  });
}

