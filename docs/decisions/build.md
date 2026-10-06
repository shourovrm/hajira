# Build

## Current state
- Single `:app` module, package `com.rms.hazira`, minSdk 26, targetSdk 35, compileSdk 36.
- Gradle wrapper 8.13, AGP 8.13.0, Kotlin 2.4.0, KSP 2.3.9, Compose BOM 2026.06.01, Room 2.8.4: all copied from `~/repos/fyi-player` so the shared `~/.gradle` cache already holds them.
- SDK at `/home/rms/android-sdk` (`local.properties`, gitignored).

- Release builds only (user's rule, same as fyi-player): `assembleRelease`, `testReleaseUnitTest`. R8 minification is on.
- Signing: `hazira-release.jks` + `keystore.properties` in the repo root, both gitignored, chmod 600. This key is the only copy; an APK signed with another key cannot update the installed app.

## Open items
- The signing key has no backup outside this directory.

## Gotchas
- Build with `JAVA_HOME=~/.gradle/jdks/temurin-21 ./gradlew --offline <task>`; `/usr/lib/jvm/java-21-openjdk` is corrupt.
- `kotlinx-coroutines-test` is not in the cache, so unit tests cover pure functions only.

## Tried / rejected
