/**
 * Upload page component for RapidPhotoUpload web application.
 * 
 * Placeholder page for photo upload feature.
 * Full implementation coming in Phase 6.
 */

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function UploadPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Upload Photos</h1>
        <p className="text-gray-600 mt-2">Upload and manage your photos</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Photo Upload</CardTitle>
          <CardDescription>Coming in Phase 6</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-12">
            <div className="inline-block p-4 bg-gray-100 rounded-full mb-4">
              <svg
                className="w-12 h-12 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                />
              </svg>
            </div>
            <p className="text-gray-500 text-lg">Photo Upload feature will be available here</p>
            <p className="text-gray-400 text-sm mt-2">
              You'll be able to upload up to 100 photos simultaneously with progress tracking
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

