# Decisions index

## Topics
- `docs/decisions/build.md` — Gradle, JDK, version pins, disk limits
- `docs/decisions/data-model.md` — people, day records, payments, how a month's charge is worked out
- `docs/decisions/screens.md` — the four tabs and what each opens on

## Next
- First implementation in progress: Room data layer, People and Dues screens, Today and Month screens.
- Not built yet: reminders, home-screen widget, backup/export, Bangla UI, release signing.

## Gotchas (cross-cutting)
- System JDK 21 is corrupt: build with `JAVA_HOME=~/.gradle/jdks/temurin-21 ./gradlew --offline <task>`.
- Only `material-icons-core` is available; the extended icon set is not in the Gradle cache.
- The ADB phone is shared with other sessions; check the foreground app before taking the screen.
