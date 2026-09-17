#!/bin/bash
set -e

echo "Version bump script starting..."

# Get version bump type from environment variable, default to 'patch'
VERSION_BUMP="${VERSION_BUMP:-patch}"

# Check if gradle.properties exists
if [ ! -f "gradle.properties" ]; then
  echo "Error: gradle.properties not found"
  exit 1
fi

# Version bump logic
if [ "$VERSION_BUMP" != "none" ]; then
  echo "Bumping version (type: $VERSION_BUMP)..."

  # Read current version
  CURRENT_VERSION=$(grep "^VERSION_NAME=" gradle.properties | cut -d'=' -f2)
  CURRENT_CODE=$(grep "^VERSION_CODE=" gradle.properties | cut -d'=' -f2)
  CURRENT_WEAR_CODE=$(grep "^WEAR_VERSION_CODE=" gradle.properties | cut -d'=' -f2)

  if [ -z "$CURRENT_WEAR_CODE" ]; then
    CURRENT_WEAR_CODE=$((CURRENT_CODE + 1000))
    echo "WEAR_VERSION_CODE not found; using derived current Wear code $CURRENT_WEAR_CODE"
  fi

  echo "Current version: $CURRENT_VERSION (phone code $CURRENT_CODE, Wear code $CURRENT_WEAR_CODE)"

  # Parse version into components
  IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
  MAJOR=${VERSION_PARTS[0]}
  MINOR=${VERSION_PARTS[1]}
  PATCH=${VERSION_PARTS[2]}

  # Apply version bump
  case "$VERSION_BUMP" in
    major)
      MAJOR=$((MAJOR + 1))
      MINOR=0
      PATCH=0
      ;;
    minor)
      MINOR=$((MINOR + 1))
      PATCH=0
      ;;
    patch)
      PATCH=$((PATCH + 1))
      ;;
    *)
      echo "Error: Invalid VERSION_BUMP value: $VERSION_BUMP"
      echo "Valid values are: major, minor, patch, none"
      exit 1
      ;;
  esac

  # Calculate new version
  NEW_VERSION="$MAJOR.$MINOR.$PATCH"
  NEW_CODE=$((CURRENT_CODE + 1))
  NEW_WEAR_CODE=$((CURRENT_WEAR_CODE + 1))

  # Update gradle.properties
  # Use sed with backup for compatibility across platforms
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS requires empty string after -i
    sed -i '' "s/^VERSION_CODE=.*/VERSION_CODE=$NEW_CODE/" gradle.properties
    sed -i '' "s/^WEAR_VERSION_CODE=.*/WEAR_VERSION_CODE=$NEW_WEAR_CODE/" gradle.properties
    sed -i '' "s/^VERSION_NAME=.*/VERSION_NAME=$NEW_VERSION/" gradle.properties
  else
    # Linux
    sed -i "s/^VERSION_CODE=.*/VERSION_CODE=$NEW_CODE/" gradle.properties
    sed -i "s/^WEAR_VERSION_CODE=.*/WEAR_VERSION_CODE=$NEW_WEAR_CODE/" gradle.properties
    sed -i "s/^VERSION_NAME=.*/VERSION_NAME=$NEW_VERSION/" gradle.properties
  fi

  echo "Version bumped to $NEW_VERSION (phone code $NEW_CODE, Wear code $NEW_WEAR_CODE)"

  # Export to GITHUB_ENV for use in subsequent GitHub Actions steps
  if [ -n "$GITHUB_ENV" ]; then
    echo "NEW_VERSION=$NEW_VERSION" >> "$GITHUB_ENV"
    echo "NEW_VERSION_CODE=$NEW_CODE" >> "$GITHUB_ENV"
    echo "NEW_WEAR_VERSION_CODE=$NEW_WEAR_CODE" >> "$GITHUB_ENV"
    echo "Exported NEW_VERSION, NEW_VERSION_CODE, and NEW_WEAR_VERSION_CODE to GITHUB_ENV"
  fi

  # Also export to GITHUB_OUTPUT if available (for step outputs)
  if [ -n "$GITHUB_OUTPUT" ]; then
    echo "NEW_VERSION=$NEW_VERSION" >> "$GITHUB_OUTPUT"
    echo "NEW_VERSION_CODE=$NEW_CODE" >> "$GITHUB_OUTPUT"
    echo "NEW_WEAR_VERSION_CODE=$NEW_WEAR_CODE" >> "$GITHUB_OUTPUT"
    echo "Exported NEW_VERSION, NEW_VERSION_CODE, and NEW_WEAR_VERSION_CODE to GITHUB_OUTPUT"
  fi
else
  echo "Skipping version bump (VERSION_BUMP=none)"

  # Even when skipping, export current version
  CURRENT_VERSION=$(grep "^VERSION_NAME=" gradle.properties | cut -d'=' -f2)
  CURRENT_CODE=$(grep "^VERSION_CODE=" gradle.properties | cut -d'=' -f2)
  CURRENT_WEAR_CODE=$(grep "^WEAR_VERSION_CODE=" gradle.properties | cut -d'=' -f2)

  if [ -z "$CURRENT_WEAR_CODE" ]; then
    CURRENT_WEAR_CODE=$((CURRENT_CODE + 1000))
  fi

  if [ -n "$GITHUB_ENV" ]; then
    echo "NEW_VERSION=$CURRENT_VERSION" >> "$GITHUB_ENV"
    echo "NEW_VERSION_CODE=$CURRENT_CODE" >> "$GITHUB_ENV"
    echo "NEW_WEAR_VERSION_CODE=$CURRENT_WEAR_CODE" >> "$GITHUB_ENV"
  fi

  if [ -n "$GITHUB_OUTPUT" ]; then
    echo "NEW_VERSION=$CURRENT_VERSION" >> "$GITHUB_OUTPUT"
    echo "NEW_VERSION_CODE=$CURRENT_CODE" >> "$GITHUB_OUTPUT"
    echo "NEW_WEAR_VERSION_CODE=$CURRENT_WEAR_CODE" >> "$GITHUB_OUTPUT"
  fi
fi

echo "Version bump complete!"
