# Quick Start Guide - Phase 5 Testing

## Prerequisites Check

1. **PostgreSQL** is running
2. **Database** `rapid_photo_upload` exists
3. **Ports** 8080 (backend) and 3000 (frontend) are available

## Start Everything

### Terminal 1: Backend
```bash
cd backend
mvn spring-boot:run
```

Wait for: `Started RapidPhotoUploadApplication`

### Terminal 2: Frontend
```bash
cd web
npm run dev
```

Wait for: `Ready on http://localhost:3000`

## Quick Test (5 minutes)

1. **Open browser**: `http://localhost:3000`
2. **Should redirect to**: `/login`
3. **Click "Sign up"** link
4. **Create account**:
   - Email: `test@example.com`
   - Password: `password123`
   - Confirm: `password123`
5. **Click "Sign Up"**
6. **Should redirect to**: `/gallery`
7. **Verify**:
   - ✅ Header shows "RapidPhotoUpload"
   - ✅ Navigation shows "Gallery" and "Upload"
   - ✅ Logout button visible
   - ✅ Gallery placeholder page displays

## Test Navigation

1. Click **"Upload"** in navigation → Should show upload placeholder
2. Click **"Gallery"** in navigation → Should show gallery placeholder
3. Click **"Logout"** → Should redirect to login
4. Try accessing `/gallery` directly → Should redirect to login

## Verify API Integration

1. Open **Browser DevTools** (F12)
2. Go to **Network** tab
3. Login again
4. Check request to `/api/auth/login`:
   - ✅ Status: 200
   - ✅ Response contains `token`, `userId`, `email`
5. Check **Application** → **Local Storage**:
   - ✅ `auth_token` key exists with token value

## Common Issues

### Backend won't start
- Check PostgreSQL is running: `psql -U postgres -l`
- Verify database exists
- Check port 8080 is free

### Frontend won't start
- Run `npm install` in `web/` directory
- Check Node.js version: `node --version` (should be 18+)

### Can't login
- Verify backend is running: `http://localhost:8080/actuator/health`
- Check browser console for errors
- Verify `.env.local` has `NEXT_PUBLIC_API_URL=http://localhost:8080`

## Success Criteria

✅ Can signup new user  
✅ Can login with credentials  
✅ Can navigate between pages  
✅ Can logout  
✅ Protected routes redirect when not authenticated  
✅ Token stored in localStorage  
✅ API requests include Authorization header  

If all checks pass, Phase 5 is working correctly! 🎉

