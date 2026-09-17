# Plan: Wallet App Touchups

## Context

Several improvements across architecture, dependencies, tests, and CI/CD are needed to clean up the codebase. The key insight from exploration is that **most of the hard work is already done as untracked files** — split ViewModels, Koin modules, WalletApplication, and the first ViewModel test all exist but haven't been wired into the running app yet. The main work is connecting these new pieces and finishing the remaining items.

---

## Current State (Untracked, Already Written)

These files exist but aren't tracked by git and aren't yet hooked into the running app:
- `ui/viewmodel/PassGridViewModel.kt` (143 lines)
- `ui/viewmodel/PassPreviewViewModel.kt` (96 lines)
- `ui/viewmodel/CustomPassBuilderViewModel.kt` (30 lines)
- `ui/viewmodel/ImportStatusHolder.kt` (14 lines)
- `ui/viewmodel/PassUiState.kt` (15 lines — sealed classes `ImportStatus`, `PreviewStatus`)
- `di/KoinModules.kt` — full DI graph (data, domain, UI modules)
- `WalletApplication.kt` — starts Koin in `onCreate`
- `app/src/test/.../PassGridViewModelTest.kt` — 3 tests using MockK

---

## Task 1: Wire Up Koin + Split ViewModels

### 1a. Add Gradle dependencies
**File:** `gradle/libs.versions.toml`
- Add `koin = "4.0.4"` (latest stable)
- Add `mockk = "1.13.16"`
- Add `coroutines-test` entry (already have coroutines version `1.8.0`)
- Add libraries: `koin-android`, `koin-compose`, `mockk`, `kotlinx-coroutines-test`

**File:** `app/build.gradle.kts`
- Add `implementation(libs.koin.android)` + `implementation(libs.koin.compose)`
- Add `testImplementation(libs.mockk)` + `testImplementation(libs.kotlinx.coroutines.test)`

### 1b. Register Application class
**File:** `app/src/main/AndroidManifest.xml`
- Add `android:name=".WalletApplication"` to the `<application>` tag

### 1c. Refactor MainActivity
**File:** `app/src/main/java/com/luntikius/wallet/MainActivity.kt`
- Remove manual DB/repo/ViewModel instantiation (lines 59–67)
- Replace `private lateinit var viewModel: PassViewModel` with:
  ```kotlin
  private val gridViewModel: PassGridViewModel by viewModel()
  private val previewViewModel: PassPreviewViewModel by viewModel()
  ```
- Update `PassNavGraph(...)` call to pass `gridViewModel` and `previewViewModel`
- Update `LaunchedEffect(newIntentUri)` to call `previewViewModel.previewPass(uri)`
- Add `import org.koin.androidx.viewmodel.ext.android.viewModel`

### 1d. Refactor NavGraph
**File:** `app/src/main/java/com/luntikius/wallet/ui/navigation/NavGraph.kt`
- Change signature from `viewModel: PassViewModel` to:
  ```kotlin
  gridViewModel: PassGridViewModel,
  previewViewModel: PassPreviewViewModel,
  ```
- `CustomPassBuilderViewModel` is obtained via `koinViewModel()` inside `CustomPassBuilderScreen` (no need to thread it through NavGraph)
- `CameraScanScreen.onScanResult` callback:
  - `ScanResult.UrlDetected` → call `previewViewModel.downloadAndPreviewPass(url)` directly (no `handleScannedCode`)
  - `ScanResult.BarcodeDetected` → just navigate (no VM call needed)
  - `onCancel` → just `navController.popBackStack()` (no `clearScanResult`)
- `PassPreviewScreen` `onAdd` → call `previewViewModel.confirmAddPass()` directly
- `PassPreviewScreen` `onCancel` → call `previewViewModel.cancelPreview()` directly
- Remove import of `PassViewModel`

### 1e. Update screens
**`InitialScreen.kt`**: Change parameter type from `PassViewModel` to `PassPreviewViewModel`

**`PassGridScreen.kt`**: Change parameter type from `PassViewModel` to `PassGridViewModel`
- `importStatus` now comes from `PassGridViewModel.importStatus` (which wraps `ImportStatusHolder`)

**`PassPreviewScreen.kt`**: Change parameter type from `PassViewModel` to `PassPreviewViewModel`

**`CustomPassBuilderScreen.kt`**: Change parameter type from `PassViewModel` to `CustomPassBuilderViewModel`
- Add `val viewModel: CustomPassBuilderViewModel = koinViewModel()` if not already wired as parameter
- The screen receives `barcodeValue`/`barcodeFormat` args and `onCancel`/`onPassCreated` callbacks; ViewModel obtained internally

### 1f. Delete PassViewModel
**File:** `app/src/main/java/com/luntikius/wallet/ui/viewmodel/PassViewModel.kt`
- Remove after all screens are migrated
- `ImportStatus` and `PreviewStatus` sealed classes defined there move to `PassUiState.kt`

---

## Task 2: Replace Gson with kotlinx.serialization

### 2a. Add dependencies
**File:** `gradle/libs.versions.toml`
- Add `kotlinx-serialization-json = "1.7.3"`
- Add `retrofit-kotlin-serialization` (JakeWharton's `retrofit2-kotlinx-serialization-converter` `1.0.0`)
- Add library entries

**File:** `app/build.gradle.kts`
- Add plugin `alias(libs.plugins.kotlin.serialization)` to the `plugins {}` block
- Add `implementation(libs.kotlinx.serialization.json)`
- Add `implementation(libs.retrofit.kotlin.serialization)`
- Remove `implementation(libs.retrofit.gson)` and `implementation(libs.gson)`

**File:** `gradle/libs.versions.toml`
- Add `kotlin-serialization` plugin entry (`org.jetbrains.kotlin.plugin.serialization`)
- Remove `gson` version/library entries

### 2b. Annotate data classes
Files involved:
- `data/parser/pkpass/PKPassJson.kt` — add `@Serializable` to all data classes; add `@SerialName` only where JSON field name differs from the Kotlin property name (PKPass uses camelCase which matches, but verify)
- `data/builder/CustomPassJson.kt` (or wherever `CustomPassJson` is defined) — add `@Serializable`

### 2c. Replace Gson calls

**`PKPassParser.kt`**:
- Replace `private val gson = Gson()` with `private val json = Json { ignoreUnknownKeys = true }`
- `gson.fromJson(String(bytes), PKPassJson::class.java)` → `json.decodeFromString<PKPassJson>(String(bytes))`
- `gson.toJson(...)` → `json.encodeToString(passJsonWithLocalizations)`

**`CustomPassBuilder.kt`**:
- `Gson().toJson(customPassJson)` → `Json.encodeToString(customPassJson)`

**`PassData.kt`**:
- `Gson().fromJson(rawData, PKPassJson::class.java)` → `Json { ignoreUnknownKeys = true }.decodeFromString<PKPassJson>(rawData)`
- `Gson().fromJson(rawData, CustomPassJson::class.java)` → similar

**`NetworkModule.kt`**:
- Remove `GsonBuilder().setLenient()...` singleton
- Replace `GsonConverterFactory.create(gson)` with `Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())`
- Add necessary imports for `kotlinx.serialization.json.Json`, `retrofit2.converter.kotlinx.serialization`

---

## Task 3: CI/CD — README Update Only

GitHub Actions is **already set up** (`pr-validation.yml`, `release.yml`). The README CI/CD section just needs to reflect this.

**File:** `docs/readme/` — update the CI/CD section to describe the existing GitHub Actions workflows.

---

## Task 4: Tests

### 4a. `PassGridViewModelTest.kt` is already written
The file at `app/src/test/.../PassGridViewModelTest.kt` has 3 tests. Needs MockK + coroutines-test dependencies (Task 1a).

### 4b. Add `PassPreviewViewModelTest.kt`
Tests to cover:
- `previewPass` success → `previewStatus == Ready`, `previewPass != null`
- `previewPass` failure → `previewStatus == Error`
- `confirmAddPass` success → `ImportStatus.Success` emitted via `ImportStatusHolder`
- `cancelPreview` → calls `cleanupPreviewAssets`

### 4c. Add `CustomPassBuilderViewModelTest.kt`
Tests to cover:
- `createCustomPass` success → `ImportStatus.Success` via holder
- `createCustomPass` failure → `ImportStatus.Error` via holder

---

## Task 5: UI Error Handling

**Scope:** Show errors to users when network failures or parse errors occur.

- `PassGridScreen`: `RefreshStatus.Error` already has a message — ensure a Snackbar or toast is shown (check current handling)
- `PassPreviewScreen`: `PreviewStatus.Error` — show error message in UI instead of empty state
- These are low-priority and can be addressed after Tasks 1–4

---

## Critical Files

| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add Koin, MockK, kotlinx-serialization; remove Gson |
| `app/build.gradle.kts` | Add/remove deps, add serialization plugin |
| `app/src/main/AndroidManifest.xml` | Add `android:name=".WalletApplication"` |
| `MainActivity.kt` | Remove manual DI, use Koin `by viewModel()` |
| `ui/navigation/NavGraph.kt` | Use split ViewModels, remove PassViewModel |
| `ui/screens/InitialScreen.kt` | Type: PassViewModel → PassPreviewViewModel |
| `ui/screens/PassGridScreen.kt` | Type: PassViewModel → PassGridViewModel |
| `ui/screens/PassPreviewScreen.kt` | Type: PassViewModel → PassPreviewViewModel |
| `ui/screens/CustomPassBuilderScreen.kt` | Type: PassViewModel → CustomPassBuilderViewModel |
| `ui/viewmodel/PassViewModel.kt` | **Delete** |
| `data/parser/pkpass/PKPassJson.kt` | Add `@Serializable` |
| `data/parser/pkpass/PKPassParser.kt` | Gson → kotlinx.serialization |
| `data/builder/CustomPassBuilder.kt` | Gson → kotlinx.serialization |
| `data/model/PassData.kt` | Gson → kotlinx.serialization |
| `data/network/NetworkModule.kt` | Gson → kotlinx.serialization converter |

## New files to commit
- `di/KoinModules.kt`
- `WalletApplication.kt`
- `ui/viewmodel/PassGridViewModel.kt`
- `ui/viewmodel/PassPreviewViewModel.kt`
- `ui/viewmodel/CustomPassBuilderViewModel.kt`
- `ui/viewmodel/ImportStatusHolder.kt`
- `ui/viewmodel/PassUiState.kt`
- `app/src/test/.../PassGridViewModelTest.kt`
- `app/src/test/.../PassPreviewViewModelTest.kt` (new)
- `app/src/test/.../CustomPassBuilderViewModelTest.kt` (new)

---

## Task 6: Supported Formats Expansion

The Parser Registry pattern is already in place. Adding a new format is a mechanical process, but each format needs its own investigation before implementation.

### Candidate formats

| Format | File ext / MIME | Notes |
|--------|----------------|-------|
| **Google Wallet** (JWT passes) | `.jwt` / `application/jwt` | JSON payload inside a signed JWT; no ZIP |
| **ESPASS** | `.espass` / `application/vnd.espass-espass+zip` | Open format; ZIP with `main.json` |
| **Generic barcode** (already partial) | any | Custom passes already cover this via the builder |

### What each new format requires

1. **Model**: a data class for the format's JSON (e.g., `EspassJson`) annotated with `@Serializable`
2. **Parser**: a class implementing `PassParser` — reads the file, extracts assets, maps fields to the common `Pass` entity
3. **Registry**: one line in `ParserRegistry` to register the parser against the file extension / MIME type
4. **`PassFormat` enum**: add a value (e.g., `ESPASS`)
5. **`PassData`**: add a sealed subclass so `getPassData()` can reconstruct the format-specific JSON from `rawData`
6. **UI**: the existing `PassCardFront` / `PassCardBack` components may need a new variant if the format's visual structure differs significantly from PKPass (e.g., Google Wallet has a banner image layout)
7. **Category mapping**: map format-specific pass types to existing `PassCategory` values, or add new categories

### Recommended order

1. ESPASS first — simplest structure, closest to PKPass (ZIP + JSON), good proof-of-concept for the registry
2. Google Wallet JWT second — requires JWT parsing dependency (`java-jwt` or similar); more complex field mapping

### Files to create per format (example: ESPASS)

- `data/parser/espass/EspassJson.kt` — data model
- `data/parser/espass/EspassParser.kt` — implements `PassParser`
- `data/model/PassData.kt` — add `EspassPass` subclass
- `ui/components/pass/espass/` — card front/back if visual layout differs
- `app/src/test/.../EspassParserTest.kt` — parser unit tests with sample files

**Files to modify:**
- `data/parser/ParserRegistry.kt` — register the new parser
- `data/model/PassFormat.kt` — add enum value
- `app/src/main/AndroidManifest.xml` — add `<data android:mimeType="..."/>` to the intent filter if the format has a distinct MIME type

---

## Verification

1. `./gradlew build` — builds without errors
2. `./gradlew test` — all unit tests pass
3. `./gradlew ktlintCheck detekt lintDebug` — no lint errors
4. Manual: install debug APK, import a .pkpass file, verify grid/preview/custom pass flows all work
5. Manual: confirm import success toast shows on PassGridScreen after adding a pass
