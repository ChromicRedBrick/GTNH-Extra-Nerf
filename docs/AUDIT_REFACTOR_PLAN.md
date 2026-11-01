# Audit refactor plan

Branch: `codex/refactor-audit-system`, based on merged PR #11.

## Contracts to preserve

- Existing AuditScope identities, optional-mod activation and count expectations.
- Modifier transactions record counts only after commit; audit is not a rollback engine.
- Registration-time mixins retain their upstream behavior and return values.
- Nested scopes attribute counts locally and measure exclusive elapsed time.
- Production integrity failures stop startup; development diagnostics remain explicit.

## Implementation steps

1. Introduce an independently owned AuditSession and non-reusable AuditHandle.
   Enforce thread ownership and LIFO closure. Closing without an explicit outcome
   records failure. Callback helpers record success only after normal return.
2. Model immutable counts, execution status and results. Retain failure/skip
   reasons and a final immutable report after the session is closed.
3. Replace duplicated expectation text/predicates with AuditExpectation objects.
   AuditScope becomes a definition, independent of global statistics. Preserve
   count contracts, including dynamic optional-mod variants and equal-count rules.
4. Keep AuditManager only as a narrow bridge for mixins. Move development failure
   policy to startup/recipe integration. Migrate all captures to outcome-aware calls.
5. Finalize once after load-complete. Report all operation counts and non-success
   outcomes. A rejected finish must leave the session usable; cleanup must not
   replace an original startup exception.
6. Verify public API tests, existing transaction tests, formatting/build, Python
   harness and the production JAR in the isolated full pack. Update documentation,
   commit and publish a separate PR without merging it.

## Agreed test boundaries

Public session, handle, expectation and report APIs: outcomes, failures, skipped
work, nested time, stale handles, thread/LIFO constraints, finish lifecycle and
immutable reports. No reflection, source assertions or balance-value snapshots.

## Review focus

No pooling of public handles; no global state in definitions; no silent success
for failed callbacks; no development suppression of arbitrary programmer errors;
no untyped ingredient-style model or reflection-based test resets.

## Completed verification

All six implementation steps are complete. The final build passed 39 Java
and 3 Python tests. The production full pack passed all 64 audit definitions
with 3006 modified, 36 added and 120 removed observations, reached Done and
shut down with exit code 0. No client/dev launch or performance benchmark is
claimed. See `AUDIT_SYSTEM.md` for the implemented contracts and limitations.
