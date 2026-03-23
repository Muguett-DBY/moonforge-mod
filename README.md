# Moonforge Mod

A starter repository for a Minecraft Fabric mod targeting `1.21.1`.

## Stack

- Minecraft `1.21.1`
- Fabric Loader `0.16.9`
- Fabric API `0.102.1+1.21.1`
- Java `21`

## Project Structure

```text
src/main/java/com/muguett/moonforge/
src/main/resources/
```

## Next Steps

1. Install JDK 21.
2. Generate the Gradle wrapper:

   ```powershell
   gradle wrapper
   ```

3. Run the dev client:

   ```powershell
   .\gradlew runClient
   ```

## Notes

- This repository was bootstrapped without a local Java/Gradle runtime, so the Gradle wrapper is not included yet.
- Update dependency versions in `gradle.properties` if Fabric publishes newer recommended versions for `1.21.1`.
