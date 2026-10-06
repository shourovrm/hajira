# Hazira

Hazira (হাজিরা, "attendance"). The working directory is still named `hujur-tracker`; that name is retired.

Household service tracker for a Bangladeshi home: records the outside people who serve the household on a recurring basis — a child's religious teacher, home tutor, coaching class or art school, the daily maid, the milk delivery — so the family can see who came, what was delivered, and what is owed.

Android app (Kotlin, Compose, Material 3, Room, single Activity). Sideloaded APK, no backend, no network permission. Screen designs: `docs/mockups/hazira-directions.html`.

## Token economy
- If `caveman` plugin/skill available: keep active (full). Terse output always.
- If `ponytail` plugin/skill available: keep active (full). Laziest working solution.
- Fallback (no plugins): terse replies, no filler; YAGNI, stdlib/native before dependencies, shortest working diff.

## Memory — MANDATORY
`DECISIONS.md` is a small index; the knowledge lives in `docs/decisions/`.
- **`DECISIONS.md`** (keep under ~50 lines): **Topics** (one line per topic file), **Next** (in-flight handoff, 3-5 bullets max, prune ruthlessly), **Gotchas (cross-cutting)** (only what every session needs, one line each).
- **`docs/decisions/<topic>.md`**, one per area of the project, each with:
  - **Current state** — living snapshot, edit in place.
  - **Open items** — unfinished or unverified work for this topic.
  - **Gotchas** — living quirks (flaky tests, env vars, wrong docs), one line each.
  - **Tried / rejected** — one line: what + why dead; never re-attempt anything listed.
- **`docs/decisions/LOG.md`** — append-only: `YYYY-MM-DD | decision | why`.

Before any work: read `DECISIONS.md`, then only the topic files the task touches. Never read `LOG.md` whole; grep it.
Before trying a new approach: `grep -ri <keyword> docs/decisions/` — a rejected attempt may sit in a topic you did not open.
Update in the same commit as the change it describes. Terse. New topic = new file + one index line. If missing, create the index and `docs/decisions/LOG.md`.
Split of memory: CLAUDE.md = rules, `DECISIONS.md` + `docs/decisions/` = knowledge, git history = events. Don't duplicate across them.

## Commits
- Every feature/code change = one terse commit immediately. Conventional type prefix (feat/fix/docs/chore), subject ≤50 chars.
- AI co-author trailers (Co-Authored-By) are allowed in this repo.
- `DECISIONS.md`, `docs/decisions/` and planning docs: committed — they are the project's working memory and this repo is the only copy.

## Style
- KISS, UNIX philosophy: one file/module = one job, keep files small. One idea per function; if it needs "and" to describe, split it.
- Write for a human reading this in six months, not for brevity. Name things fully: `user_count`, not `uc`. No invented abbreviations.
- Prefer an obvious ten lines over a clever three. No nested ternaries, no chained one-liners, no comprehension doing three jobs.
- Comments explain WHY, especially where the obvious approach is wrong; skip comments that restate the code. Never leave a comment describing behaviour the code does not have — a lying comment is worse than none. Same for UI copy: a setting must say what it actually does.
- Look for an existing helper, util or pattern in this repo before writing a new one. No abstraction until there are two real callers.
- Standard library and well-known algorithms before hand-rolling. Pick the right complexity; optimise beyond that only when a measurement says to.
- Match the surrounding file's naming and idiom where it differs from the above.
- Check current library docs before using an API (Context7 MCP if available). Don't trust memory for API shapes; verify a version before relying on a feature.
- When debugging, find the root cause before changing code. When the logic is non-trivial, write the test first.

## Subagents
- Do not spawn subagents on your own initiative. If a task looks like it would benefit from them, ask the user first and wait for a yes.
- A subagent must never spawn another subagent. Say so in every subagent brief.
- When the user does approve subagents: give each one disjoint files, and keep Gradle out of their hands — two builds in one project collide, and a stale output yields a false green. One clean build at integration, run by the main session.
- A subagent's report is a claim, not a fact. Verify anything load-bearing yourself, and be especially sceptical of a report that attributes an observation to the user — check the actual conversation before acting on it.

## Build
- Android app, Kotlin + Compose, single `:app` module. Versions in `gradle/libs.versions.toml` are pinned to what `~/repos/fyi-player` uses so the shared `~/.gradle` cache already holds every artifact. This laptop is short on disk: do not install another Gradle, JDK, SDK platform or build-tools version, and check the cache before bumping any version.
- System JDK 21 is corrupt on this machine. Build with `JAVA_HOME=~/.gradle/jdks/temurin-21 ./gradlew --offline <task>`; `--offline` proves nothing new is being downloaded.
- Release builds only, never debug: `assembleRelease` for the APK, `testReleaseUnitTest` for unit tests. Signing comes from the gitignored `keystore.properties` and `hazira-release.jks` in the repo root.

## Verification
Nothing is "done" until exercised for real: run the test suite AND drive the changed flow end-to-end in the running app on the ADB-connected phone. Other sessions share that phone; check what is in the foreground before taking the screen. Report failures verbatim. Clean up test data. Never claim a fix works because it compiles.

For anything visual: no hosted artifacts. Write the file locally or start the dev server, verify it through Playwright MCP (headless Helium — navigate, then screenshot/snapshot), and when the user should see it too, also open it with `helium-browser <file-or-url>`. Never run `playwright install`.

## Secrets & confidentiality
- Credentials, keys, tokens, personal data: gitignored files only, chmod 600, never printed to output/transcripts, never in commit history.
- Records about named people are personal data: no real names, phone numbers or addresses in fixtures, logs, screenshots or bug reports.
- Before any public push: audit HEAD *and history* for leaks.
