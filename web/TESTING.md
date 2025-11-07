# Phase 5: Web Foundation - Testing Guide

This guide explains how to test the Phase 5 implementation of the RapidPhotoUpload web application.

## Prerequisites

1. **PostgreSQL Database** running on `localhost:5432`
   - Database name: `rapid_photo_upload`
   - Username: `postgres`
   - Password: `postgres` (or set via `DB_PASSWORD` env var)

2. **Backend API** running on `http://localhost:8080`

3. **Node.js** and **npm** installed

## Step 1: Start the Backend

### Option A: Using Maven (Recommended)

```bash
cd backend
mvn spring-boot:run
```

### Option B: Using Maven Wrapper

```bash
cd backend
./mvnw spring-boot:run
# On Windows:
.\mvnw.cmd spring-boot:run
```

### Verify Backend is Running

- Open browser: `http://localhost:8080/actuator/health`
- Should return: `{"status":"UP"}`

## Step 2: Start the Frontend

```bash
cd web
npm run dev
```

The frontend will start on `http://localhost:3000`

## Step 3: Testing Checklist

### ✅ Authentication Flow

#### Test 1: Login Page
1. Navigate to `http://localhost:3000`
2. Should redirect to `/login`
3. Verify login form displays:
   - Email input field
   - Password input field
   - "Sign In" button
   - Link to signup page

#### Test 2: Login with Valid Credentials
**Option A: Sign up first (Recommended)**
1. First, create an account using the signup flow (Test 5)
2. Then logout and login with those credentials

**Option B: Use existing test user**
1. The backend has a test user, but password hashing makes it difficult to use
2. For Phase 5 testing, it's easier to signup a new user first
3. After signup, logout and login with those credentials
4. Should redirect to `/gallery`
5. Should see dashboard header with navigation

#### Test 3: Login with Invalid Credentials
1. Enter invalid email/password
2. Should display error message: "Invalid email or password"
3. Should remain on login page

#### Test 4: Form Validation
1. Try submitting empty form → Should show validation error
2. Enter invalid email format → Should show validation error
3. Enter password < 6 characters → Should show validation error

#### Test 5: Signup Flow
1. Click "Sign up" link on login page
2. Should navigate to `/signup`
3. Fill in form:
   - Email: `newuser@example.com`
   - Password: `password123`
   - Confirm Password: `password123`
4. Click "Sign Up"
5. Should create account and redirect to `/gallery`

#### Test 6: Signup Validation
1. Try passwords that don't match → Should show error
2. Try existing email → Should show error
3. Try invalid email format → Should show error

### ✅ Route Protection

#### Test 7: Protected Routes (Unauthenticated)
1. Clear browser localStorage (or use incognito)
2. Navigate directly to `http://localhost:3000/gallery`
3. Should redirect to `/login`

#### Test 8: Protected Routes (Authenticated)
1. Login successfully
2. Navigate to `/gallery` → Should display gallery page
3. Navigate to `/upload` → Should display upload page
4. Should see header with navigation

#### Test 9: Root Redirect
1. When authenticated: Navigate to `/` → Should redirect to `/gallery`
2. When not authenticated: Navigate to `/` → Should redirect to `/login`

### ✅ Navigation

#### Test 10: Header Navigation
1. After login, verify header displays:
   - "RapidPhotoUpload" logo (clickable, links to gallery)
   - Navigation links: "Gallery", "Upload"
   - "Logout" button

#### Test 11: Active Route Highlighting
1. Navigate to `/gallery` → "Gallery" link should be highlighted
2. Navigate to `/upload` → "Upload" link should be highlighted

#### Test 12: Navigation Links
1. Click "Gallery" → Should navigate to `/gallery`
2. Click "Upload" → Should navigate to `/upload`
3. Click logo → Should navigate to `/gallery`

### ✅ Logout

#### Test 13: Logout Functionality
1. While authenticated, click "Logout" button
2. Should clear authentication token
3. Should redirect to `/login`
4. Try navigating to `/gallery` → Should redirect back to `/login`

### ✅ Placeholder Pages

#### Test 14: Gallery Placeholder
1. Navigate to `/gallery`
2. Should see:
   - Page title: "Gallery"
   - Card with "Photo Gallery - Coming in Phase 7"
   - Placeholder icon and message

#### Test 15: Upload Placeholder
1. Navigate to `/upload`
2. Should see:
   - Page title: "Upload Photos"
   - Card with "Photo Upload - Coming in Phase 6"
   - Placeholder icon and message

### ✅ API Integration

#### Test 16: API Client Configuration
1. Open browser DevTools → Network tab
2. Login with valid credentials
3. Verify:
   - Request to `POST http://localhost:8080/api/auth/login`
   - Request includes `Content-Type: application/json`
   - Response contains `token`, `userId`, `email`

#### Test 17: Token Storage
1. After successful login, check browser DevTools → Application → Local Storage
2. Should see `auth_token` key with token value

#### Test 18: Automatic Token Injection
1. After login, check Network tab for any API requests
2. Verify `Authorization: Bearer <token>` header is included

#### Test 19: 401 Error Handling
1. Manually edit localStorage `auth_token` to invalid value
2. Try to navigate to protected route
3. Should detect 401 error and redirect to `/login`
4. Token should be cleared from localStorage

### ✅ State Management

#### Test 20: Zustand Stores
1. Open browser DevTools → Console
2. Type: `window.useUploadStore` (if exposed for debugging)
3. Or verify stores are working by checking:
   - No console errors related to Zustand
   - UI state updates correctly (select mode, filters, etc.)

#### Test 21: TanStack Query
1. Verify no console errors related to React Query
2. Check that queries are cached properly
3. Verify loading states work correctly

## Step 4: Browser Console Checks

Open browser DevTools Console and verify:

- ✅ No TypeScript errors
- ✅ No React errors
- ✅ No API errors (except intentional test cases)
- ✅ No CORS errors
- ✅ No authentication errors (after successful login)

## Step 5: Visual Checks

### Login Page
- ✅ Form is centered on page
- ✅ Input fields are styled correctly
- ✅ Button has proper hover states
- ✅ Error messages display in red
- ✅ Link to signup is visible

### Dashboard
- ✅ Header is fixed at top
- ✅ Navigation links are visible
- ✅ Logout button is visible
- ✅ Main content area has proper spacing
- ✅ Placeholder pages display correctly

## Troubleshooting

### Backend Not Starting
- Check PostgreSQL is running
- Verify database exists: `rapid_photo_upload`
- Check backend logs for errors
- Verify port 8080 is not in use

### Frontend Not Starting
- Check Node.js version (should be 18+)
- Run `npm install` to ensure dependencies are installed
- Check for port conflicts (default: 3000)

### Authentication Not Working
- Verify backend is running on `http://localhost:8080`
- Check `.env.local` has correct `NEXT_PUBLIC_API_URL`
- Check browser console for CORS errors
- Verify backend CORS configuration allows `http://localhost:3000`

### Routes Not Working
- Clear browser cache
- Check Next.js dev server logs
- Verify route files are in correct locations
- Check for TypeScript compilation errors

## Quick Test Script

Run this in browser console after login to verify state:

```javascript
// Check token is stored
console.log('Token:', localStorage.getItem('auth_token'));

// Check if authenticated (should be true)
// This requires accessing the hook, but you can verify by:
// 1. Token exists in localStorage
// 2. Can navigate to /gallery without redirect
```

## Expected Behavior Summary

1. **Unauthenticated users**: Always redirected to `/login`
2. **Authenticated users**: Can access `/gallery` and `/upload`
3. **Login/Signup**: Forms validate input and show errors appropriately
4. **Navigation**: Active route is highlighted, links work correctly
5. **Logout**: Clears token and redirects to login
6. **API Integration**: Token is automatically included in requests
7. **Error Handling**: 401 errors trigger logout and redirect

## Next Steps

After verifying Phase 5 works correctly:
- Phase 6: Implement photo upload feature
- Phase 7: Implement photo gallery feature
- Phase 8: Enhance authentication with JWT

