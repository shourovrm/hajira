# Hazira (হাজিরা)

An Android app for keeping track of the people who serve a household on a regular basis: a Quran teacher, a home tutor, a coaching class, an art school, the house help, the milk delivery.

"Hazira" means attendance. The app replaces the paper register many households keep for this.

## What it does

- Lists who is expected today. One tap marks each person as came or absent.
- Records a quantity for deliveries, such as litres of milk, which can differ from day to day.
- Shows a whole month for the whole household in three forms:
  - a calendar with one dot per person per day
  - one strip of days per person
  - a register grid where any past day can be corrected
- Works out what is owed for the month from the marked days.
- Records payments and advances against a month.
- Produces a plain-text statement for one person that can be sent through any app.

## How a person is paid

Each person has one of these rates:

- **Daily**: an amount for each day marked as came.
- **Weekly**: an amount for each week with at least one day marked as came. A week runs Saturday to Friday.
- **Monthly**: one amount for the month.
- **Per unit**: an amount for each unit delivered. Deliveries only.

## Privacy

- Everything is stored on the phone.
- The app requests no permissions and has no network access.
- Android backup is turned off for the app, because the data is names, phone numbers and payments of real people.

## Building

Requirements: JDK 17 or newer, and the Android SDK with platform 36.

1. Put the SDK path in `local.properties`:

   ```
   sdk.dir=/path/to/android-sdk
   ```

2. Create a signing key and a `keystore.properties` file in the repository root:

   ```
   storeFile=hazira-release.jks
   storePassword=...
   keyAlias=hazira
   keyPassword=...
   ```

   Both files are in `.gitignore`. Keep a copy of the key: an APK signed with a different key cannot update an installed copy.

3. Build and test:

   ```
   ./gradlew testReleaseUnitTest assembleRelease
   ```

The APK is written to `app/build/outputs/apk/release/app-release.apk`. Only the release build is used, so the APK that is tested is the one that is installed.

## Layout of the code

- `domain/`: the data types, the repository interface, and the month calculation. No Android code.
- `data/`: the Room database behind the repository interface.
- `ui/today`, `ui/month`, `ui/people`, `ui/dues`: one package per tab.
- `docs/mockups/hazira-directions.html`: the screen designs the app was built from.
- `DECISIONS.md` and `docs/decisions/`: why things are the way they are.

## Not built yet

- Reminders at the expected time, and a home-screen widget
- Backup and export
- A Bangla interface
- A dark theme

## Requirements

Android 8.0 (API 26) or newer.
