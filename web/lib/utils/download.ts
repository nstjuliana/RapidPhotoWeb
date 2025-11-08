/**
 * Download utility functions for photo downloads.
 * 
 * Provides:
 * - downloadSinglePhoto: Trigger browser download for a single photo
 * - downloadMultiplePhotos: Prepare for batch ZIP download (when backend endpoint available)
 * 
 * @module lib/utils/download
 */

/**
 * Download a single photo by triggering browser download.
 * 
 * @param photoUrl Presigned URL or download URL for the photo
 * @param filename Filename to save the photo as
 */
export async function downloadSinglePhoto(
  photoUrl: string,
  filename: string
): Promise<void> {
  try {
    // Fetch the photo
    const response = await fetch(photoUrl);
    if (!response.ok) {
      throw new Error(`Failed to fetch photo: ${response.statusText}`);
    }

    // Get blob from response
    const blob = await response.blob();

    // Create object URL
    const objectUrl = URL.createObjectURL(blob);

    // Create temporary anchor element and trigger download
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();

    // Cleanup
    document.body.removeChild(link);
    URL.revokeObjectURL(objectUrl);
  } catch (error) {
    console.error('Download failed:', error);
    throw error;
  }
}

/**
 * Download multiple photos as a ZIP file.
 * 
 * Note: This requires a backend endpoint that generates ZIP files.
 * Currently returns a placeholder implementation.
 * 
 * @param photoIds Array of photo IDs to download
 * @returns Promise that resolves when download starts
 */
export async function downloadMultiplePhotos(
  photoIds: string[]
): Promise<void> {
  // TODO: Implement when backend ZIP endpoint is available
  // This would call an endpoint like: POST /api/photos/batch-download
  // with body: { photoIds: string[] }
  // The backend would generate a ZIP and return a download URL
  
  throw new Error(
    'Batch download not yet implemented. Backend ZIP endpoint required.'
  );
}

