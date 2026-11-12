package kesler.keslerclient.feature.secrets

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object SecretsSection : ConfigSection {
    override val title = "Secrets"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Secret Clicker"), cfg.secretClickerEnabled)
                .setDefaultValue(defaults.secretClickerEnabled)
                .setSaveConsumer { cfg.secretClickerEnabled = it }
                .build()
        )
    }
}
