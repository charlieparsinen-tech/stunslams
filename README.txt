Stun Slam Auto Axe  (Fabric, Minecraft 1.21.11, client-side only)

What it does
  Hit a player who is blocking with a shield (and facing you) -> the mod selects the first
  axe in your hotbar for that hit, then goes back to the slot you were on ~2 ticks later
  (your mace), so you can follow up with the smash.
  If you have no axe in the hotbar, or the target isn't blocking, nothing changes.

Build
  1. Install JDK 21.
  2. Open this folder in IntelliJ IDEA (it will import Gradle), or run:
        gradle wrapper
        ./gradlew build          (Windows: gradlew.bat build)
  3. The jar is in build/libs/ (use the one WITHOUT "-sources").
  4. Put it in your mods folder with Fabric Loader 0.18+ and Fabric API for 1.21.11.

Tweaks
  SWAP_BACK_TICKS in StunSlamClient.java controls how long the axe stays out.

Note: many servers ban automatic hotbar-swapping in combat. Check your server's rules.
