# Minecart Speed

A Fabric mod that makes minecarts faster when a powered rail sits on a trigger block. The default speed is 120 blocks per second. The default trigger block is a redstone block.

The boost runs on the server. Clients do not need the mod installed. It is still safe to put it on a client (the mod metadata uses `environment: "*"`). The client entrypoint does not add any behavior.

## How a track works

Put a trigger block under the rails you want this mod to control. The block directly under the rail is what counts.

- Powered rail on a trigger block: speeds a moving cart up toward `boostSpeed`. With `requirePowered` left on (the default), that powered rail also has to be receiving redstone. Turn `requirePowered` off and an unpowered powered rail on a trigger block is enough.
- Any other rail on a trigger block: if the cart is already faster than vanilla (0.4 blocks per tick), the mod keeps it on that rail at its current speed. Slower carts are left to vanilla.
- Rail with no trigger block under it: vanilla physics. The mod stays out of the way.

A cart that is standing still is not launched by this mod. It has to already be moving (a normal powered rail nudge is enough). After that, speed ramps from vanilla's cap up to the boost speed over `accelerationBlocks` (default 3 blocks).

Each tick, movement faster than vanilla's 0.4 blocks is split into smaller steps that use Minecraft's own rail movement. Curves and slopes follow that rail logic. If `keepOnRails` is on (the default), a cart the mod just handled can be put back on a nearby rail when it leaves the track. That snap lasts 20 ticks after the mod was controlling the cart, and it does not pull unrelated carts back onto rails.

## Requirements

This branch targets Minecraft 26.3.

- Minecraft `~26.3`
- Fabric Loader `>= 0.19.5`
- Fabric API
- Java 25 or newer

## Install

1. Put `minecart-speed-<version>.jar` in the server `mods/` folder, next to Fabric API. A build from this project lands at `build/libs/minecart-speed-<version>.jar`.
2. Start the server once. If `config/minecart-speed.json` is missing, the mod writes a default file there.
3. Edit the config, then run `/minecartspeed reload` to apply it without a restart. That command needs op level 2 (gamemaster).

## Config

`config/minecart-speed.json`:

```json
{
  "enabled": true,
  "boostSpeed": 120.0,
  "triggerBlocks": ["minecraft:redstone_block"],
  "requirePowered": true,
  "preserveDirection": true,
  "ejectPassengersOnBoost": false,
  "accelerationBlocks": 3.0,
  "keepOnRails": true
}
```

| Key | Default | What it does |
| --- | --- | --- |
| `enabled` | `true` | Master switch. |
| `boostSpeed` | `120.0` | Target speed in blocks per second. The mod divides this by 20 to get blocks per tick. |
| `triggerBlocks` | `["minecraft:redstone_block"]` | Block ids under the rail. Use the full id (`namespace:block`). Invalid ids are skipped. |
| `requirePowered` | `true` | When true, a powered rail must be powered to boost. When false, the powered rail block is enough. |
| `preserveDirection` | `true` | Written to the config file. The mod does not read this field. |
| `ejectPassengersOnBoost` | `false` | When true, riders are removed when a boost starts. |
| `accelerationBlocks` | `3.0` | Distance (in blocks) used to ramp from vanilla speed up to `boostSpeed`. `0` skips the ramp. |
| `keepOnRails` | `true` | When true, a cart the mod just handled can be placed back on a nearby rail if it leaves the track. |

## Commands

- `/minecartspeed reload` reloads `config/minecart-speed.json`. Requires op level 2.

## Build

From the project root:

```sh
./gradlew build
```

The jar is written to `build/libs/minecart-speed-<version>.jar`.

## Limits

- Default boost is 120 blocks per second. Vanilla's cap in this mod is 0.4 blocks per tick (8 blocks per second). A cart can cover a lot of ground in one tick, so it can run into chunks that are not loaded yet. Pre-generate the route, or raise the server view distance and simulation distance.
- A gap with no trigger block drops the cart back to vanilla physics while it is still fast, so it can leave the rail. Put a trigger block under every rail in the fast section, including curves and slopes.
- `keepOnRails` only helps for a short time after the mod was controlling that cart. It is not a lock for the whole world.

## License

CC0 1.0 (public domain). See `LICENSE`.
