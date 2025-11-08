/**
 * S3 upload utility for direct file uploads using presigned URLs.
 * 
 * Uses XMLHttpRequest for upload progress tracking (fetch API doesn't support
 * upload progress events). Handles direct uploads to S3 with progress callbacks
 * and proper error handling.
 */

/**
 * Throttle progress updates to prevent excessive re-renders.
 * Only calls the callback if enough time has passed or progress increased significantly.
 */
function createThrottledProgressCallback(
  callback: (progress: number) => void,
  throttleMs: number = 100,
  minIncrement: number = 1
): (progress: number) => void {
  let lastProgress = 0;
  let lastUpdateTime = 0;

  return (progress: number) => {
    const now = Date.now();
    const timeSinceLastUpdate = now - lastUpdateTime;
    const progressIncrement = progress - lastProgress;

    // Update if:
    // 1. Enough time has passed (throttle), OR
    // 2. Progress increased by minimum increment, OR
    // 3. Progress reached 100%
    if (
      timeSinceLastUpdate >= throttleMs ||
      progressIncrement >= minIncrement ||
      progress >= 100
    ) {
      lastProgress = progress;
      lastUpdateTime = now;
      callback(progress);
    }
  };
}

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

    // Create throttled progress callback to prevent excessive updates
    const throttledProgress = createThrottledProgressCallback(onProgress);

    // Track last reported progress to ensure it only increases
    let lastReportedProgress = 0;

    // Set up progress tracking
    xhr.upload.addEventListener('progress', (event) => {
      if (event.lengthComputable) {
        const rawProgress = (event.loaded / event.total) * 100;
        // Round to nearest integer and ensure it only increases
        const progress = Math.max(
          lastReportedProgress,
          Math.round(rawProgress)
        );
        // Clamp to 0-100
        const clampedProgress = Math.min(100, Math.max(0, progress));
        
        // Only update if progress actually increased
        if (clampedProgress > lastReportedProgress) {
          lastReportedProgress = clampedProgress;
          throttledProgress(clampedProgress);
        }
      }
    });

    // Handle successful upload
    xhr.addEventListener('load', () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        // Ensure progress is set to 100% on completion
        if (lastReportedProgress < 100) {
          onProgress(100);
        }
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

