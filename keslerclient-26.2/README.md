# KeslerClient

A client-side Fabric mod for Minecraft 26.2.

## Features

- **Chat:** adjust chat height while chat is open, keep up to 10,000 history entries, copy a message with right-click, and merge consecutive identical messages with a count.
- **Controls:** always sprint and assign extra keyboard keys to attack, use, movement, jump, sneak, and sprint.
- **Custom commands:** create up to 10 command aliases and map each to another command. Arguments typed after an alias are passed to the target command.
- **Dungeons:** optional boss stopwatch with adjustable position and scale; optional F3/M3 Professor helper that walks to a Fire Freeze position and uses the staff when prompted and in range.
- **Tooltips:** scroll long item tooltips and optionally show lore line indices in debug mode.
- **Visuals:** fullbright, hide dying mobs immediately, show container slot indices, shrink first-person held items, slow the attack swing animation, hide explosions, make sneaking instant, make invisible mobs and players easier to see, remove blindness fog, lower the fire overlay, and disable damage camera shake.
- **Secrets:** optional SkyBlock dungeon secret clicker for configured wither and redstone skulls, chests, and levers.
- **Gathering:** optionally harvest flowering azalea blocks (Lushllilac Bushes) and sea pickles with at least three pickles (Sea Lumies).
- **Auto clicker:** configurable average clicks per second and random spread.
- **Commands:** `/kesler` opens the config, and `/k` is a shortcut. `/kesler merge` starts anvil book merging, `/kesler rightclick` toggles held right-click, and `/kesler secrets setwither` or `/kesler secrets setredstone` saves the skull currently being viewed.

## Requirements

- Minecraft 26.2
- Fabric Loader and Fabric API
- Fabric Language Kotlin
- Cloth Config

Mod Menu is supported for opening the config screen.

## Build

Run `./gradlew build`. The mod jar is created in `build/libs/`.
