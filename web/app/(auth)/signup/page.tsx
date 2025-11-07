/**
 * Signup page component for RapidPhotoUpload web application.
 * 
 * Displays signup form for new user registration.
 * Redirects to gallery on successful signup.
 */

'use client';

import { SignupForm } from '@/components/auth/SignupForm';

export default function SignupPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-md space-y-8 p-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold">RapidPhotoUpload</h1>
          <p className="mt-2 text-gray-600">Create a new account</p>
        </div>
        <SignupForm />
      </div>
    </div>
  );
}

