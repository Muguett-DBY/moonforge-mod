# Moonforge

**A Minecraft sandbox for projectile physics, trajectory overlays, and experimental weapons.**

`Minecraft 1.21.11` · `Fabric` · `Java 21` · `Prototype`

## In this repository

- Tick-based projectile simulation and trajectory rendering.
- Impact feedback, target selection, and motion-prediction code.
- A configuration screen and custom weapon/entity experiments.

The project began as a singleplayer training overlay and now contains broader gameplay experiments. It is a development prototype, not a finished release or a multiplayer compatibility guarantee.

## Build and run

Use **JDK 21**. Minecraft and Fabric versions are pinned in `gradle.properties`; the Gradle wrapper is included.

```powershell
.\gradlew.bat runClient
.\gradlew.bat build
```

On macOS/Linux, use `./gradlew` instead. Build output goes to `build/libs/`. These are development builds; use a separate test world.

## Code map

Inside `src/main/java/com/muguett/moonforge/`:

- `physics/` and `prediction/` — projectile simulation and motion estimates.
- `render/` and `gui/` — overlays, feedback, and configuration.
- `item/`, `entity/`, and `network/` — custom weapons and their behavior.

Licensed under [MIT](LICENSE).
