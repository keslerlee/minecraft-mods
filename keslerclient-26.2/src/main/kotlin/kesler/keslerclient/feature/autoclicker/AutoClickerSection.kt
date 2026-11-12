package kesler.keslerclient.feature.autoclicker

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object AutoClickerSection : ConfigSection {
    override val title = "Auto Clicker"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Auto Clicker"), cfg.autoClickerEnabled)
                .setDefaultValue(defaults.autoClickerEnabled)
                .setSaveConsumer { cfg.autoClickerEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startIntSlider(Component.literal("Average CPS"), cfg.autoClickerCps, 1, 20)
                .setDefaultValue(defaults.autoClickerCps)
                .setSaveConsumer { cfg.autoClickerCps = it }
                .build()
        )
        category.addEntry(
            entries.startIntSlider(Component.literal("CPS Spread"), cfg.autoClickerSpread, 0, 10)
                .setDefaultValue(defaults.autoClickerSpread)
                .setSaveConsumer { cfg.autoClickerSpread = it }
                .build()
        )
    }
}