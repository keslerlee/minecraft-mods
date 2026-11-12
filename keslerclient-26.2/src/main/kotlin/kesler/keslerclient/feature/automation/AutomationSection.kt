package kesler.keslerclient.feature.automation

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object AutomationSection : ConfigSection {
    override val title = "Automation"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Enable Auto Clicker"), cfg.autoClickerEnabled)
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
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Auto Harvest Lushllilac Bushes"), cfg.lushllilacBushAutoHarvestEnabled)
                .setDefaultValue(defaults.lushllilacBushAutoHarvestEnabled)
                .setSaveConsumer { cfg.lushllilacBushAutoHarvestEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Auto Harvest Sea Lumies (3+)"), cfg.seaLumieAutoHarvestEnabled)
                .setDefaultValue(defaults.seaLumieAutoHarvestEnabled)
                .setSaveConsumer { cfg.seaLumieAutoHarvestEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Auto Fishing"), cfg.autoFishingEnabled)
                .setDefaultValue(defaults.autoFishingEnabled)
                .setSaveConsumer { cfg.autoFishingEnabled = it }
                .build()
        )
    }
}
