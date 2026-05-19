# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Use Android Studio or the Gradle wrapper from the project root:

```powershell
# Build debug APK
.\gradlew assembleDebug

# Run unit tests
.\gradlew test

# Run instrumented tests (requires connected device/emulator)
.\gradlew connectedAndroidTest

# Run a single unit test class
.\gradlew test --tests "com.example.taller3_movil.ExampleUnitTest"

# Install and run on connected device
.\gradlew installDebug
```

## Architecture

Single-Activity app (`MainActivity`) using **Jetpack Compose** with **Compose Navigation**.

**Navigation** (`navigation.kt`): Routes are defined as an `enum class Screens` with values `Login`, `Register`, `Map`, `Users`. The `Navigation()` composable bootstraps `NavHost` with `Login` as the start destination. All screen transitions go through this NavHost — pass `navController` down to screens that need to navigate.

**Screens** (`screens/`): Each screen is a top-level `@Composable` function receiving a `NavController`. Current state:
- `Login.kt` / `Register.kt` — UI scaffolded, Firebase logic not yet wired
- `Map.kt` / `Users.kt` — empty stubs

**Firebase** (configured via `google-services.json`):
- `firebase-auth` / `firebase-auth-ktx` — Authentication
- `firebase-database` — Realtime Database
- `firebase-storage` — File/image storage

`FirebaseAuth.getInstance()` is held on `MainActivity` but not yet passed to screens — screens will need to access it via `FirebaseAuth.getInstance()` directly or through a shared ViewModel.

**Theme** (`ui/theme/`): Standard Material3 theme split across `Color.kt`, `Type.kt`, and `Theme.kt`. Applied via `Taller3_MovilTheme` wrapper (not currently used in `MainActivity` — `Navigation()` is called without it).

## Key Dependencies

- Kotlin + Jetpack Compose BOM
- `androidx.navigation:navigation-compose` for screen routing
- Firebase Auth, Realtime Database, Storage (via Google Services plugin)
- minSdk 24, targetSdk 36
