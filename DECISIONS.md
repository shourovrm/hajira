# Decisions index

## Topics
- `docs/decisions/build.md` — Gradle, JDK, version pins, disk limits
- `docs/decisions/data-model.md` — people, day records, payments, how a month's charge is worked out
- `docs/decisions/screens.md` — the four tabs and what each opens on

## Next
- Version 0.1.0 is built and pushed: Today, People, Month (three forms) and Dues, with daily, weekly, monthly and per-unit rates.
- Checked on the phone on 2026-10-07: add person, mark a day, quantity sheet, Month forms, dues, payment. NOT yet checked on the phone: the weekly rate option and the status bar icon fix.
- Not built yet: reminders, home-screen widget, backup/export, Bangla UI, dark theme, back-dating a payment.

## Gotchas (cross-cutting)
- System JDK 21 is corrupt: build with `JAVA_HOME=~/.gradle/jdks/temurin-21 ./gradlew --offline <task>`.
- Only `material-icons-core` is available; the extended icon set is not in the Gradle cache.
- The ADB phone is shared with other sessions; check the foreground app before taking the screen.
- R8 prints many "error parsing kotlin metadata" warnings (Kotlin 2.4 is newer than AGP 8.13's R8). The release APK still runs.
- `navigation-compose` wants serialization-core 1.7.3, which is not cached; a constraint in `app/build.gradle.kts` pins 1.11.0.
