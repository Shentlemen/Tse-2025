#!/bin/bash

# Test script for API Key Authentication
# Usage: ./test-api-auth.sh

BASE_URL="http://localhost:8080/prestador/api/documents"
API_KEY="change-me-in-production-secure-random-key-12345"

echo "=========================================="
echo "API Key Authentication Test Suite"
echo "=========================================="
echo ""

# Test 1: Request without API key (should fail with 401)
echo "Test 1: Request without API key"
echo "Expected: 401 Unauthorized"
echo "Command: curl -s -o /dev/null -w '%{http_code}' $BASE_URL"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL")
echo "Result: HTTP $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo "✓ PASSED"
else
    echo "✗ FAILED"
fi
echo ""

# Test 2: Request with invalid API key (should fail with 401)
echo "Test 2: Request with invalid API key"
echo "Expected: 401 Unauthorized"
echo "Command: curl -s -o /dev/null -w '%{http_code}' -H 'X-API-Key: invalid-key' $BASE_URL"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "X-API-Key: invalid-key" "$BASE_URL")
echo "Result: HTTP $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo "✓ PASSED"
else
    echo "✗ FAILED"
fi
echo ""

# Test 3: Request with valid API key (should succeed with 200)
echo "Test 3: Request with valid API key"
echo "Expected: 200 OK"
echo "Command: curl -s -o /dev/null -w '%{http_code}' -H 'X-API-Key: $API_KEY' $BASE_URL"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "X-API-Key: $API_KEY" "$BASE_URL")
echo "Result: HTTP $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo "✓ PASSED"
else
    echo "✗ FAILED (Note: Application may not be running)"
fi
echo ""

# Test 4: Full response with invalid key
echo "Test 4: Full error response with invalid API key"
echo "Expected: JSON error response"
echo "Command: curl -s -H 'X-API-Key: invalid-key' $BASE_URL"
RESPONSE=$(curl -s -H "X-API-Key: invalid-key" "$BASE_URL")
echo "Response:"
echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
echo ""

# Test 5: Full response with valid key
echo "Test 5: Full response with valid API key"
echo "Expected: Document list or empty array"
echo "Command: curl -s -H 'X-API-Key: $API_KEY' $BASE_URL"
RESPONSE=$(curl -s -H "X-API-Key: $API_KEY" "$BASE_URL")
echo "Response:"
echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
echo ""

# Test 6: POST request without API key
echo "Test 6: POST request without API key"
echo "Expected: 401 Unauthorized"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","documentType":"CLINICAL_NOTE","patientId":"12345678","clinicId":"clinic-001","professionalId":"doc-001","dateOfVisit":"2025-11-18"}' \
  "$BASE_URL")
echo "Result: HTTP $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo "✓ PASSED"
else
    echo "✗ FAILED"
fi
echo ""

# Test 7: GET specific document without API key
echo "Test 7: GET specific document without API key"
echo "Expected: 401 Unauthorized"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/1")
echo "Result: HTTP $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo "✓ PASSED"
else
    echo "✗ FAILED"
fi
echo ""

echo "=========================================="
echo "Test suite completed"
echo "=========================================="
