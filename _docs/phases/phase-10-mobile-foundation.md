# Phase 10: Mobile Foundation

## Goal

Establish the mobile application foundation using Expo (managed workflow), React Navigation, state management, JWT authentication flow, and API client setup. This phase creates the scaffolding for the mobile app that mirrors the web application functionality.

## Deliverables

- Expo project initialized and configured
- React Navigation setup with stack and tab navigators
- Zustand and TanStack Query configured
- JWT authentication screens and flow
- API client setup for backend integration
- Basic navigation structure
- Development environment ready

## Prerequisites

- Phase 9 completed (web JWT authentication working)
- Backend API fully functional with JWT
- Expo CLI installed
- iOS Simulator or Android Emulator (or physical device)

## Features

### 1. Expo Project Initialization

**Goal:** Set up Expo project with TypeScript and essential configuration.

**Steps:**
1. Initialize Expo project in `mobile/` directory:
   - Use `npx create-expo-app` with TypeScript template
   - Configure `app.json` with app metadata
   - Set up project structure following mobile conventions
2. Configure TypeScript:
   - `tsconfig.json` with strict mode
   - Path aliases (@/ for src directory)
   - Type definitions for React Native
3. Install core dependencies:
   - React Navigation v6+
   - Zustand, TanStack Query
   - Axios for API calls
   - Secure storage for tokens
4. Configure Expo SDK:
   - Set SDK version in `package.json`
   - Configure `app.json` with required permissions
   - Set up development build configuration
5. Test project runs:
   - Start Expo dev server
   - Run on iOS simulator or Android emulator
   - Verify basic app displays

**Success Criteria:**
- Expo project initialized successfully
- TypeScript configured correctly
- Dependencies installed
- App runs on simulator/emulator
- Project structure organized

---

### 2. React Navigation Setup

**Goal:** Configure navigation structure with stack and tab navigators.

**Steps:**
1. Install React Navigation dependencies:
   - `@react-navigation/native`, `@react-navigation/stack`, `@react-navigation/bottom-tabs`
   - `react-native-screens`, `react-native-safe-area-context`
2. Create navigation structure:
   - `src/navigation/AppNavigator.tsx` - Root navigator
   - `src/navigation/AuthNavigator.tsx` - Auth stack (Login, Signup)
   - `src/navigation/MainNavigator.tsx` - Main app stack (Gallery, Upload, Detail)
   - Optional: Tab navigator for main screens
3. Define navigation types:
   - `src/navigation/types.ts` - TypeScript types for navigation params
   - Type-safe navigation with TypeScript
4. Configure navigation options:
   - Header styling and behavior
   - Screen options per navigator
   - Back button handling (Android)
5. Test navigation:
   - Navigate between screens
   - Test deep linking (optional)
   - Verify navigation types work

**Success Criteria:**
- Navigation structure configured
- Stack and tab navigators work
- Type-safe navigation implemented
- Navigation options configured
- Deep linking works (if implemented)

---

### 3. State Management Setup

**Goal:** Configure Zustand and TanStack Query for mobile app state management.

**Steps:**
1. Install state management dependencies:
   - `zustand` for UI state
   - `@tanstack/react-query` for server state
2. Create Zustand stores:
   - `src/lib/stores/uploadStore.ts` - Upload queue management
   - `src/lib/stores/uiStore.ts` - UI state (filters, selection)
   - `src/lib/stores/authStore.ts` - Authentication state
3. Configure TanStack Query:
   - `src/lib/queries/queryClient.ts` - QueryClient configuration
   - Wrap app with QueryClientProvider
   - Configure default options (staleTime, cacheTime)
4. Create query key factories:
   - `src/lib/queries/keys.ts` - Consistent query keys
   - Photo queries, auth queries, tag queries
5. Test state management:
   - Stores update correctly
   - Queries fetch data
   - State persists appropriately

**Success Criteria:**
- Zustand stores created and functional
- TanStack Query configured
- Query keys organized consistently
- State management ready for features
- Providers wrap app correctly

---

### 4. API Client Configuration

**Goal:** Set up API client for backend communication with JWT authentication.

**Steps:**
1. Create `src/lib/api/client.ts`:
   - Configure axios with base URL
   - Request interceptor: add Authorization header
   - Response interceptor: handle 401, token refresh
2. Create API endpoints:
   - `src/lib/api/endpoints.ts` - Endpoint constants and functions
   - Match web API client structure
3. Create API types:
   - `src/lib/api/types.ts` - TypeScript types matching backend DTOs
   - Request/response types
4. Implement secure token storage:
   - Use `expo-secure-store` for token storage
   - Store access and refresh tokens securely
   - Retrieve tokens for API calls
5. Test API client:
   - Health check endpoint works
   - Authentication headers added
   - Token refresh works
   - Error handling correct

**Success Criteria:**
- API client configured with base URL
- Authentication headers added automatically
- Secure token storage implemented
- Token refresh works
- Error handling covers failures

---

### 5. Authentication Screens

**Goal:** Create login and signup screens with JWT authentication flow.

**Steps:**
1. Create `src/screens/auth/LoginScreen.tsx`:
   - Email and password input fields
   - Login button
   - Error message display
   - Loading state during login
2. Create `src/screens/auth/SignupScreen.tsx`:
   - Email, password, confirm password fields
   - Validation (password match, email format)
   - Signup button
   - Error handling
3. Create `src/lib/hooks/useAuth.ts`:
   - `login(email, password)` - Calls API, stores tokens
   - `signup(email, password)` - Creates user, stores tokens
   - `logout()` - Clears tokens, navigates to login
   - `isAuthenticated()` - Checks token validity
4. Implement authentication flow:
   - Login → Store tokens → Navigate to Gallery
   - Signup → Store tokens → Navigate to Gallery
   - Logout → Clear tokens → Navigate to Login
5. Add navigation guards:
   - Redirect authenticated users away from auth screens
   - Redirect unauthenticated users to login
   - Check authentication on app start

**Success Criteria:**
- Login screen functional
- Signup screen functional
- Authentication flow works
- Tokens stored securely
- Navigation guards work

---

### 6. Secure Token Storage

**Goal:** Implement secure storage for JWT tokens using Expo Secure Store.

**Steps:**
1. Install `expo-secure-store`:
   - Secure storage for sensitive data
   - Platform-specific secure storage
2. Create token storage utilities:
   - `src/lib/services/storage/SecureStorage.ts`
   - `setAccessToken(token)`, `getAccessToken()`, `clearTokens()`
   - Store access and refresh tokens securely
3. Integrate with authentication:
   - Store tokens after login/signup
   - Retrieve tokens for API calls
   - Clear tokens on logout
4. Handle token expiration:
   - Check token expiration before API calls
   - Refresh tokens automatically
   - Clear expired tokens
5. Test secure storage:
   - Tokens persist across app restarts
   - Tokens cleared on logout
   - Secure storage works on iOS and Android

**Success Criteria:**
- Secure token storage implemented
- Tokens persist securely
- Token retrieval works
- Token expiration handled
- Works on both iOS and Android

---

### 7. Basic Navigation Structure

**Goal:** Create placeholder screens and navigation structure for main app.

**Steps:**
1. Create placeholder screens:
   - `src/screens/gallery/GalleryScreen.tsx` - Gallery placeholder
   - `src/screens/upload/UploadScreen.tsx` - Upload placeholder
   - `src/screens/photos/PhotoDetailScreen.tsx` - Detail placeholder
2. Set up main navigator:
   - Tab navigator or stack navigator for main screens
   - Navigation between Gallery, Upload, Profile (optional)
3. Create navigation header:
   - Header component with title
   - Logout button
   - User info display
4. Add loading states:
   - Loading screen while checking authentication
   - Skeleton screens for data loading
5. Test navigation:
   - Navigate between main screens
   - Header displays correctly
   - Logout navigates to login

**Success Criteria:**
- Navigation structure created
- Placeholder screens display
- Header works correctly
- Navigation flows work
- Ready for feature implementation

## Success Criteria (Phase Completion)

- ✅ Expo project initialized and running
- ✅ React Navigation configured
- ✅ State management (Zustand + TanStack Query) set up
- ✅ API client configured with JWT authentication
- ✅ Login and signup screens functional
- ✅ Secure token storage implemented
- ✅ Navigation structure created
- ✅ Foundation ready for mobile features

## Notes and Considerations

- **Expo SDK:** Pin Expo SDK version for stability. Upgrade carefully to avoid breaking changes.
- **Navigation:** Use stack navigator for main flow, tab navigator for bottom navigation (optional). Type-safe navigation with TypeScript.
- **Secure Storage:** Use `expo-secure-store` for tokens. More secure than AsyncStorage. Works on iOS and Android.
- **API Client:** Match web API client structure for consistency. Share types if possible (monorepo).
- **Platform Differences:** Handle iOS and Android differences (back button, safe areas, permissions).
- **Development:** Use Expo Go for quick development, development builds for native modules.
- **Next Steps:** Phase 11 will implement core mobile features (upload, gallery, etc.).

