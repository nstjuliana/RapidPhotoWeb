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
