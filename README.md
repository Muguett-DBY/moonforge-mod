# Moonforge Mod

Moonforge is a client-side Minecraft training and accessibility mod for learning projectile physics in singleplayer. It is intentionally scoped as a visualization tool, not a cheat mod.

## Chosen Stack

The best supported modern choice for this project is:

- Minecraft `1.21.11`
- Fabric Loader
- Fabric API
- Fabric Loom
- Java `21`

Why this stack:

- Fabric still has the lightest iteration loop for client-only HUD and world rendering tools.
- Fabric's rendering callbacks are a good fit for practice overlays and trajectory visualization.
- Current Fabric Maven artifacts are available for `1.21.11`, so we can target a modern stable line instead of an older maintenance version.

Official references used for this choice:

- Fabric docs version selector for `1.21.11`: https://docs.fabricmc.net/develop/
- Fabric API Maven index for `0.141.1+1.21.11`: https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.141.1%2B1.21.11/
- Yarn docs for `1.21.11+build.3`: https://maven.fabricmc.net/docs/yarn-1.21.11%2Bbuild.3/

## Safety Scope

- Singleplayer practice and accessibility only.
- No silent aim.
- No packet manipulation.
- No multiplayer bypass.
- No hidden auto-lock or automatic firing.
- Assistive overlays are disabled outside singleplayer when the safeguard is enabled.

## Project Structure

```text
src/main/java/com/muguett/moonforge/
  MoonforgeMod.java
  MoonforgeModClient.java
  config/
    MoonforgeConfig.java
    MoonforgeConfigManager.java
  physics/
    ProjectilePhysicsProfile.java
    ProjectileWeaponType.java
    TrajectoryPoint.java
    TrajectoryResult.java
    TrajectorySimulator.java
  render/
    TrajectoryRenderer.java
    WorldSafetyOverlay.java
```

Planned next modules:

- `targeting/` for singleplayer mob selection
- `prediction/` for future-position estimation based on velocity
- `gui/` for a proper config screen

## Step 1: Physics + Trajectory Rendering

This first implementation phase includes:

- bow, crossbow, and trident support
- predicted trajectory arc rendering
- predicted impact point highlighting
- per-weapon enable toggles in config
- a singleplayer-only safeguard banner

## Physics Notes

The simulator uses the same basic discrete projectile idea as Minecraft:

1. Position update

   `p_next = p_current + v_current`

2. Drag update

   `v_dragged = v_current * drag`

3. Gravity update

   `v_next = v_dragged + (0, -gravity, 0)`

Because Minecraft projectiles are updated per tick, the simulator steps once per tick and raycasts between consecutive points to find collisions.

For a future target prediction step in the next phase, we will use:

- time-to-target estimate:
  `t ~= distance / projectile_speed`
- future target position:
  `future_pos = current_pos + target_velocity * t * prediction_multiplier`

These formulas are intentionally kept visible in code comments so the mod stays educational.

## Running

1. Install JDK 21.
2. Generate the Gradle wrapper:

   ```powershell
   gradle wrapper
   ```

3. Run the client:

   ```powershell
   .\gradlew runClient
   ```

## Notes

- This repository was prepared before Java was available on the machine, so the Gradle wrapper still needs to be generated locally.
- The current phase focuses on clean projectile physics and rendering only. The config screen and target-indicator guidance will come in the next step.