package kesler.keslerclient.feature.dungeonstopwatch

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object DungeonStopwatchSection : ConfigSection {
    override val title = "Dungeons"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Dungeon Boss Stopwatch"), cfg.dungeonStopwatchEnabled)
                .setDefaultValue(defaults.dungeonStopwatchEnabled)
                .setSaveConsumer { cfg.dungeonStopwatchEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startIntSlider(Component.literal("Horizontal Position (%)"), cfg.dungeonStopwatchX, 0, 100)
                .setDefaultValue(defaults.dungeonStopwatchX)
                .setSaveConsumer { cfg.dungeonStopwatchX = it }
                .build()
        )
        category.addEntry(
            entries.startIntSlider(Component.literal("Vertical Position (%)"), cfg.dungeonStopwatchY, 0, 100)
                .setDefaultValue(defaults.dungeonStopwatchY)
                .setSaveConsumer { cfg.dungeonStopwatchY = it }
                .build()
        )
        category.addEntry(
            entries.startIntSlider(Component.literal("Scale (%)"), cfg.dungeonStopwatchScale, 50, 200)
                .setDefaultValue(defaults.dungeonStopwatchScale)
                .setSaveConsumer { cfg.dungeonStopwatchScale = it }
                .build()
        )
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Auto Walk and Use Fire Freeze (F3/M3)"), cfg.fireFreezeCueEnabled)
                .setDefaultValue(defaults.fireFreezeCueEnabled)
                .setSaveConsumer { cfg.fireFreezeCueEnabled = it }
                .build()
        )
    }
}
