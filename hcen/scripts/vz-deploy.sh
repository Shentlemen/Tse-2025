#!/usr/bin/env bash
set -euo pipefail

# Virtuozzo (Jelastic) redeploy script
# Usage: ./scripts/vz-deploy.sh <tag>
# Requires env vars:
#   VZ_API_HOST   (e.g. paas.minube.antel.com)
#   VZ_ENV_NAME   (environment name)
#   VZ_NODE_ID    (numeric node id) OR VZ_NODE_GROUP
#   VZ_TOKEN      (session token) OR VZ_USER + VZ_PASSWORD to fetch token
#   CI_REGISTRY_IMAGE (image repo path)
# Optional:
#   KEEP_VOLUMES=true|false (default true)
#   SEQUENTIAL=true|false (for scaled nodes)
#   DELAY=30 (seconds between sequential redeploy)

TAG="${1:-}"
if [ -z "$TAG" ]; then
  echo "ERROR: Tag argument required" >&2
  exit 1
fi

if [ -z "${VZ_API_HOST:-}" ] || [ -z "${VZ_ENV_NAME:-}" ]; then
  echo "ERROR: VZ_API_HOST and VZ_ENV_NAME must be set" >&2
  exit 1
fi

API_BASE="https://${VZ_API_HOST}/1.0"

fetch_token() {
  if [ -n "${VZ_TOKEN:-}" ]; then
    echo "$VZ_TOKEN"
    return 0
  fi
  if [ -z "${VZ_USER:-}" ] || [ -z "${VZ_PASSWORD:-}" ]; then
    echo "ERROR: Provide VZ_TOKEN or VZ_USER/VZ_PASSWORD" >&2
    exit 1
  fi
  # Auth endpoint (may differ; adjust if provider uses different path)
  RESP=$(curl -sS -X POST "${API_BASE}/users/authenticate" -d "email=${VZ_USER}" -d "password=${VZ_PASSWORD}") || {
    echo "Authentication request failed" >&2; exit 1; }
  TOKEN=$(echo "$RESP" | sed -n 's/.*"session":"\([^"]*\)".*/\1/p')
  if [ -z "$TOKEN" ]; then
    echo "ERROR: Could not parse session token from response: $RESP" >&2
    exit 1
  fi
  echo "$TOKEN"
}

SESSION=$(fetch_token)
echo "Using session token: ${SESSION:0:8}..."

IMAGE_REF="${CI_REGISTRY_IMAGE}:${TAG}"
echo "Redeploying image tag: $IMAGE_REF"

KEEP_VOLUMES=${KEEP_VOLUMES:-true}
SEQUENTIAL=${SEQUENTIAL:-false}
DELAY=${DELAY:-30}

redeploy_payload() {
  # Form data parameters for redeploycontainer endpoint
  if [ -n "${VZ_NODE_ID:-}" ]; then
    cat <<EOF
envName=${VZ_ENV_NAME}&nodeId=${VZ_NODE_ID}&tag=${TAG}&session=${SESSION}
EOF
  elif [ -n "${VZ_NODE_GROUP:-}" ]; then
    cat <<EOF
envName=${VZ_ENV_NAME}&nodeGroup=${VZ_NODE_GROUP}&tag=${TAG}&session=${SESSION}
EOF
  else
    echo "ERROR: Need VZ_NODE_ID or VZ_NODE_GROUP" >&2
    exit 1
  fi
}

REDEPLOY_ENDPOINT="${API_BASE}/environment/control/rest/redeploycontainer"
RESP=$(curl -sS -X POST "$REDEPLOY_ENDPOINT" --data "$(redeploy_payload)") || {
  echo "Redeploy request failed" >&2; exit 1; }

echo "Raw response: $RESP"

RESULT=$(echo "$RESP" | sed -n 's/.*"result":\([0-9]*\).*/\1/p')
if [ "$RESULT" != "0" ]; then
  echo "ERROR: Redeploy returned non-zero result code ($RESULT)" >&2
  exit 1
fi

echo "Redeploy initiated successfully. Check dashboard for progress/logs."