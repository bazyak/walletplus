#!/bin/bash
set -e

echo "Setting up keystore..."

# Check required environment variables
if [ -z "$KEYSTORE_BASE64" ]; then
  echo "Error: KEYSTORE_BASE64 environment variable is not set"
  exit 1
fi

# Use GITHUB_WORKSPACE in CI, current directory for local testing
WORKSPACE_DIR="${GITHUB_WORKSPACE:-.}"
if [ ! -d "$WORKSPACE_DIR" ] || [ ! -w "$WORKSPACE_DIR" ]; then
  echo "Warning: $WORKSPACE_DIR not writable, using current directory"
  WORKSPACE_DIR="."
fi

# Create workspace directory if needed
mkdir -p "$WORKSPACE_DIR"

# Decode keystore
echo "Decoding keystore from base64..."
echo "$KEYSTORE_BASE64" > /tmp/keystore.b64.txt
cat /tmp/keystore.b64.txt | tr -d ' \n\r\t' | base64 -d > "$WORKSPACE_DIR/release.jks"
rm /tmp/keystore.b64.txt

# Verify keystore was created
if [ ! -f "$WORKSPACE_DIR/release.jks" ]; then
  echo "Error: Failed to create keystore file"
  exit 1
fi

echo "Keystore created successfully:"
ls -lh "$WORKSPACE_DIR/release.jks"

# Verify it's a valid keystore (optional but helpful)
file "$WORKSPACE_DIR/release.jks" || true

# Export keystore path to GITHUB_ENV for use in subsequent steps
if [ -n "$GITHUB_ENV" ]; then
  echo "KEYSTORE_FILE=$WORKSPACE_DIR/release.jks" >> "$GITHUB_ENV"
fi

echo "Keystore setup complete!"
echo "KEYSTORE_PATH=$WORKSPACE_DIR/release.jks"
