@echo off
REM Test script for API Key Authentication (Windows)
REM Usage: test-api-auth.bat

SET BASE_URL=http://localhost:8080/prestador/api/documents
SET API_KEY=change-me-in-production-secure-random-key-12345

echo ==========================================
echo API Key Authentication Test Suite
echo ==========================================
echo.

REM Test 1: Request without API key (should fail with 401)
echo Test 1: Request without API key
echo Expected: 401 Unauthorized
curl -s -o nul -w "Result: HTTP %%{http_code}" %BASE_URL%
echo.
echo.

REM Test 2: Request with invalid API key (should fail with 401)
echo Test 2: Request with invalid API key
echo Expected: 401 Unauthorized
curl -s -o nul -w "Result: HTTP %%{http_code}" -H "X-API-Key: invalid-key" %BASE_URL%
echo.
echo.

REM Test 3: Request with valid API key (should succeed with 200)
echo Test 3: Request with valid API key
echo Expected: 200 OK
curl -s -o nul -w "Result: HTTP %%{http_code}" -H "X-API-Key: %API_KEY%" %BASE_URL%
echo.
echo.

REM Test 4: Full response with invalid key
echo Test 4: Full error response with invalid API key
echo Expected: JSON error response
curl -s -H "X-API-Key: invalid-key" %BASE_URL%
echo.
echo.

REM Test 5: Full response with valid key
echo Test 5: Full response with valid API key
echo Expected: Document list or empty array
curl -s -H "X-API-Key: %API_KEY%" %BASE_URL%
echo.
echo.

REM Test 6: POST request without API key
echo Test 6: POST request without API key
echo Expected: 401 Unauthorized
curl -s -o nul -w "Result: HTTP %%{http_code}" -X POST -H "Content-Type: application/json" -d "{\"title\":\"Test\",\"documentType\":\"CLINICAL_NOTE\",\"patientId\":\"12345678\",\"clinicId\":\"clinic-001\",\"professionalId\":\"doc-001\",\"dateOfVisit\":\"2025-11-18\"}" %BASE_URL%
echo.
echo.

REM Test 7: GET specific document without API key
echo Test 7: GET specific document without API key
echo Expected: 401 Unauthorized
curl -s -o nul -w "Result: HTTP %%{http_code}" %BASE_URL%/1
echo.
echo.

echo ==========================================
echo Test suite completed
echo ==========================================
pause
