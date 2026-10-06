# Minecart Speed

A Fabric mod that makes minecarts go fast — by default **120 blocks/sec** —
whenever a **powered rail** is placed directly on top of a configurable
**trigger block** (default: redstone block). Curves, slopes, and long
straightaways all work at high speed.

**The trigger block is also a track lock.** Any rail (powered, regular,
detector, activator) placed on top of a trigger block becomes a
"high-speed rail" — minecarts traveling at boost speed will not fly off
the track. Place trigger blocks under every rail in your boost zone,
including the regular rails between powered-rail boosters and the rails
through turns and slopes.

- **Powered rail + trigger block** = accelerates the cart to boost speed.
- **Any other rail + trigger block** = keeps the cart locked to the rail at its current speed (turns, slopes, straights).
- **Rail without a trigger block** = vanilla physics. At boost speed the cart will likely fly off.

## Requirements
- Minecraft (matches the version configured in `gradle.properties`)
- Fabric Loader (`>= 0.19.5`)
- Fabric API
- Java 25

This is a server-side mod — works on dedicated servers, single-player,
and LAN. Clients do **not** need to install it (but it is safe to install
on the client too; it is marked `environment: "*"`).

## Install on a server
1. Drop the built jar (`build/libs/minecart-speed-<version>.jar`) into the
   server's `mods/` folder.
2. Make sure Fabric API is installed alongside it.
3. Start the server once — a default config is created at
   `config/minecart-speed.json`.
4. Edit the config and run `/minecartspeed reload` in-game (op level 2)
   to apply changes without restarting.

## Config

`config/minecart-speed.json`:

```json
{
  "enabled": true,
  "boostSpeed": 120.0,
  "triggerBlocks": ["minecraft:redstone_block"],
  "requirePowered": true,
  "preserveDirection": true,
  "ejectPassengersOnBoost": false
}
```

| Key | Default | Notes |
| --- | --- | --- |
| `enabled` | `true` | Master switch. |
| `boostSpeed` | `120.0` | Target speed in blocks/sec. Internally divided by 20 to get blocks/tick. |
| `triggerBlocks` | `["minecraft:redstone_block"]` | Any number of block IDs. Use the full namespaced ID (`modid:block_id`). |
| `requirePowered` | `true` | If `true`, the powered rail itself must be powered (lit). If `false`, just placing a powered rail on the trigger block is enough. |
| `preserveDirection` | `true` | (reserved) Keeps cart moving the direction it was already moving. |
| `ejectPassengersOnBoost` | `false` | Boots any rider out the moment the boost engages — useful to stop launch-pad death. |

## Commands

- `/minecartspeed reload` — reloads `minecart-speed.json` without a restart. Requires op level 2.

## Build

From the project root:

```sh
./gradlew build
```

The output jar lives at `build/libs/minecart-speed-<version>.jar`.

## Caveats — read this

120 blocks/sec is about **15× vanilla minecart speed**. The mod handles
this by **substepping**: each game tick, the cart is advanced through
vanilla's rail logic many times in succession (one vanilla-tick-sized
hop per substep). Curves and slopes follow correctly because each hop
is within vanilla's physics tolerance. But:

- **Chunk-skipping**: the cart can still outrun chunk loading on freshly
  visited tracks. Pre-generate the route, or bump the server's
  `view-distance` / `simulation-distance`.
- **Gaps in the trigger track**: a single rail without a trigger block
  underneath, in the middle of a boost zone, can launch the cart at
  120 b/s. Lay redstone (or your chosen trigger block) under EVERY rail
  in the boosted section — including curves and slope tiles.
- **Passenger ejection**: vanilla physics may yeet riders at the first
  bump. Set `ejectPassengersOnBoost: true` to boot riders before the
  boost engages.

If you want a smoother experience, lower `boostSpeed` to something like
`40.0` or `60.0` — still much faster than vanilla, but with even fewer
artifacts.

## License

CC0-1.0 — see `LICENSE`.
