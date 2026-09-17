# UI Cleanup Plan

## Context

Three targeted changes:

1. **Theme-token cleanup for `InfoBlock` and `DeleteZone`** — Components currently reference hardcoded `Color.*` values. The WalletTheme's `ColorScheme` already defines the correct values in the right slots. Components should read those slots instead of hardcoding, making the theme the single source of truth. Visual result stays the same because the theme values and the hardcoded values are identical (or semantically equivalent) in the forced-light context where these components live.

2. **Remove empty-state icon** — The 64 dp file icon added above "No passes yet" should be removed; text-only empty state is preferred.

3. **Revert `DottedDivider` height change** — The `height = 0.dp` original behaviour should be restored; the density-based height calculation introduced earlier should be removed.

---

## Verified theme values (from `ColorScheme.kt`)

`PassCardExpansion` wraps all card content in `WalletTheme(darkTheme = false)`, so `InfoBlock` always renders in the **light** scheme. Values confirmed by reading `design-system/src/main/java/com/luntikius/wallet/designsystem/theme/ColorScheme.kt` directly:

**Light scheme — explicitly set:**

| Token | Defined value | Matches hardcoded |
|---|---|---|
| `surface` | `Color.White` (`#FFFFFFFF`) | = `Color.White` in InfoBlock ✓ |
| `onSurface` | `Color.Black` (`#FF000000`) | ≈ `Color(0xFF1A1A1A)` — visually identical ✓ |
| `primary` | `Color(0xFF1850C3)` | = `Color(0xFF1850C3)` in InfoBlock exactly ✓ |
| `error` | `Color(0xFFB3261E)` | already used by `DeleteZone` |
| `onError` | `Color.White` | = white ✓ |
| `onSurfaceVariant` | `Color(0xFF44474F)` | used for non-hover delete zone content ✓ |

**Why NOT to use `surfaceContainerHigh`:** this slot is NOT explicitly set in `ColorScheme.kt`. It falls back to Material 3 defaults (light gray in light mode, dark gray in dark mode) — which is why our first attempt with that token turned InfoBlock gray.

---

## Changes

### 1. `InfoBlock.kt` — replace hardcoded colors with theme tokens

`app/src/main/java/com/luntikius/wallet/ui/components/pass/pkpass/InfoBlock.kt`

Remove the two private color constants:
```kotlin
private val InfoBlockTextColor = Color(0xFF1A1A1A)  // ← delete
private val InfoBlockLinkColor = Color(0xFF1850C3)   // ← delete
```

Replace usages in the composable:

| Was | Becomes |
|---|---|
| `color = Color.White` (Surface) | `color = MaterialTheme.colorScheme.surface` |
| `color = InfoBlockTextColor` (title Text) | `color = MaterialTheme.colorScheme.onSurface` |
| `color = InfoBlockTextColor` (HtmlText) | `color = MaterialTheme.colorScheme.onSurface` |
| `linkColor = InfoBlockLinkColor` | `linkColor = MaterialTheme.colorScheme.primary` |

Remove the now-unused `import androidx.compose.ui.graphics.Color`.

### 2. `DeleteZone.kt` — no component change needed

Already uses `MaterialTheme.colorScheme.error`, `onError`, and `onSurfaceVariant`. Nothing to do here.

### 3. `PassGridScreen.kt` — remove empty-state icon

`app/src/main/java/com/luntikius/wallet/ui/screens/PassGridScreen.kt`

Revert `EmptyPassGridState` to text-only:
- Remove the `Icon(...)` call
- Remove the `Spacer` between the icon and the title
- Remove the `import androidx.compose.foundation.layout.size` added for the icon (verify it is not used elsewhere in the file before removing)

### 4. `DottedDivider.kt` — revert height change

`app/src/main/java/com/luntikius/wallet/ui/components/common/DottedDivider.kt`

Restore the original implementation:
- Remove `val density = LocalDensity.current` and `val canvasHeightDp = ...`
- Change `.height(canvasHeightDp)` back to `.height(0.dp)`
- Change `val centerY = size.height / 2f` + `center = Offset(x, centerY)` back to `center = Offset(x, 0f)`
- Remove `import androidx.compose.ui.platform.LocalDensity`
- Restore `import androidx.compose.foundation.layout.height` (it was replaced — ensure `height` import is present for `.height(0.dp)`)
- Restore the KDoc comment to say "Height is 0dp because dots are centered on the divider line and extend above/below it."

---

## Verification

1. **Build** — `./gradlew :app:compileDebugKotlin ktlintCheck detekt` must pass with no new errors
2. **InfoBlock colours** — open a PKPass with back fields; info blocks must appear white with black text and blue links, identical to before
3. **DeleteZone** — drag a card; the delete zone must appear with a neutral background, turning red when hovering over it, with white icon and text — identical to before
4. **Empty state** — delete all passes (or test with empty DB); screen shows "No passes yet" + "Tap + to add a pass" text only, no icon above
5. **Dotted divider** — open an event ticket pass; the perforated divider dots should appear as before (dots centred on the divider line, extending above/below the 0-height canvas)
