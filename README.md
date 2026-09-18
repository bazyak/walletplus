# .wallet+

**English** | [Русский](README.ru.md)

A fork of [luntikius/wallet](https://github.com/luntikius/wallet) — `.wallet`, an Android and Wear OS
manager for PKPass and custom barcode passes. This is the only upstream: all of the code here comes
from that project, plus the changes described below.

It also picks up an idea from [the-dise/Mir-Pay-Wallet](https://github.com/the-dise/Mir-Pay-Wallet):
keeping the watch display awake so a contactless payment can go through. Only the idea — no code
from that project is used here, and the mechanism below was written from scratch against on-device
measurements. It is not a port, and it ended up markedly smaller than the implementation that
inspired it.

Everything upstream `.wallet` does still works. What follows is what this fork changes.

## Keeping the watch display awake — rebuilt for OnePlus Watch

The Wear OS module gains a dedicated page, reachable by swiping **down** from the barcode, showing a
wallet pictogram and a countdown. While that page is open the display is held genuinely on, which is
what a contactless payment needs. The countdown resets when you leave the page, a double tap adds
five seconds, and reaching zero closes the app.

The problem: on OnePlus/Oppo firmware, turning your wrist away sends the display to `DOZE` even with
`FLAG_KEEP_SCREEN_ON` held. A dozing display still looks lit, but NFC only reads while the display
is genuinely `STATE_ON`, so payments silently fail. Waking the display back after the transition has
already committed produces a visible flash on every wrist turn.

The fix is to register an `AmbientLifecycleObserver` — which is what makes the system route
wrist-down through the app instead of swapping in its own watch face — and, on the leading edge of
`onEnterAmbient`, request the display back with `setTurnScreenOn(true)`, `FLAG_TURN_SCREEN_ON`, and a
`singleTop` self-relaunch. The relaunch matters because those window flags are only honoured while a
window is being brought to the front; without it the request is never processed. Firing on the
leading edge means the transition to `DOZE` is usually never committed at all, so there is nothing to
flash back from.

Notes from measuring this on hardware:

- `Display.state` is the signal that matters. `PowerManager.isInteractive` is a separate,
  laggier system-level flag that can read `false` while the display is on — it is not a useful
  diagnostic here.
- The keyguard is not involved: `isKeyguardLocked` stays false throughout a wrist turn.
- No probing loop, no wake lock, and no `WAKE_LOCK` permission are needed. Earlier iterations had
  all three; on-device logging showed `display.state` never leaves `ON` without them.
- Adaptive brightness can still dim the screen without changing `display.state`. That is a watch
  setting, not an app problem, and it does not affect NFC.

## PKPass device registration and unregistration

Upstream implemented only the pass-fetch half of the PassKit web service protocol
(`GET /v1/passes/{passTypeIdentifier}/{serialNumber}`). Device registration was missing entirely.

This matters more than it sounds. Many issuers treat a registration as the signal that someone is
actually carrying a pass, and serve an unregistered device `200` with an empty body forever — so
those passes could never update, with no error to explain why. (One issuer's pass would start
updating only while a *different* wallet app held a registration for it, and stop again the moment
that app was uninstalled.)

This fork implements both directions:

- `POST /v1/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}` when
  a pass is added — including passes restored from a backup archive, since the device identifier is
  per-install and is not carried in the archive
- `DELETE` on the same endpoint when a pass is removed, so issuers stop tracking passes you no longer
  hold

The device identifier and push token are generated once and kept for the life of the install; the
identifier has to be stable, because it is the key an issuer stores registrations under. The push
token is a stand-in — the protocol was designed around APNs, which Android cannot receive — but
issuers require the field and store it verbatim, and registration is what unblocks updates.

A newly added pass is now also refreshed immediately after registering, rather than waiting for the
next manual pull: the file you add is a snapshot from whenever it was issued, and a balance can
already be stale on arrival.

## Other fixes

- **Passes with no `messageEncoding` no longer fail to import.** The field is required by Apple's
  specification but omitted by a number of real issuers, and such passes render fine in Apple Wallet.
  It was declared non-optional, so deserialization threw and the whole pass was rejected — which
  surfaced both as a failed import and as "could not read the updated pass" on every refresh.
- **The expanded pass card updates in place.** It held a one-off snapshot refreshed only on a
  successful in-app pull, so a balance changed by the background worker did not appear until the card
  was reopened. It now observes the database directly.
- **A failing pass no longer hides the result for every other pass.** A batch refresh reported the
  first error it hit instead of the number of passes actually updated; per-pass failures are now
  skipped.
- **The daily refresh worker no longer retries indefinitely.** Any failure returned `Result.retry()`,
  which for periodic work means exponential-backoff reruns forever on a pass that can never succeed.
  Only transient failures are retried now, and at most three times.
- **HTTP 429 is reported as rate limiting** rather than a raw status code, and a `200` with an empty
  body is reported as an empty response rather than blamed on the parser.
- **Update responses are dumped to disk** at
  `Android/data/<applicationId>/files/update-dumps/<timestamp>_<status>_<serial>.bin`, written
  unconditionally for any status and any size. Issuers misuse status codes often enough that the raw
  bytes are the only reliable record of what arrived.
- **Fields with no value are no longer drawn.** A pass may declare a field with a key and an empty
  value; Apple Wallet renders nothing for those. This fork rendered them anyway and, finding no
  label, fell back to the raw PKPass key — so cards showed captions like `secondaryFields1` or
  `title` floating over empty space. Filtering happens at render time, on the text that would
  actually be drawn, so placeholder values such as `&nbsp;` are caught too, and no issuer-specific
  handling is needed.
- **The crown button closes the app** instead of leaving it in the background.
- **`ClickableText` replaced with a link-annotated `Text`.** The deprecated composable also
  swallowed every gesture across the whole text block, not just on the links.

## Build

Versions are managed by the root build script. The phone and the watch carry their own numbers,
since a change usually touches only one of them. Building a module that is behind the other catches
it up; building the one already ahead moves it forward; equal versions always bump. Numbers only
move when something is actually built, so an IDE sync or a test run leaves them alone.

Pairing does not depend on the version: the Data Layer matches on applicationId and signing key.

Release artifacts are named after the app and its version (`WalletPlus-1.0.14.apk`,
`WalletPlus-wear-1.0.14.apk`) rather than the default `app-release.apk`. Identically named builds
are indistinguishable once they leave the output directory, which makes it easy to sideload an old
APK and conclude a fix did not work.

Bump `VERSION_MAJOR` / `VERSION_MINOR` in `version.properties` by hand; leave the rest to the build.

## Credits

All credit for `.wallet` goes to its author. This fork exists because that app and the idea behind
Mir Pay Wallet solve adjacent halves of the same problem, and neither quite worked on a OnePlus
Watch.
