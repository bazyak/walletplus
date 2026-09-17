# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An Android wallet application built with Kotlin and Jetpack Compose. The project uses modern Android development practices with Material 3 design system and edge-to-edge display support.

**Package:** `com.bazyak.walletplus`
**Min SDK:** 33 (Android 13)
**Target SDK:** 36
**Compile SDK:** 36

## Build System

This project uses Gradle with Kotlin DSL and the Version Catalog feature for dependency management.

### Common Commands

**Build the app:**
```bash
./gradlew build
```

**Install debug build on connected device:**
```bash
./gradlew installDebug
```

**Run unit tests:**
```bash
./gradlew test
```

**Run a specific test class:**
```bash
./gradlew test --tests com.bazyak.walletplus.ExampleUnitTest
```

**Run instrumented tests (requires emulator or device):**
```bash
./gradlew connectedAndroidTest
```

**Run a specific instrumented test:**
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.bazyak.walletplus.ExampleInstrumentedTest
```

**Clean build artifacts:**
```bash
./gradlew clean
```

**Run all lint checks:**
```bash
./gradlew ktlintCheck detekt lintDebug
```

**Auto-fix formatting:**
```bash
./gradlew ktlintFormat
```

**View lint reports:**
- ktlint: `app/build/reports/ktlint/`
- detekt: `app/build/reports/detekt/detekt.html`
- Android Lint: `app/build/reports/lint-results-debug.html`

**Install git hooks (for pre-commit auto-formatting):**
```bash
brew install lefthook  # macOS
# or: npm install -g lefthook
lefthook install
```

**Bypass pre-commit hooks (emergencies only):**
```bash
git commit --no-verify -m "Emergency fix"
```

## Architecture

### Multimodal Pass Format Support

The app is architected to support multiple pass formats (PKPass, Google Wallet, etc.) through a **Parser Registry pattern**:

- **PassParser Interface** (`data/parser/PassParser.kt`): Defines contract for parsing different pass formats
- **ParserRegistry** (`data/parser/ParserRegistry.kt`): Factory that resolves the correct parser based on file type/MIME type
- **Format-Agnostic Storage**: The `Pass` entity (`data/model/Pass.kt`) abstracts away format differences, storing common fields (organization, colors, images) while preserving format-specific data in a `rawData` JSON blob

To add a new pass format:
1. Implement the `PassParser` interface
2. Register the parser in `ParserRegistry`
3. Add the new `PassFormat` enum value in `data/model/PassFormat.kt`

Currently implemented: PKPass (Apple Wallet) format via `PKPassParser`

### Design System Module

**Location:** `design-system/`
**Package:** `com.bazyak.walletplus.designsystem`

The app uses a centralized design system module for all UI theming and reusable components.

#### Structure

```
design-system/
├── theme/           # Main theme system
│   ├── WalletTheme.kt      # Theme composable
│   ├── ColorScheme.kt      # Material 3 color schemes
│   ├── Typography.kt       # Typography scale
│   ├── Shape.kt            # Shape system
│   └── Dimensions.kt       # Size constants
├── foundation/      # Design tokens
│   ├── color/              # Color system
│   ├── typography/         # Text styles
│   ├── spacing/            # Spacing scale
│   └── animation/          # Animation specs
└── components/      # Reusable components
    ├── button/
    ├── card/
    ├── dialog/
    ├── input/
    ├── menu/
    ├── navigation/
    ├── feedback/
    └── picker/
```

#### Key Features

**Theme System:**
- Black & White color scheme with Blue accent (#FF0077b6)
- No dynamic color support (fixed color schemes)
- Automatic dark/light theme switching
- Material 3 exclusively

**Color System:**
- 8 pastel pass color palettes (`PassColorPalette` data class)
- WCAG AA contrast validation (4.5:1 ratio)
- Color utilities: `parseColor`, `ensureContrast`, `blendColors`, `createCustomPassGradient`
- Semantic color tokens for consistent usage

**Typography:**
- Complete Material 3 type scale (Display, Headline, Title, Body, Label)
- Semantic text styles (primary/secondary variants)
- Replaces manual `.copy(alpha = 0.5)` patterns

**Spacing:**
- 4dp-based grid system
- 9-level scale: `extraSmall` (4dp) to `massive` (48dp)
- Accessed via `MaterialTheme.spacing.medium`

**Components (11 total):**
- Buttons: Filled, Outlined, Text, Icon
- Dialogs: AlertDialog
- Input: TextField (3 variants)
- Menus: DropdownMenu, MenuItem
- Navigation: TopAppBar
- Feedback: EmptyState, Snackbar, ProgressIndicator
- Pickers: ColorPicker

**Usage Examples:**

```kotlin
// Theme
WalletTheme {
    // App content
}

// Spacing
Box(modifier = Modifier.padding(MaterialTheme.spacing.medium))

// Pass colors
val palette = MaterialTheme.walletColors[0]

// Semantic text styles
Text(
    text = "Primary",
    style = MaterialTheme.textStyles.bodyPrimary
)
```

**Migration Notes:**
- Old `ui/theme` package replaced by design-system
- `CustomPassColors` → `DefaultPassColors` (PassColorPalette data class)
- `PassUtils` color functions → `designsystem.foundation.color.ColorUtils`
- `AnimationConstants` → `AnimationTokens`
- Hardcoded spacing → `MaterialTheme.spacing` tokens
- Opacity patterns → Semantic text styles

**See Also:** `/design-system/README.md` for detailed documentation

### Data Layer

- **Room Database**: Single-source-of-truth for passes (`PassDatabase`)
  - Database name: `pass_database`
  - Current version: 5 (includes 4 migrations for schema evolution)
  - Singleton pattern with thread-safe initialization
- **Repository Pattern**: `PassRepository` interface with `PassRepositoryImpl` handles pass import/export/deletion/refresh
- **DAO**: `PassDao` provides reactive Flow-based queries with custom ordering (`displayOrder ASC, importedDate DESC`)
- **Type Converters**: `PassTypeConverters` handles enum serialization for Room

**Database Schema (Pass Entity):**
The `Pass` entity includes:
- Common fields: `organizationName`, `description`, `iconPath`, `logoPath`, color fields
- Format-specific: `rawData` (JSON blob preserving format-specific data)
- Refresh tracking: `lastRefreshDate`, `autoRefreshEnabled`
- Display management: `displayOrder` (for drag-and-drop grid reordering)
- Category: `PassCategory` enum (BOARDING_PASS, EVENT_TICKET, COUPON, STORE_CARD, GENERIC)

**Database Migrations:**
- v1→v2: Added image path columns (logoPath, stripPath, backgroundPath)
- v2→v3: Added displayOrder for custom grid ordering
- v3→v4: Added lastRefreshDate for refresh tracking
- v4→v5: Added autoRefreshEnabled boolean

**Key flows:**
- Pass import: URI → ParserRegistry → Parser → Pass entity → Room database
- Pass deletion: Database removal + cleanup of internal storage files (assets directory)
- Pass refresh: Network fetch → Parser update → Database sync

**Internal Storage Structure:**
Pass assets are stored in app's internal storage:
```
/app/files/passes/pkpass/{serialNumber}/
  ├── icon.png (or @2x, @3x variants)
  ├── logo.png
  ├── strip.png
  └── background.png
```
PKPassParser extracts ZIP archives and selects best image resolution (@3x > @2x > @1x)

### UI Layer

- **Compose-based UI**: All UI components built with Jetpack Compose
- **Single Activity**: `MainActivity` serves as the entry point, using Compose for all screens
- **Navigation**: Jetpack Compose Navigation with shared element transitions between grid and detail screens
- **Material 3**: Uses Material Design 3 with dynamic color support (Android 12+)
- **Edge-to-Edge**: App uses edge-to-edge display with proper inset handling via Scaffold

**Navigation Structure:**
- Grid Screen (`PassGridScreen`): Displays all passes in a grid layout with drag-and-drop reordering
- Detail Screen (`PassDetailScreen`): Shows expanded pass with barcode/QR code
- Routes defined in `ui/navigation/NavGraph.kt`
- Shared element transitions between grid and detail screens

**Grid Features:**
- Drag-and-drop reordering: Uses Reorderable library with custom `displayOrder` persistence
- Pull-to-refresh: Manual pass refresh trigger
- Swipe-to-delete: Delete zone appears during drag operations
- Optimistic UI updates during drag operations

### ViewModel Layer

- **PassViewModel**: Single ViewModel manages all pass state using StateFlow
- **State Management**: Uses sealed classes for operation status tracking:
  - `ImportStatus`: Idle, Loading, Success, Error(message)
  - `RefreshStatus`: Idle, Loading(passId), Success(updatedCount), Error(message, passId)
- **Reactive Updates**: Exposes `Flow<List<Pass>>` from repository for automatic UI updates
- **Coroutine Scope**: Uses `viewModelScope.launch {}` for async operations

### Theme System

Located in `app/src/main/java/com/bazyak/walletplus/ui/theme/`:
- `Theme.kt`: Contains `WalletTheme` composable that handles dark/light mode and dynamic colors
- `Color.kt`: Color definitions for the app
- `Type.kt`: Typography configuration

The theme supports:
- Dark and light color schemes
- Dynamic color extraction from system (Android 12+)
- System theme following via `isSystemInDarkTheme()`

### Network Layer

**PKPass Update Protocol** (`data/network/PKPassUpdateService.kt`):
- Extracts web service URL and auth token from pass `rawData` JSON
- Creates dynamic Retrofit instance for each pass's specific service
- Makes authenticated requests to fetch updated pass data
- Handles standard PKPass web service responses:
  - 200: Updated (new pass data provided)
  - 304: Not Modified
  - 401: Unauthorized
  - 404: Deleted/Voided
  - Network errors

**Network Module** (`data/network/NetworkModule.kt`):
- Centralized Gson and OkHttp client configuration
- Shared across all network operations
- Includes logging interceptor for debugging

### Background Refresh System

**WorkManager Integration** (`data/worker/PassRefreshWorker.kt`):
- Periodic background refresh using WorkManager
- Runs every 24 hours with 2-hour flex period
- Constraints: WiFi + battery not low
- Exponential backoff on failure
- Filters passes: only refreshes if `autoRefreshEnabled == true`
- Format-gated: only PKPASS format supports refresh currently

**Scheduling in MainActivity:**
```kotlin
PeriodicWorkRequestBuilder(1 day, 2 hour flex)
    .setConstraints(WiFi + battery not low)
    .setBackoffPolicy(EXPONENTIAL)
    .enqueueUniquePeriodicWork("pass_refresh", KEEP)
```

### Key Architectural Decisions

1. **Manual Dependency Injection**: Database, repository, and ViewModel are manually instantiated in `MainActivity`. No DI framework (Hilt/Koin) is used. Note: `PassRefreshWorker` re-instantiates dependencies for background execution.
2. **Coroutines & Flow**: All async operations use Kotlin coroutines; reactive data uses Flow
3. **Internal Storage**: Pass assets (images, raw files) stored in app's internal storage directory, referenced by path in Room database
4. **Intent Handling**: App handles `ACTION_VIEW` intents for `.pkpass` files via `MainActivity.onNewIntent()`
5. **Single ViewModel**: One ViewModel manages all pass state for simplified state management
6. **Sealed Class Status Pattern**: Import and refresh operations use sealed classes for type-safe state representation

### Code Quality

The project enforces code quality through three lint tools:

- **ktlint**: Kotlin code formatting (auto-fixable via pre-commit hooks)
- **detekt**: Static code analysis for code smells and complexity
- **Android Lint**: Android-specific checks for API usage, resources, and best practices

Pre-commit hooks automatically format code before commits using Lefthook. All checks run in CI on pull requests with results uploaded to GitHub Security (SARIF format).

## Dependency Management

Dependencies are managed via Version Catalog in `gradle/libs.versions.toml`. To add a new dependency:
1. Add the version to `[versions]` section
2. Add the library to `[libraries]` section
3. Reference it in `app/build.gradle.kts` using `libs.` prefix

## Code Organization

- **Main source:** `app/src/main/java/com/bazyak/walletplus/`
- **Tests:** `app/src/test/java/com/bazyak/walletplus/`
- **Instrumented tests:** `app/src/androidTest/java/com/bazyak/walletplus/`
- **Resources:** `app/src/main/res/`

## Java/Kotlin Configuration

- **Java Version:** 11 (source and target compatibility)
- **JVM Target:** 11
- **Kotlin Version:** 2.0.21
