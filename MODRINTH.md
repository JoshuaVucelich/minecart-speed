# Minecart Speed

Make minecarts go **really** fast — and stay on the tracks.

By default, any **powered rail** placed on top of a **redstone block**
becomes a high-speed booster that accelerates minecarts to **120
blocks/second**. Curves, slopes, and long straightaways all work — and
the cart stays glued to the track for as long as the track is built on
top of redstone blocks.

## How it works

The trigger block — redstone block by default, but configurable —
serves two jobs:

- **Acceleration.** A *powered rail* sitting on a trigger block boosts
  any minecart that touches it to the configured speed.
- **Track lock.** *Any rail* (regular, powered, detector, activator)
  sitting on a trigger block becomes a "high-speed rail" — minecarts
  travelling at boost speed will not fly off through corners, slopes,
  or junctions.

Lay your trigger blocks under every rail in the boosted section
(curves and slope tiles included). Anywhere your tracks **lack** a
trigger block underneath, vanilla physics takes over — and at 120
blocks/second a cart will launch off the rail.

Disable the mod or yank the redstone, and minecarts go back to being
their slow, sad selves.

## Features

- **Configurable speed.** Default 120 b/s, but set it to whatever you
  want — 40 b/s for a tame "express line", 200 b/s for chaos.
- **Configurable trigger blocks.** Want diamond block to be the
  trigger? Add it to the list.
- **Powered rail required for acceleration**, but you can also turn
  that off so any powered rail with a trigger block boosts (even
  without redstone signal).
- **Eject passengers on boost.** Optional — booting riders out before
  the boost engages stops them getting yeeted into the void at warp 9.
- **In-game reload.** `/minecartspeed reload` reloads the config
  without a server restart (requires op level 2+).
- **Server-side mod.** Clients don't need to install anything. Drop
  the jar in your server's `mods/` folder and you're done.

## Setup

1. Install **Fabric Loader** and **Fabric API** on your server.
2. Drop `minecart-speed-X.Y.Z.jar` into the server's `mods/` folder.
3. Start the server once. A default config is created at
   `config/minecart-speed.json`.
4. Build a track: redstone block, powered rail on top. Place more
   redstone blocks under the curves and straights so the cart stays
   on track at speed.
5. Push a minecart onto the booster, then hold on.

## Configuration

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

| Key | Default | What it does |
| --- | --- | --- |
| `enabled` | `true` | Master switch. |
| `boostSpeed` | `120.0` | Target speed in blocks/sec. Tune to taste. |
| `triggerBlocks` | `["minecraft:redstone_block"]` | List as many as you want. Use full namespaced IDs. |
| `requirePowered` | `true` | Whether the powered rail itself must be receiving a redstone signal to actually boost. |
| `ejectPassengersOnBoost` | `false` | Boot riders out before boost — stops mid-track death. |

After editing the config, run `/minecartspeed reload` in-game (op
level 2+) to apply the new values without restarting.

## Caveats

- 120 b/s is about **15× vanilla minecart speed**. Pre-generate the
  route or bump `view-distance` / `simulation-distance` so the cart
  doesn't outrun chunk loading.
- A single missing trigger block in the middle of a boost zone will
  launch the cart. Build carefully.
- Players riding boosted carts may get tossed off by vanilla physics
  on bumps — `ejectPassengersOnBoost` is a safer (if less heroic)
  default.

## Requirements

- Minecraft (matches the version in the jar)
- Fabric Loader 0.19.5+
- Fabric API
- Java 25

Server-side only. Safe to also install on clients.

## License

CC0-1.0 — do what you want with it.
