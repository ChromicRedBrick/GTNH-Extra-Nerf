# GTNH Extra Nerf

GTNH Extra Nerf is a fixed-ruleset difficulty add-on for GregTech: New
Horizons on Minecraft 1.7.10. It reduces power efficiency, increases selected
recipe costs, removes progression shortcuts, and rebalances processing chains
from early steam power to endgame materials.

The mod is built for the pinned full GTNH environment. It is not intended as a
standalone GregTech configuration mod, and it currently has no per-nerf config
switches.

Supported pack release: **GT New Horizons 2.9.0** (see
[compatibility](docs/COMPATIBILITY.md)). Release JARs are published on the
[GitHub releases page](https://github.com/kuba6000/GTNH-Extra-Nerf/releases).

## Highlights

- Lower steam, gas, diesel, plasma and semi-fluid generator efficiency.
- 5:1 steam conversion and 3/4 Large Chemical Reactor overclocking.
- Ten-times shorter Thorium, Uranium and MOX fuel-rod lifetime.
- Reduced Naquadah small-ore generation.
- Restricted early Steel production and reworked MV blast-furnace routes.
- Reworked Aluminium, Ilmenite, Titanium, Silicon, Platinum, Samarium and
  Indium processing.
- More expensive circuits, circuit boards, casings, cables and endgame parts.
- Machines below HV require a physical Integrated Circuit; built-in ghost
  selection starts at HV.
- Removed selected crafting and machine-recipe shortcuts.
- Slime can no longer be extracted or centrifuged, and selected slime-bound
  crafting routes now require Electro-Magic Tools Glue.
- Safety certification: Bronze/Steel steam machines and, from LV upwards, electric
  machines and energy/dynamo hatches stay paused until the save holds the matching
  certificates. They are earned with an in-game exam (Steam, LV) and by payment (MV
  upwards).
- A prototype Industrial Brainrotter multiblock, eight fictional UQV components with a
  UFO Assembler recipe page, and a server-wide limit on ME Controllers from lootbags.
- Assembly Lines only form in AE2 Spatial Storage worlds, and GT machines explode in
  the Toxic Everglades.
- Startup integrity checks that stop production/full-pack launches when an
  expected patch no longer matches, while deobfuscated development launches
  warn and continue.

The current implementation contains 50 mixin classes (6 early and 44 late)
registered through 40 `Mixin` enum entries, and 11 load-complete recipe
modules.

## Telemetry and team tracking

Telemetry is enabled by default outside development environments. The logical server
reports a random ID valid for one server run, online player count, highest available
world progression tier, singleplayer status and an optional randomized JAR proof
about once per hour.
We'd appreciate you keeping telemetry enabled. It is used only for community
statistics about active servers, online players, and world progression.
The owner can disable reporting with
`/extranerftelemetry off` or `telemetry.enabled=false` in
`config/gtnhextranerf.cfg`. See the
[Privacy & Telemetry Policy](https://extranerf.kuba6000.pl/privacy) and
[telemetry details and testing](docs/TELEMETRY.md).

Team tracking is a separate feature that is off until an operator runs
`/extranerfteam on`; see [team tracking](docs/TEAM_TRACKING.md). The mod also checks
for newer releases ([update check](docs/UPDATE_CHECK.md)).

## Documentation

- [Project overview](docs/OVERVIEW.md)
- [Complete gameplay change catalog](docs/NERFS.md)
- [Progression certificates](docs/PROGRESSION.md) and [certification exams](docs/CERTIFICATION_EXAM.md)
- [Fictional component tiers and registry](docs/COMPONENTS.md)
- [Compatibility and supported environment](docs/COMPATIBILITY.md)
- [Building and running](docs/BUILDING.md)
- [Telemetry and opt-out](docs/TELEMETRY.md)
- [Update check](docs/UPDATE_CHECK.md)
- [Opt-in team tracking](docs/TEAM_TRACKING.md)
- [Technical overview](docs/TECHNICAL_OVERVIEW.md)
- [Development guide](docs/DEVELOPMENT.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Agent and maintainer documentation](AI/README.md)

[`docs/NERFS.md`](docs/NERFS.md) is the canonical human-readable list of every
active gameplay change. Historical claims that are not implemented by the
current source are explicitly called out there.

## Supported baseline

- Minecraft 1.7.10
- Forge 10.13.4.1614
- GT5-Unofficial 5.09.54.261
- NewHorizonsCoreMod 2.9.99
- NotEnoughItems 2.8.171-GTNH
- Complete GTNH mod environment required by the referenced recipes and mixins

## Build and test

Clone the repository with its GTNHGradle submodule, then run the Gradle checks:

```bash
git clone --recurse-submodules https://github.com/kuba6000/GTNH-Extra-Nerf.git
cd GTNH-Extra-Nerf
./gradlew check
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat check
.\gradlew.bat build
```

Build artifacts are written to `build/libs`. See
[`docs/BUILDING.md`](docs/BUILDING.md) for full-pack client/server tasks and
toolchain details.

## Integrity model

Recipe patches identify exact upstream recipes and require their expected
cardinality. Typed audit scopes aggregate successful semantic changes from
recipe helpers and audited mixins. In production, a missing, duplicate or
rejected patch produces a fatal `GTNH Extra Nerf integrity check failed`
startup error instead of silently running an incomplete balance ruleset. In a
deobfuscated development environment, the same failures are logged as warnings
and startup continues so an incomplete local runtime remains usable.

## License

GTNH Extra Nerf is licensed under the GNU Lesser General Public License,
version 3 or later. See [`LICENSE`](LICENSE).
