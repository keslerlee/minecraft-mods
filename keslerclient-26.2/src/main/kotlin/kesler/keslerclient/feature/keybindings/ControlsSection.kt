package kesler.keslerclient.feature.keybindings

import com.mojang.blaze3d.platform.InputConstants
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object ControlsSection : ConfigSection {
    override val title = "Controls"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Always Sprint"), cfg.alwaysSprintEnabled)
                .setDefaultValue(false)
                .setSaveConsumer { cfg.alwaysSprintEnabled = it }
                .build()
        )

        addKey(category, entries, "Additional Attack Key", cfg.additionalAttackKey) { cfg.additionalAttackKey = it }
        addKey(category, entries, "Additional Use Key", cfg.additionalUseKey) { cfg.additionalUseKey = it }
        addKey(category, entries, "Additional Forward Key", cfg.additionalForwardKey) { cfg.additionalForwardKey = it }
        addKey(category, entries, "Additional Back Key", cfg.additionalBackKey) { cfg.additionalBackKey = it }
        addKey(category, entries, "Additional Left Key", cfg.additionalLeftKey) { cfg.additionalLeftKey = it }
        addKey(category, entries, "Additional Right Key", cfg.additionalRightKey) { cfg.additionalRightKey = it }
        addKey(category, entries, "Additional Jump Key", cfg.additionalJumpKey) { cfg.additionalJumpKey = it }
        addKey(category, entries, "Additional Sneak Key", cfg.additionalSneakKey) { cfg.additionalSneakKey = it }
        addKey(category, entries, "Additional Sprint Key", cfg.additionalSprintKey) { cfg.additionalSprintKey = it }
    }

    private fun addKey(
        category: ConfigCategory,
        entries: ConfigEntryBuilder,
        label: String,
        storedKey: String,
        saveKey: (String) -> Unit
    ) {
        val key = runCatching { InputConstants.getKey(storedKey) }.getOrDefault(InputConstants.UNKNOWN)
        category.addEntry(
            entries.startKeyCodeField(Component.literal(label), key)
                .setAllowKey(true)
                .setAllowMouse(false)
                .setAllowModifiers(false)
                .setDefaultValue(InputConstants.UNKNOWN)
                .setKeySaveConsumer {
                    saveKey(it.name)
                    AlternateKeyBindings.refreshMappings()
                }
                .build()
        )
    }
}
