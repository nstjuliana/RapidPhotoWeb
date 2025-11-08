/**
 * S3 upload utility for direct file uploads using presigned URLs.
 * 
 * Uses XMLHttpRequest for upload progress tracking (fetch API doesn't support
 * upload progress events). Handles direct uploads to S3 with progress callbacks
 * and proper error handling.
 */

/**
 * Upload a file directly to S3 using a presigned URL with progress tracking.
 * 
 * @param file The file to upload
 * @param presignedUrl The presigned S3 URL for PUT operation
 * @param onProgress Progress callback function called with progress percentage (0-100)
 * @returns Promise that resolves when upload completes successfully
 * @throws Error if upload fails (network error, S3 error, etc.)
 * 
 * @example
 * ```typescript
 * await uploadToS3(file, presignedUrl, (progress) => {
 *   console.log(`Upload progress: ${progress}%`);
 * });
 * ```
 */
export async function uploadToS3(
  file: File,
  presignedUrl: string,
  onProgress: (progress: number) => void
): Promise<void> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();

    // Set up progress tracking
    xhr.upload.addEventListener('progress', (event) => {
      if (event.lengthComputable) {
        const progress = Math.round((event.loaded / event.total) * 100);
        onProgress(progress);
      }
    });

    // Handle successful upload
    xhr.addEventListener('load', () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve();
      } else if (xhr.status === 403) {
        // 403 often indicates CORS or permission issues
        reject(
          new Error(
            'S3 upload failed: Access denied. Check S3 bucket CORS configuration and permissions.'
          )
        );
      } else {
        // S3 returns 200-299 for successful uploads
        // Other status codes indicate errors
        reject(
          new Error(
            `S3 upload failed with status ${xhr.status}: ${xhr.statusText}`
          )
        );
      }
    });

    // Handle network errors
    xhr.addEventListener('error', () => {
      // Check if it's a CORS error
      if (xhr.status === 0 || xhr.status === 403) {
        reject(
          new Error(
            'CORS error: S3 bucket is not configured to allow uploads from this origin. Please configure CORS on the S3 bucket.'
          )
        );
      } else {
        reject(new Error('Network error during S3 upload'));
      }
    });

    // Handle upload abort
    xhr.addEventListener('abort', () => {
      reject(new Error('S3 upload was aborted'));
    });

    // Handle timeout
    xhr.addEventListener('timeout', () => {
      reject(new Error('S3 upload timed out'));
    });

    // Configure request
    xhr.open('PUT', presignedUrl);
    xhr.setRequestHeader('Content-Type', file.type);

    // Set timeout (5 minutes for large files)
    xhr.timeout = 5 * 60 * 1000;

    // Start upload
    xhr.send(file);
  });
}

