# Lootbag implementation status

Branch: `codex/lootbag-system`. Updated 2026-10-04.
[Current architecture](LOOTBAG_SYSTEM_DESIGN.md).

## Implemented native architecture

- Post-deserialization patch of actual ELB tables on both physical sides.
- Typed startup audit: exact controller mutation count and published-table verification.
- Separate audit session for every successful full-file reload, using the same scopes.
- Recomputed native weight totals; ELB clears and rebuilds merged/Fortune caches.
- Original ELB weighted selection, native limits, stack generation and delivery.
- Hidden global controller allowance; durable claim before native spawn.
- Existing v1 global journal retained, including previous player-count history.
- Native YAMCore DB handle and global runtime reset between server/save sessions.
- Native NEI's table data contains one controller without exposing the global cap.
- Audited reload command; live add/edit commands cannot bypass the load pipeline.

## Removed prototype components

The separate XML reader, catalog, policy compiler, random planner, reward editor,
opening service, fingerprint builder and standalone native-count importer were
deleted. Tests of that removed engine were replaced with tests of the native-table
patch and hidden allowance boundary. Value objects needed to read v1 claims remain;
`LootPlan` is now only a proposed ledger transition, not a random selection plan.

The original design commit `ea76683` and Git history preserve the larger proposal.
Standalone first-opening guarantees and independent percentage bonuses are not
wired into this simpler adapter. The requested real gameplay rule is a random
ME Controller reward with a one-unit global cap.

## Verification

- Native table tests: exact controller sources, unchanged fields/weights, no partial
  patch on missing targets, native sum-weight updates, post-publication tampering.
- Global allowance tests: shared players/bags/Fortune, reopen, failed/pending delivery,
  read-only availability, independent worlds, off-thread and closed-ledger rejection.
- Journal tests: corruption/truncation, missing files, exclusive ownership and a
  binary v1 fixture written by the previous engine (no reset of existing counters).
- Opt-in full-pack fixture uses the real ELB right-click with deterministic native
  RNG. It verifies one controller, subsequent rejection for another player/bag,
  ledger reopen, an unrelated reward still dropping, unchanged XML, audited reload,
  rebuilt pre-warmed caches and controller entries remaining visible after the cap.
- Final ordinary `spotlessApply check build`: 62 Java tests, no failures/errors/skips.
  The reduced count reflects removal of the duplicate engine and its obsolete tests.
  The opt-in fixture also passed compilation/Checkstyle; three Python harness tests passed.
- Final full-pack dedicated-server run passed with exit code 0 and clean shutdown.
  The startup audit accepted all scopes; both reload audits observed exactly one
  modification and no additions/removals. Native opening/cache/persistence fixture
  passed. An intermediate fixture failure exposed the separate World.rand budget
  RNG; the final fixture controls that RNG as well as ELB's item RNG.
- Inspected the ordinary JAR: native editor present, removed planner absent,
  integration fixture absent. The old journal fixture still retains its spent cap.

## Remaining verification and limitations

A graphical client session and switching between integrated-server worlds have not
been exercised. Native table data read by NEI is verified, but this is not a claim
of a visual client test. Arbitrary server-only XML drop edits are not synchronized
by ELB; use matching client/server pack configs. No hidden limit state is synchronized.

The ledger is still append-only and rejects damaged tails without automatic repair.
Only limited rewards append new claims now. Physical disk/force failure injection
and an administrator recovery interface are outside this refactor.
