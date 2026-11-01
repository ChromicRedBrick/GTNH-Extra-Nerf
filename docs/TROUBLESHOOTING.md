# Troubleshooting

## `GTNH Extra Nerf integrity check failed`

This message means the mod could not prove that its complete balance ruleset
was applied. The following lines list typed `AuditScope` names, their expected
recipe counts and the observed added, modified and removed counts. A direct
recipe-patching exception also names the active scope, operation and recipe
map.

In a production or full-pack launch this blocks startup. The
client stays on a Forge error screen; a dedicated server follows the fatal
FML exception path. In a deobfuscated development environment, known recipe
or registry validation failures are logged as warnings and later startup
checks still run. This includes unavailable certification fee materials in
the reduced runtime. Their audit status remains FAILED; programmer errors
and backend write/rollback failures are not tolerated. Treat the warning
as an incomplete local ruleset, not as compatibility confirmation.

Common causes:

- GT5-Unofficial or NewHorizonsCoreMod does not match the pinned version;
- a required full-pack mod is missing;
- another mod already removed or replaced the expected recipe;
- a mixin target method changed;
- duplicate registration caused a patch to match more than once;
- a replacement recipe collided with an existing recipe.

Do not delete the integrity check or lower the expected count as a first fix.
Compare the target recipe or method with the supported dependency snapshot.

## Useful files

For a local client/server, inspect:

- `logs/latest.log`;
- the relevant FML log;
- `crash-reports/`;
- the complete `AuditManager` integrity message and exception stack trace.

The CI full-pack job uploads:

- `console.log`;
- `latest.log` when available;
- all generated crash reports.

## NEI recipe loading fails inside `getNonUnifiedStacks`

An `ArrayIndexOutOfBoundsException` in Fastutil, called by
`GTOreDictUnificator.getNonUnifiedStacks` on an NEI worker thread, can come from
GT's lazy unification cache. One worker fills the map while another sees that
the map is no longer empty and reads it during resizing. The recipe category
named in the error (for example Forge Hammer) is where lookup failed.

Extra Nerf serializes the complete alternative lookup so readers cannot observe
a partially built map. It preserves GT's alternative items and stack amounts.
The hook is required and checked by the material audit; native tests use concurrent
lookups after registering aliases through GT's public API. Restart the client
after installing this fix, including when using `runClient25` with hot swapping.

## Gradle daemon disappears on Windows

The root build requests an Adoptium Java 25 daemon. Verify that the configured
JDK can start, that antivirus/sandbox software is not terminating child JVMs,
and retry with:

```powershell
.\gradlew.bat test --no-daemon --stacktrace --info
```

The mod itself still targets Java 8-compatible bytecode.

## Server never reaches `Done (`

Check whether:

- the Minecraft EULA was accepted for a local run;
- the full-pack download completed;
- the manifest/runtime path exists under `build/fullpack`;
- an `AuditManager` integrity check or mixin application failed before normal
  server readiness;
- the configured timeout is sufficient for the machine.

## A documented recipe is still available

First confirm it is exactly the recipe described in [`NERFS.md`](NERFS.md).
Many machines have parallel single-block/multiblock, fluid-cell, gas or
optional-mod variants. If the supposedly removed exact route still exists in
the supported pack, preserve logs and the NEI recipe details as a bug report.
