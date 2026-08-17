---
name: run-game-client
description: Runs the Minecraft game client locally with the mod for manual testing. Use when asked to launch, run, or start the game client for testing.
---

# Run Game Client

Runs the Minecraft client with the Too Many Chests mod for manual testing.

## Quick Start

Ensure Java 21 is active (`jenv local 21` or `sdk use java 21-amzn`), then:

```bash
./gradlew runClient
```

JEI and EMI stay out of the dev client by default so their overlays do not cover the screens
screenshots capture. To exercise the recipe viewer integration, launch with them loaded:

```bash
./gradlew runClient -Precipe_viewers=true
```

## Testing Commands

- `/gamemode creative` - access spawn eggs
- `/summon too-many-chests:<entity_id>` - spawn entity directly (see `ModEntities.java` for IDs)
- `/locate biome minecraft:<biome>` - find biomes (see `ModSpawns.java` for spawn locations)

## Post-Test Protocol

After running the game client for manual testing, always ask for human input before continuing with next steps.

## Hot Reload

Press **F3+T** to reload textures/models without restarting.
