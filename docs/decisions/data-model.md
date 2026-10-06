# Data model

## Current state
- `Person`: one recurring service. `ServiceKind` is VISIT (comes to the house), OUTING (a household member goes out) or DELIVERY (has a quantity).
- `DayRecord`: at most one per person per date, status CAME, ABSENT or HOLIDAY. No record means not marked.
- `Payment`: money counted towards one month. An advance is a payment made before the month ends.
- A month's charge (`buildMonthAccount`): MONTHLY is the flat rate, WEEKLY is weeks times rate, PER_VISIT (shown as "Daily") is came days times rate, PER_UNIT is total quantity times rate, rounded to whole taka.
- A week runs Saturday to Friday and is charged only if it has a CAME day. A week across two months is charged once, to the month of its first CAME day.
- Money is whole taka (`Int`). Quantities are `Double`.

## Open items
- A make-up lesson is a CAME record on a day that is not scheduled; there is no separate flag for it.

## Gotchas
- A weekly account needs records from `firstDateNeededFor(month)`, up to six days before the month starts.
- A MONTHLY charge is zero for a month with no CAME record, so months before a person was added do not show as unpaid.

## Tried / rejected
- Separate ADVANCE and BONUS payment kinds: dropped, a note on the payment covers both.
