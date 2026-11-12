package kesler.keslerclient.feature.tooltip

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object TooltipSection : ConfigSection {
    override val title = "Tooltips"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Scrollable Tooltips"), cfg.tooltipScrollingEnabled)
                .setDefaultValue(defaults.tooltipScrollingEnabled)
                .setSaveConsumer { cfg.tooltipScrollingEnabled = it }
                .build()
        )
    }
}
