# Screens

## Current state
- Four tabs: Today (home), People, Month, Dues. Mockups in `docs/mockups/hazira-directions.html`.
- Today: the people due today, one tap to mark. Deliveries open a quantity sheet.
- A person can be marked on any day, scheduled or not. Today lists the others under "Not expected today" with a Mark action; the calendar day panel does the same; every past register cell is tappable. The schedule only decides who is listed first and what counts as unmarked.
- Month: the whole household for one month in three forms: Calendar (dots per day), Strips (one row per person) and Register (editable grid).
- Dues: what is owed for a month, with a statement per person that can be shared.
- Navigation shell and routes: `ui/HaziraNavigation.kt`. Detail screens hide the bottom bar.

## Open items

## Gotchas
- The shell consumes the window insets; a screen's own `Scaffold` must not pad for the navigation bar again.

## Tried / rejected
