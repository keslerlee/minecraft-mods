package kesler.keslerclient.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

class Config {
    // Chat height: disabled = vanilla behavior
    @JvmField var chatHeightEnabled: Boolean = false
    @JvmField var chatHeight: Int = 180 // pixels
    @JvmField var additionalAttackKey: String = "key.keyboard.unknown"
    @JvmField var additionalUseKey: String = "key.keyboard.unknown"
    @JvmField var additionalForwardKey: String = "key.keyboard.unknown"
    @JvmField var additionalBackKey: String = "key.keyboard.unknown"
    @JvmField var additionalLeftKey: String = "key.keyboard.unknown"
    @JvmField var additionalRightKey: String = "key.keyboard.unknown"
    @JvmField var additionalJumpKey: String = "key.keyboard.unknown"
    @JvmField var additionalSneakKey: String = "key.keyboard.unknown"
    @JvmField var additionalSprintKey: String = "key.keyboard.unknown"
    @JvmField var alwaysSprintEnabled: Boolean = false
    @JvmField var extendedChatHistoryEnabled: Boolean = false
    @JvmField var copyChatOnRightClickEnabled: Boolean = false
    @JvmField var mergeConsecutiveChatEnabled: Boolean = false
    @JvmField var customCommandBindings: MutableList<String> = mutableListOf()

    // Dungeon boss stopwatch HUD
    @JvmField var dungeonStopwatchEnabled: Boolean = false
    @JvmField var dungeonStopwatchX: Int = 50
    @JvmField var dungeonStopwatchY: Int = 10
    @JvmField var dungeonStopwatchScale: Int = 100
    @JvmField var fireFreezeCueEnabled: Boolean = false

    // Scroll long item tooltips
    @JvmField var tooltipScrollingEnabled: Boolean = false

    // Visual adjustments
    @JvmField var fullbrightEnabled: Boolean = false
    @JvmField var hideMobDeathAnimationEnabled: Boolean = false
    @JvmField var containerDebugOverlayEnabled: Boolean = false
    @JvmField var smallerHeldItemsEnabled: Boolean = false
    @JvmField var slowAttackAnimationEnabled: Boolean = false
    @JvmField var hideExplosionsEnabled: Boolean = false
    @JvmField var instantSneakAnimationEnabled: Boolean = false
    @JvmField var showInvisibleEntitiesEnabled: Boolean = false
    @JvmField var disableBlindnessVisualsEnabled: Boolean = false
    @JvmField var reduceFireOverlayEnabled: Boolean = false
    @JvmField var disableDamageCameraShakeEnabled: Boolean = false

    // Secret clicker
    @JvmField var secretClickerEnabled: Boolean = false
    @JvmField var witherSkullUuid: String = ""
    @JvmField var redstoneSkullUuid: String = ""

    // Gathering helpers
    @JvmField var lushllilacBushAutoHarvestEnabled: Boolean = false
    @JvmField var seaLumieAutoHarvestEnabled: Boolean = false

    // Auto clicker
    @JvmField var autoClickerEnabled: Boolean = false
    @JvmField var autoClickerCps: Int = 9
    @JvmField var autoClickerSpread: Int = 2
}

object ConfigManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val path = FabricLoader.getInstance().configDir.resolve("keslerclient.json")
    private var config: Config? = null

    @JvmStatic
    fun get(): Config = config ?: load()

    @JvmStatic
    fun load(): Config {
        val loaded = runCatching {
            if (Files.exists(path)) gson.fromJson(Files.readString(path), Config::class.java) else null
        }.getOrNull() ?: Config()
        config = loaded
        save()
        return loaded
    }

    @JvmStatic
    fun save() {
        runCatching { Files.writeString(path, gson.toJson(get())) }
    }
}
