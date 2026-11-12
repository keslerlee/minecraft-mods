package kesler.keslerclient.feature.gathering

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object GatheringSection : ConfigSection {
    override val title = "Gathering"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

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
    }
}
