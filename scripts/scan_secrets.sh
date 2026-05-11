#!/usr/bin/env bash

# envguard - Secret Scanner
# Scans staged files for common secret patterns before commit

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PATTERNS_FILE="${SCRIPT_DIR}/../config/patterns.txt"

if [[ ! -f "$PATTERNS_FILE" ]]; then
  echo "[envguard] ERROR: patterns file not found at $PATTERNS_FILE" >&2
  exit 1
fi

# Get list of staged files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM 2>/dev/null)

if [[ -z "$STAGED_FILES" ]]; then
  echo "[envguard] No staged files to scan."
  exit 0
fi

FOUND_SECRETS=0

while IFS= read -r file; do
  # Skip binary files
  if git diff --cached --name-only -z | xargs -0 file | grep -q "$file.*binary"; then
    continue
  fi

  while IFS='|' read -r label pattern; do
    # Skip comment lines and empty lines
    [[ "$label" =~ ^#.*$ || -z "$label" ]] && continue

    match=$(git diff --cached -U0 -- "$file" | grep '^+' | grep -v '^+++' | grep -Ei "$pattern" || true)

    if [[ -n "$match" ]]; then
      echo "[envguard] DETECTED [$label] in staged file: $file"
      echo "  Match: $(echo "$match" | head -1 | cut -c1-120)"
      FOUND_SECRETS=1
    fi
  done < "$PATTERNS_FILE"
done <<< "$STAGED_FILES"

if [[ "$FOUND_SECRETS" -eq 1 ]]; then
  echo ""
  echo "[envguard] Commit BLOCKED: potential secrets detected in staged changes."
  echo "[envguard] Review the matches above, remove secrets, and try again."
  echo "[envguard] To bypass (not recommended): git commit --no-verify"
  exit 1
fi

echo "[envguard] Scan passed. No secrets detected."
exit 0
