# PowerShell script to test all RapidPhotoUpload API endpoints
# Usage: .\test-all-endpoints.ps1 [baseUrl] [userId]
# Example: .\test-all-endpoints.ps1 http://localhost:8080 550e8400-e29b-41d4-a716-446655440000

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$UserId = "550e8400-e29b-41d4-a716-446655440000"
)

# Color output functions
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }

# Test counter
$script:TestsPassed = 0
$script:TestsFailed = 0

function Test-Endpoint-WebRequest {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [int]$ExpectedStatus = 200,
        [string]$Description = ""
    )
    
    Write-Info "`n========================================"
    Write-Info "Testing: $Name"
    if ($Description) {
        Write-Info "Description: $Description"
    }
    Write-Info "Method: $Method"
    Write-Info "URL: $Url"
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
            Write-Info "Body: $($params.Body)"
        }
        
        $response = Invoke-WebRequest @params
        $actualStatus = $response.StatusCode
        
        if ($actualStatus -eq $ExpectedStatus) {
            Write-Success "[PASS] Status: $actualStatus"
            try {
                $jsonResponse = $response.Content | ConvertFrom-Json
                Write-Info "Response: $($jsonResponse | ConvertTo-Json -Depth 10)"
                $script:TestsPassed++
                return $jsonResponse
            } catch {
                # Try to display as string if not JSON
                $contentStr = $response.Content
                if ($contentStr -is [array]) {
                    $contentStr = [System.Text.Encoding]::UTF8.GetString($contentStr)
                }
                Write-Info "Response: $contentStr"
                $script:TestsPassed++
                return $null
            }
        } else {
            Write-Error "[FAIL] Expected status $ExpectedStatus, got $actualStatus"
            $script:TestsFailed++
            return $null
        }
    }
    catch {
        $statusCode = $null
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
        }
        
        if ($statusCode -and $statusCode -eq $ExpectedStatus) {
            Write-Success "[PASS] Status: $statusCode (as expected for error test)"
            $script:TestsPassed++
            return $null
        } elseif ($statusCode) {
            Write-Error "[FAIL] Expected status $ExpectedStatus, got $statusCode"
            Write-Error "Error: $($_.Exception.Message)"
            $script:TestsFailed++
            return $null
        } else {
            Write-Error "[FAIL] Expected status $ExpectedStatus, but got exception"
            Write-Error "Error: $($_.Exception.Message)"
            $script:TestsFailed++
            return $null
        }
    }
}

function Upload-FileToS3 {
    param(
        [string]$PresignedUrl,
        [string]$FilePath,
        [string]$ContentType
    )
    
    Write-Info "Uploading file to S3: $FilePath"
    
    try {
        if (-not (Test-Path $FilePath)) {
            Write-Error "File not found: $FilePath"
            return $false
        }
        
        $fileContent = [System.IO.File]::ReadAllBytes($FilePath)
        
        $headers = @{
            "Content-Type" = $ContentType
        }
        
        $response = Invoke-WebRequest `
            -Uri $PresignedUrl `
            -Method "PUT" `
            -Headers $headers `
            -Body $fileContent `
            -ErrorAction "Stop"
        
        if ($response.StatusCode -eq 200) {
            Write-Success "[PASS] File uploaded to S3 successfully"
            return $true
        } else {
            Write-Error "[FAIL] Upload returned status: $($response.StatusCode)"
            return $false
        }
    }
    catch {
        Write-Error "[FAIL] Failed to upload file to S3: $($_.Exception.Message)"
        return $false
    }
}

Write-Info "=========================================="
Write-Info "RapidPhotoUpload API Endpoint Tests"
Write-Info "=========================================="
Write-Info "Base URL: $BaseUrl"
Write-Info "User ID: $UserId"
Write-Info "=========================================="

# Test 1: Home endpoint
$null = Test-Endpoint-WebRequest `
    -Name "Home Endpoint" `
    -Method "GET" `
    -Url "$BaseUrl/" `
    -ExpectedStatus 200 `
    -Description "Get API information"

# Test 2: Health check
$null = Test-Endpoint-WebRequest `
    -Name "Health Check" `
    -Method "GET" `
    -Url "$BaseUrl/actuator/health" `
    -ExpectedStatus 200 `
    -Description "Check application health"

# Test 3: Initiate upload (success)
$uploadBody = @{
    filename = "test-photo.jpg"
    contentType = "image/jpeg"
    fileSize = 2048576
    tags = @("test", "integration")
}

$uploadHeaders = @{
    "x-user-id" = $UserId
}

$uploadResponse = Test-Endpoint-WebRequest `
    -Name "Initiate Upload (Success)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads" `
    -Headers $uploadHeaders `
    -Body $uploadBody `
    -ExpectedStatus 201 `
    -Description "Create a new upload and get presigned URL"

$photoId = $null
if ($uploadResponse -and $uploadResponse.photoId) {
    $photoId = $uploadResponse.photoId
    Write-Info "Created photo with ID: $photoId"
    
    # Actually upload the file to S3 using the presigned URL
    if ($uploadResponse.presignedUrl) {
        $testPhotoPath = Join-Path $PSScriptRoot "test-photo.jpg"
        if (Test-Path $testPhotoPath) {
            Write-Info "Uploading test photo to S3..."
            $uploadSuccess = Upload-FileToS3 `
                -PresignedUrl $uploadResponse.presignedUrl `
                -FilePath $testPhotoPath `
                -ContentType "image/jpeg"
            
            if ($uploadSuccess) {
                Write-Success "[PASS] File successfully uploaded to S3"
            } else {
                Write-Warning "File upload to S3 failed, but continuing with tests"
            }
        } else {
            Write-Warning "Test photo file not found at: $testPhotoPath"
            Write-Warning "Skipping actual S3 upload. File will not appear in S3 bucket."
        }
    }
}

# Test 4: Initiate upload (missing user ID - should fail)
$uploadBodyNoUser = @{
    filename = "test-photo.jpg"
    contentType = "image/jpeg"
    fileSize = 2048576
}

Test-Endpoint-WebRequest `
    -Name "Initiate Upload (Missing User ID)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads" `
    -Body $uploadBodyNoUser `
    -ExpectedStatus 401 `
    -Description "Should fail without user ID header"

# Test 5: Initiate upload (invalid file size - should fail)
$uploadBodyInvalidSize = @{
    filename = "huge-photo.jpg"
    contentType = "image/jpeg"
    fileSize = 100000000
}

Test-Endpoint-WebRequest `
    -Name "Initiate Upload (Invalid File Size)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads" `
    -Headers $uploadHeaders `
    -Body $uploadBodyInvalidSize `
    -ExpectedStatus 400 `
    -Description "Should fail with file size > 50MB"

# Test 6: Initiate upload (invalid content type - should fail)
$uploadBodyInvalidType = @{
    filename = "document.pdf"
    contentType = "application/pdf"
    fileSize = 1024
}

Test-Endpoint-WebRequest `
    -Name "Initiate Upload (Invalid Content Type)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads" `
    -Headers $uploadHeaders `
    -Body $uploadBodyInvalidType `
    -ExpectedStatus 400 `
    -Description "Should fail with non-image content type"

# Test 7: Get upload status (if we have a photo ID)
if ($photoId) {
    Test-Endpoint-WebRequest `
        -Name "Get Upload Status" `
        -Method "GET" `
        -Url "$BaseUrl/api/uploads/$photoId/status" `
        -ExpectedStatus 200 `
        -Description "Get status of uploaded photo"
} else {
    Write-Warning "Skipping status check - no photo ID available"
}

# Test 8: Get upload status (non-existent photo - should fail)
$fakePhotoId = "00000000-0000-0000-0000-000000000000"
Test-Endpoint-WebRequest `
    -Name "Get Upload Status (Non-existent)" `
    -Method "GET" `
    -Url "$BaseUrl/api/uploads/$fakePhotoId/status" `
    -ExpectedStatus 404 `
    -Description "Should fail for non-existent photo"

# Test 9: Report upload completion (if we have a photo ID)
if ($photoId) {
    Test-Endpoint-WebRequest `
        -Name "Report Upload Completion" `
        -Method "POST" `
        -Url "$BaseUrl/api/uploads/$photoId/complete" `
        -ExpectedStatus 200 `
        -Description "Mark upload as completed"
    
    # Verify status changed to COMPLETED
    Start-Sleep -Seconds 1
    $statusAfterComplete = Test-Endpoint-WebRequest `
        -Name "Verify Status After Completion" `
        -Method "GET" `
        -Url "$BaseUrl/api/uploads/$photoId/status" `
        -ExpectedStatus 200 `
        -Description "Verify status is now COMPLETED"
    
    if ($statusAfterComplete -and $statusAfterComplete.status -eq "COMPLETED") {
        Write-Success "[PASS] Status correctly updated to COMPLETED"
    }
    $null = $statusAfterComplete
} else {
    Write-Warning "Skipping completion test - no photo ID available"
}

# Test 10: Report upload failure (create new upload first)
$uploadBody2 = @{
    filename = "test-photo-2.jpg"
    contentType = "image/jpeg"
    fileSize = 1024
    tags = @("test")
}

$uploadResponse2 = Test-Endpoint-WebRequest `
    -Name "Initiate Upload (For Failure Test)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads" `
    -Headers $uploadHeaders `
    -Body $uploadBody2 `
    -ExpectedStatus 201 `
    -Description "Create upload for failure test"

$photoId2 = $null
if ($uploadResponse2 -and $uploadResponse2.photoId) {
    $photoId2 = $uploadResponse2.photoId
    
    $failureBody = @{
        errorMessage = "Upload failed due to network timeout"
    }
    
    Test-Endpoint-WebRequest `
        -Name "Report Upload Failure" `
        -Method "POST" `
        -Url "$BaseUrl/api/uploads/$photoId2/fail" `
        -Headers $uploadHeaders `
        -Body $failureBody `
        -ExpectedStatus 200 `
        -Description "Mark upload as failed"
    
    # Verify status changed to FAILED
    Start-Sleep -Seconds 1
    $statusAfterFail = Test-Endpoint-WebRequest `
        -Name "Verify Status After Failure" `
        -Method "GET" `
        -Url "$BaseUrl/api/uploads/$photoId2/status" `
        -ExpectedStatus 200 `
        -Description "Verify status is now FAILED"
    
    if ($statusAfterFail -and $statusAfterFail.status -eq "FAILED") {
        Write-Success "[PASS] Status correctly updated to FAILED"
    }
    $null = $statusAfterFail
} else {
    Write-Warning "Skipping failure test - could not create upload"
}

# Test 11: Report completion (non-existent photo - should fail)
Test-Endpoint-WebRequest `
    -Name "Report Completion (Non-existent)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads/$fakePhotoId/complete" `
    -ExpectedStatus 404 `
    -Description "Should fail for non-existent photo"

# Test 12: Report failure (non-existent photo - should fail)
$failureBody = @{
    errorMessage = "Test error"
}

Test-Endpoint-WebRequest `
    -Name "Report Failure (Non-existent)" `
    -Method "POST" `
    -Url "$BaseUrl/api/uploads/$fakePhotoId/fail" `
    -Headers $uploadHeaders `
    -Body $failureBody `
    -ExpectedStatus 404 `
    -Description "Should fail for non-existent photo"

# Test 13: Invalid photo ID format
Test-Endpoint-WebRequest `
    -Name "Get Status (Invalid ID Format)" `
    -Method "GET" `
    -Url "$BaseUrl/api/uploads/invalid-id/status" `
    -ExpectedStatus 400 `
    -Description "Should fail with invalid UUID format"

# ========================================
# Phase 4: Authentication Tests
# ========================================
Write-Info "`n========================================"
Write-Info "Phase 4: Authentication Tests"
Write-Info "========================================"

# Test 14: User Signup
# Use a unique email based on timestamp to avoid conflicts from previous test runs
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$testEmail = "testuser$timestamp@example.com"
$signupBody = @{
    email = $testEmail
    password = "testpassword123"
}

$signupResponse = Test-Endpoint-WebRequest `
    -Name "User Signup" `
    -Method "POST" `
    -Url "$BaseUrl/api/auth/signup" `
    -Body $signupBody `
    -ExpectedStatus 200 `
    -Description "Create a new user account"

$authToken = $null
$authUserId = $null
if ($signupResponse -and $signupResponse.token) {
    $authToken = $signupResponse.token
    $authUserId = $signupResponse.userId
    Write-Info "Created user with ID: $authUserId"
    Write-Info "Received auth token: $authToken"
} else {
    Write-Warning "Signup failed - user may already exist. Trying login instead..."
    # If signup failed, try to login with the same credentials
    # (in case user was created in a previous test run)
    $loginBody = @{
        email = $testEmail
        password = "testpassword123"
    }
    $loginResponse = Test-Endpoint-WebRequest `
        -Name "User Login (Fallback)" `
        -Method "POST" `
        -Url "$BaseUrl/api/auth/login" `
        -Body $loginBody `
        -ExpectedStatus 200 `
        -Description "Login with existing user"
    
    if ($loginResponse -and $loginResponse.token) {
        $authToken = $loginResponse.token
        $authUserId = $loginResponse.userId
        Write-Info "Logged in with existing user ID: $authUserId"
    }
}

# Test 15: User Signup (Duplicate Email - should fail)
# Try to signup with the same email we just used (should fail)
if ($testEmail) {
    Test-Endpoint-WebRequest `
        -Name "User Signup (Duplicate Email)" `
        -Method "POST" `
        -Url "$BaseUrl/api/auth/signup" `
        -Body $signupBody `
        -ExpectedStatus 400 `
        -Description "Should fail when email already exists"
}

# Test 16: User Login
# Use the email from signup, or fallback to a test email
if (-not $testEmail) {
    $testEmail = "testuser@example.com"
}
$loginBody = @{
    email = $testEmail
    password = "testpassword123"
}

$loginResponse = Test-Endpoint-WebRequest `
    -Name "User Login" `
    -Method "POST" `
    -Url "$BaseUrl/api/auth/login" `
    -Body $loginBody `
    -ExpectedStatus 200 `
    -Description "Login with valid credentials"

if ($loginResponse -and $loginResponse.token) {
    $authToken = $loginResponse.token
    $authUserId = $loginResponse.userId
    Write-Info "Logged in with token: $authToken"
}

# Test 17: User Login (Invalid Credentials - should fail)
$invalidLoginBody = @{
    email = $testEmail
    password = "wrongpassword"
}

Test-Endpoint-WebRequest `
    -Name "User Login (Invalid Password)" `
    -Method "POST" `
    -Url "$BaseUrl/api/auth/login" `
    -Body $invalidLoginBody `
    -ExpectedStatus 401 `
    -Description "Should fail with invalid password"

# Test 18: Validate Token
if ($authToken) {
    $validateBody = @{
        token = $authToken
    }
    
    $validateResponse = Test-Endpoint-WebRequest `
        -Name "Validate Token" `
        -Method "POST" `
        -Url "$BaseUrl/api/auth/validate" `
        -Body $validateBody `
        -ExpectedStatus 200 `
        -Description "Validate authentication token"
    
    if ($validateResponse -and $validateResponse.valid) {
        Write-Success "[PASS] Token is valid"
    }
} else {
    Write-Warning "Skipping token validation - no token available"
}

# Test 19: Validate Token (Invalid - should fail)
$invalidTokenBody = @{
    token = "invalid-token-12345"
}

Test-Endpoint-WebRequest `
    -Name "Validate Token (Invalid)" `
    -Method "POST" `
    -Url "$BaseUrl/api/auth/validate" `
    -Body $invalidTokenBody `
    -ExpectedStatus 401 `
    -Description "Should fail with invalid token"

# Test 20: Logout
if ($authToken) {
    $logoutBody = @{
        token = $authToken
    }
    
    Test-Endpoint-WebRequest `
        -Name "Logout" `
        -Method "POST" `
        -Url "$BaseUrl/api/auth/logout" `
        -Body $logoutBody `
        -ExpectedStatus 200 `
        -Description "Invalidate authentication token"
    
    # Verify token is invalidated
    Start-Sleep -Seconds 1
    Test-Endpoint-WebRequest `
        -Name "Verify Token Invalidated" `
        -Method "POST" `
        -Url "$BaseUrl/api/auth/validate" `
        -Body $logoutBody `
        -ExpectedStatus 401 `
        -Description "Token should be invalid after logout"
} else {
    Write-Warning "Skipping logout test - no token available"
}

# ========================================
# Phase 4: Photo Query Tests
# ========================================
Write-Info "`n========================================"
Write-Info "Phase 4: Photo Query Tests"
Write-Info "========================================"

# Use the userId from earlier tests or create a new one
# Prefer the hardcoded userId since that's where Phase 3 uploads went
$queryUserId = $UserId
$authPhotoIds = @()

if ($authUserId) {
    # Upload multiple photos for the authenticated user with different tags
    # This ensures we have proper test data for query and tag filtering tests
    Write-Info "Uploading test photos for authenticated user..."
    
    $uploadHeadersForAuth = @{
        "x-user-id" = $authUserId
    }
    
    # Photo 1: Beach vacation photo
    $beachPhotoBody = @{
        filename = "beach-vacation.jpg"
        contentType = "image/jpeg"
        fileSize = 2048
        tags = @("vacation", "beach", "summer")
    }
    
    $beachPhotoResponse = Test-Endpoint-WebRequest `
        -Name "Upload Beach Photo" `
        -Method "POST" `
        -Url "$BaseUrl/api/uploads" `
        -Headers $uploadHeadersForAuth `
        -Body $beachPhotoBody `
        -ExpectedStatus 201 `
        -Description "Create beach vacation photo"
    
    if ($beachPhotoResponse -and $beachPhotoResponse.photoId) {
        $beachPhotoId = $beachPhotoResponse.photoId
        $authPhotoIds += $beachPhotoId
        
        # Upload file to S3
        if ($beachPhotoResponse.presignedUrl) {
            $testPhotoPath = Join-Path $PSScriptRoot "test-photo.jpg"
            if (Test-Path $testPhotoPath) {
                Upload-FileToS3 `
                    -PresignedUrl $beachPhotoResponse.presignedUrl `
                    -FilePath $testPhotoPath `
                    -ContentType "image/jpeg" | Out-Null
            }
        }
        
        Start-Sleep -Milliseconds 300
        Test-Endpoint-WebRequest `
            -Name "Complete Beach Photo" `
            -Method "POST" `
            -Url "$BaseUrl/api/uploads/$beachPhotoId/complete" `
            -ExpectedStatus 200 `
            -Description "Mark beach photo as completed" | Out-Null
    }
    
    # Photo 2: Mountain photo (shares "vacation" tag)
    $mountainPhotoBody = @{
        filename = "mountain-hike.jpg"
        contentType = "image/jpeg"
        fileSize = 1536
        tags = @("vacation", "mountain", "hiking")
    }
    
    $mountainPhotoResponse = Test-Endpoint-WebRequest `
        -Name "Upload Mountain Photo" `
        -Method "POST" `
        -Url "$BaseUrl/api/uploads" `
        -Headers $uploadHeadersForAuth `
        -Body $mountainPhotoBody `
        -ExpectedStatus 201 `
        -Description "Create mountain photo"
    
    if ($mountainPhotoResponse -and $mountainPhotoResponse.photoId) {
        $mountainPhotoId = $mountainPhotoResponse.photoId
        $authPhotoIds += $mountainPhotoId
        
        # Upload file to S3
        if ($mountainPhotoResponse.presignedUrl) {
            $testPhotoPath = Join-Path $PSScriptRoot "test-photo.jpg"
            if (Test-Path $testPhotoPath) {
                Upload-FileToS3 `
                    -PresignedUrl $mountainPhotoResponse.presignedUrl `
                    -FilePath $testPhotoPath `
                    -ContentType "image/jpeg" | Out-Null
            }
        }
        
        Start-Sleep -Milliseconds 300
        Test-Endpoint-WebRequest `
            -Name "Complete Mountain Photo" `
            -Method "POST" `
            -Url "$BaseUrl/api/uploads/$mountainPhotoId/complete" `
            -ExpectedStatus 200 `
            -Description "Mark mountain photo as completed" | Out-Null
    }
    
    # Photo 3: City photo (different tags)
    $cityPhotoBody = @{
        filename = "city-trip.jpg"
        contentType = "image/jpeg"
        fileSize = 1024
        tags = @("city", "urban", "travel")
    }
    
    $cityPhotoResponse = Test-Endpoint-WebRequest `
        -Name "Upload City Photo" `
        -Method "POST" `
        -Url "$BaseUrl/api/uploads" `
        -Headers $uploadHeadersForAuth `
        -Body $cityPhotoBody `
        -ExpectedStatus 201 `
        -Description "Create city photo"
    
    if ($cityPhotoResponse -and $cityPhotoResponse.photoId) {
        $cityPhotoId = $cityPhotoResponse.photoId
        $authPhotoIds += $cityPhotoId
        
        # Upload file to S3
        if ($cityPhotoResponse.presignedUrl) {
            $testPhotoPath = Join-Path $PSScriptRoot "test-photo.jpg"
            if (Test-Path $testPhotoPath) {
                Upload-FileToS3 `
                    -PresignedUrl $cityPhotoResponse.presignedUrl `
                    -FilePath $testPhotoPath `
                    -ContentType "image/jpeg" | Out-Null
            }
        }
        
        Start-Sleep -Milliseconds 300
        Test-Endpoint-WebRequest `
            -Name "Complete City Photo" `
            -Method "POST" `
            -Url "$BaseUrl/api/uploads/$cityPhotoId/complete" `
            -ExpectedStatus 200 `
            -Description "Mark city photo as completed" | Out-Null
    }
    
    if ($authPhotoIds.Count -gt 0) {
        $queryUserId = $authUserId
        Write-Info "Successfully uploaded $($authPhotoIds.Count) photos for authenticated user"
    } else {
        # Fall back to hardcoded userId if uploads failed
        $queryUserId = $UserId
        Write-Warning "Could not upload photos for auth user, using hardcoded userId"
    }
}

# Test 21: List Photos (with pagination)
$listPhotosResponse = Test-Endpoint-WebRequest `
    -Name "List Photos" `
    -Method "GET" `
    -Url "$BaseUrl/api/photos?userId=$queryUserId&page=0&size=10" `
    -ExpectedStatus 200 `
    -Description "List photos with pagination"

$testPhotoId = $null
if ($listPhotosResponse -and $listPhotosResponse.Count -gt 0) {
    $testPhotoId = $listPhotosResponse[0].id
    Write-Info "Found $($listPhotosResponse.Count) photo(s). Using first photo ID: $testPhotoId"
} elseif ($authPhotoIds -and $authPhotoIds.Count -gt 0) {
    # Use the first uploaded auth photo
    $testPhotoId = $authPhotoIds[0]
    Write-Info "List returned empty, but using uploaded auth photo ID: $testPhotoId"
} elseif ($photoId) {
    # Fall back to Phase 3 photo
    $testPhotoId = $photoId
    Write-Info "Using Phase 3 photo ID: $testPhotoId"
} else {
    Write-Warning "No photos available for testing. Some tests will be skipped."
}

# Test 22: List Photos (with tag filter)
if ($testPhotoId) {
    # Test filtering by "vacation" tag (should return beach and mountain photos if using auth user)
    Test-Endpoint-WebRequest `
        -Name "List Photos (Tag Filter - vacation)" `
        -Method "GET" `
        -Url "$BaseUrl/api/photos?userId=$queryUserId&tags=vacation&page=0&size=10" `
        -ExpectedStatus 200 `
        -Description "List photos filtered by 'vacation' tag"
    
    # Test filtering by multiple tags (AND logic - should return only beach photo)
    if ($queryUserId -eq $authUserId) {
        Test-Endpoint-WebRequest `
            -Name "List Photos (Tag Filter - vacation AND beach)" `
            -Method "GET" `
            -Url "$BaseUrl/api/photos?userId=$queryUserId&tags=vacation,beach&page=0&size=10" `
            -ExpectedStatus 200 `
            -Description "List photos filtered by 'vacation' AND 'beach' tags (AND logic)"
    }
} else {
    Write-Warning "Skipping tag filter test - no photos available"
}

# Test 23: Get Photo by ID
if ($testPhotoId) {
    $photoDetail = Test-Endpoint-WebRequest `
        -Name "Get Photo by ID" `
        -Method "GET" `
        -Url "$BaseUrl/api/photos/$testPhotoId" `
        -ExpectedStatus 200 `
        -Description "Get single photo details"
    
    if ($photoDetail -and $photoDetail.downloadUrl) {
        Write-Success "[PASS] Photo has download URL"
    }
} else {
    Write-Warning "Skipping get photo test - no photo ID available"
}

# Test 24: Get Photo by ID (Non-existent - should fail)
$fakePhotoId = "00000000-0000-0000-0000-000000000000"
Test-Endpoint-WebRequest `
    -Name "Get Photo (Non-existent)" `
    -Method "GET" `
    -Url "$BaseUrl/api/photos/$fakePhotoId" `
    -ExpectedStatus 404 `
    -Description "Should fail for non-existent photo"

# Test 25: Get Download URL
if ($testPhotoId) {
    $downloadResponse = Test-Endpoint-WebRequest `
        -Name "Get Download URL" `
        -Method "GET" `
        -Url "$BaseUrl/api/photos/$testPhotoId/download" `
        -ExpectedStatus 200 `
        -Description "Get presigned download URL"
    
    if ($downloadResponse -and $downloadResponse.downloadUrl) {
        Write-Success "[PASS] Download URL generated"
        Write-Info "Download URL expires in: $($downloadResponse.expirationMinutes) minutes"
    }
} else {
    Write-Warning "Skipping download URL test - no photo ID available"
}

# ========================================
# Phase 4: Tag Management Tests
# ========================================
Write-Info "`n========================================"
Write-Info "Phase 4: Tag Management Tests"
Write-Info "========================================"

# Test 26: Add Tags to Photo
if ($testPhotoId) {
    $addTagsBody = @{
        tags = @("new-tag-1", "new-tag-2", "vacation")
    }
    
    $addTagsResponse = Test-Endpoint-WebRequest `
        -Name "Add Tags to Photo" `
        -Method "POST" `
        -Url "$BaseUrl/api/photos/$testPhotoId/tags" `
        -Body $addTagsBody `
        -ExpectedStatus 200 `
        -Description "Add tags to a photo"
    
    if ($addTagsResponse -and $addTagsResponse.tags) {
        $hasNewTags = ($addTagsResponse.tags -contains "new-tag-1") -and ($addTagsResponse.tags -contains "new-tag-2")
        if ($hasNewTags) {
            Write-Success "[PASS] Tags successfully added"
        }
    }
} else {
    Write-Warning "Skipping add tags test - no photo ID available"
}

# Test 27: Remove Tags from Photo
if ($testPhotoId) {
    $removeTagsBody = @{
        tags = @("new-tag-1")
    }
    
    $removeTagsResponse = Test-Endpoint-WebRequest `
        -Name "Remove Tags from Photo" `
        -Method "DELETE" `
        -Url "$BaseUrl/api/photos/$testPhotoId/tags" `
        -Body $removeTagsBody `
        -ExpectedStatus 200 `
        -Description "Remove tags from a photo"
    
    if ($removeTagsResponse -and $removeTagsResponse.tags) {
        $tagRemoved = -not ($removeTagsResponse.tags -contains "new-tag-1")
        if ($tagRemoved) {
            Write-Success "[PASS] Tag successfully removed"
        }
    }
} else {
    Write-Warning "Skipping remove tags test - no photo ID available"
}

# Test 28: Replace Tags on Photo
if ($testPhotoId) {
    $replaceTagsBody = @{
        tags = @("replaced-tag-1", "replaced-tag-2")
    }
    
    $replaceTagsResponse = Test-Endpoint-WebRequest `
        -Name "Replace Tags on Photo" `
        -Method "PUT" `
        -Url "$BaseUrl/api/photos/$testPhotoId/tags" `
        -Body $replaceTagsBody `
        -ExpectedStatus 200 `
        -Description "Replace all tags on a photo"
    
    if ($replaceTagsResponse -and $replaceTagsResponse.tags) {
        $hasReplacedTags = ($replaceTagsResponse.tags -contains "replaced-tag-1") -and ($replaceTagsResponse.tags -contains "replaced-tag-2")
        $onlyReplacedTags = $replaceTagsResponse.tags.Count -eq 2
        if ($hasReplacedTags -and $onlyReplacedTags) {
            Write-Success "[PASS] Tags successfully replaced"
        }
    }
} else {
    Write-Warning "Skipping replace tags test - no photo ID available"
}

# Test 29: Add Tags (Non-existent Photo - should fail)
$addTagsBody = @{
    tags = @("tag1")
}

Test-Endpoint-WebRequest `
    -Name "Add Tags (Non-existent Photo)" `
    -Method "POST" `
    -Url "$BaseUrl/api/photos/$fakePhotoId/tags" `
    -Body $addTagsBody `
    -ExpectedStatus 404 `
    -Description "Should fail for non-existent photo"

# Summary
Write-Info "`n========================================"
Write-Info "Test Summary"
Write-Info "========================================"
Write-Success "Tests Passed: $script:TestsPassed"
Write-Error "Tests Failed: $script:TestsFailed"
Write-Info "Total Tests: $($script:TestsPassed + $script:TestsFailed)"
Write-Info "========================================"

if ($script:TestsFailed -eq 0) {
    Write-Success "`n[PASS] All tests passed!"
    exit 0
} else {
    Write-Error "`n[FAIL] Some tests failed. Please review the output above."
    exit 1
}
