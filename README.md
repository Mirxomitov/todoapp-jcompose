# Mir Tasks

A simple, offline to-do list for Android, built with Kotlin and Jetpack Compose (Material 3).

- Package: `uz.mir.tasks`
- Features: tasks with notes, due dates and priorities; All / Today / Upcoming / Done filters; swipe to delete with undo; light and dark themes; English, Uzbek and Russian.
- Storage: a JSON file in app-private storage. No network access, no analytics.

## Build

```sh
./gradlew testDebugUnitTest          # unit tests
./gradlew bundleRelease              # signed AAB (needs keystore.properties)
```

Release signing reads `keystore.properties` (git-ignored) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
