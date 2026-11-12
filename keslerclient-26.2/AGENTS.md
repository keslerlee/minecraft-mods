# KeslerClient

Client-only Fabric mod for Minecraft 26.2, written in Kotlin (mixins in Java). A personal all-in-one mod: each feature is independent and configurable in game. Mod ID `keslerclient`, package `kesler.keslerclient`.

## Toolchain
- Minecraft 26.2, Fabric Loader 0.19.3+, Java 25
- Loom 1.17 (`net.fabricmc.fabric-loom`, non-remapping), Gradle 9.5.1
- Unobfuscated game: use Mojang names directly, no mappings
- Dependencies (see `build.gradle` and `gradle.properties` for exact versions): Fabric API, Fabric Language Kotlin, Cloth Config (`me.shedaniel.cloth:cloth-config-fabric:26.2.155`), Mod Menu 20.0.3 (optional at runtime)

## Build and test
- Test: Gradle > fabric > `runClient`
- Jar: Gradle > build > `build`, take `build/libs/keslerclient-<version>.jar` (not `-sources`)
- Players need Fabric API, Fabric Language Kotlin, and Cloth Config in the mods folder

## Layout
```
src/main/kotlin/kesler/keslerclient/
├── KeslerClient.kt           entrypoint; inits features, opens config screen on /kesler
├── KeslerModMenu.kt          Mod Menu integration
├── command/KeslerCommand.kt
├── config/ConfigManager.kt   Config class (all fields) + JSON load/save (config/keslerclient.json)
├── gui/                      ConfigSection interface, Categories list, Cloth Config screen builder
└── feature/<name>/           one package per feature (chat, commands, secrets, gathering, dungeonstopwatch, firefreeze, keybindings, autoclicker, tooltip)
src/main/java/kesler/keslerclient/mixin/   all mixins (must be Java), registered in keslerclient.mixins.json
```

## Adding a feature
1. Add its fields (with defaults) to `Config` in `ConfigManager.kt`
2. Create `feature/<name>/` with the logic and a `<Name>Section : ConfigSection` (one tab in the config screen)
3. Add the section to `Categories.all`
4. Call the feature's `init()` from `KeslerClient.onInitializeClient()`
5. Any mixin goes in the Java mixin package and in `keslerclient.mixins.json`
6. Update this file (Layout, Features, Status)

## Features
- **Chat** (`feature/chat`): optional custom chat height while the chat screen is open, extended history, right-click-to-copy, and merging of immediately consecutive identical messages with a repeat count. Extended history raises Minecraft's displayed-line and raw-message caps from 100 to 10,000 when enabled; the toggles are off by default.
- **Controls** (`feature/keybindings`): optional always-sprint behavior plus configurable extra keyboard bindings for attack, use, movement, jump, sneak, and sprint that share the corresponding vanilla action.
- **Custom command bindings** (`feature/chat`): the Commands config tab provides separate alias and target fields; matching aliases are replaced with the target command, with any typed arguments appended.
- **Secret clicker** (`feature/secrets`): Hypixel Skyblock dungeons. Detects a dungeon by "Dungeon: Catacombs" in the tab list (checked each second for 30s after joining a world), then right-clicks the looked-at lever, chest, or player head whose UUID matches the saved wither or redstone UUID. Clicked block positions are kept in a `HashSet<Long>` and cleared on every world join. UUIDs are set only via commands: `/kesler secrets setwither` and `/kesler secrets setredstone` while looking at the head.
- **Auto gatherer** (`feature/gathering`): separate off-by-default toggles right-click flowering azalea blocks (claimable Lushllilac Bushes) and mine Sea Lumies (sea pickles with 3 or more pickles) while targeted. Retry cooldowns are five ticks; pickle swings repeat every six ticks.
- **Dungeon stopwatch** (`feature/dungeonstopwatch`): off by default; position- and scale-configurable stopwatch shows elapsed time to hundredths, starts on a recognized floor-boss opening line during a Catacombs run (rather than generic `[BOSS]` messages that may occur in the Blood Room), and clears on leaving the run or disconnecting.
- **Fire Freeze automation** (`feature/firefreeze`): optional F3/M3 helper selects one nearest Professor stand on the preceding phase dialogue, steers toward that fixed stand, jumps on horizontal collision while grounded, and uses a hotbar staff on the follow-up line only while within one block of a stand.
- **Auto clicker** (`feature/autoclicker`): while attack is held, bumps `KeyMapping.clickCount` via mixin accessor at a random CPS in [avg - spread, avg + spread], re-rolled every click. Also zeroes `Minecraft.missTime` each tick while active.
- **Scrollable tooltips** (`feature/tooltip`): optional toggle. Long item tooltips can be scrolled with the mouse wheel inside a full-height panel; scrolling clamps at the first and last rows.
- **Visuals** (`feature/visuals`): optional fullbright (lightmap gamma factor 1,000), hide dying living entities immediately, a container debug overlay with slot indices and tooltip lore-line indices, smaller first-person held items, slower local attack swing animation, hide explosions, instant sneak camera height, opaque pale-blue tint for invisible living entities, blindness fog removal, lowered first-person fire overlay, and damage camera shake removal.
- `/k` aliases the full `/kesler` command tree.
- **Hold right click** (`feature/holdrightclick`): `/kesler rightclick` (also `/k rightclick`) toggles a virtual held use key; opening any screen or disconnecting stops it.
- **Anvil book merger** (`feature/anvilmerge`): `/kesler merge` (or `/k merge`) opens Hypixel `/av` and combines configured enchantment books (Legion, Rejuvenate, Wisdom, Ultimate Wise, Last Stand, Smarty Pants, and Feather Falling). It waits up to 10 seconds per slot transition, returns items from input slots 29 and 33 after a timeout or mismatch, then retries; it stops immediately if the container closes.
- `/kesler` opens the config screen (opened on the next tick so the closing chat screen doesn't override it).

## 26.x gotchas
- Screens moved: use `mc.gui.screen()` and `mc.gui.setScreen(...)`, not `mc.screen` / `mc.setScreen`
- `LocalPlayer.swing(hand)` plays the local animation and sends the swing packet to the server
- Skull profile: `SkullBlockEntity.ownerProfile?.partialProfile()?.id()`
- Mixin targets were written from older-version knowledge and are the first suspects if the game crashes at launch: `ChatComponent.getHeight()I`, `KeyMapping.clickCount`, `Minecraft.missTime`. Check the real 26.2 classes in IntelliJ or the Fabric docs rather than guessing
- Tooltip scrolling hooks `GuiGraphicsExtractor.tooltip`, `MouseHandler.onScroll`, and item tooltip identity; verify these 26.2 methods if the mixin fails to apply
- Verify API names against the actual jar before relying on memory; this version is newer than most training data

## Status
- Working (confirmed in game): chat height, config screen and Mod Menu entry, secret clicker, auto clicker
- Extended chat history: implemented with an off-by-default toggle; needs in-game confirmation
- Auto clicker: zeroing `Minecraft.missTime` each tick while active fixed the wait between clicks, so the `missTime` mixin accessor is required
- Reduced fire overlay: implemented with an off-by-default Visuals toggle; needs in-game confirmation
- Damage camera shake removal: implemented with an off-by-default Visuals toggle; needs in-game confirmation
- Auto gatherer: implemented; needs in-game confirmation
- Dungeon stopwatch: implemented; event detection and timing need in-game confirmation
- Fire Freeze automation: implemented in its own feature package; event detection, movement, item identification, and activation need in-game confirmation

## Preferences
- Targeted edits over full rewrites, but give complete file contents when replacing a file
- Minimal code comments
- Give multiple steps at once
- Keep the code structure above; don't add libraries without asking
- Keep this file updated whenever features are added or changed

## Note
Hypixel prohibits modifications that automate gameplay. These features are for personal use and carry ban risk.
