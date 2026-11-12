package kesler.keslerclient.feature.chat

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object CommandsSection : ConfigSection {
    override val title = "Commands"
    private const val BINDING_SLOTS = 10

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val config = ConfigManager.get()
        val slotCount = maxOf(BINDING_SLOTS, config.customCommandBindings.size)
        val aliases = MutableList(slotCount) { "" }
        val targets = MutableList(slotCount) { "" }

        config.customCommandBindings.forEachIndexed { index, line ->
            val separator = line.indexOf("->")
            if (separator < 0) {
                aliases[index] = line.trim()
            } else {
                aliases[index] = line.substring(0, separator).trim()
                targets[index] = line.substring(separator + 2).trim()
            }
        }

        category.addEntry(
            entries.startTextDescription(
                Component.literal("There are 10 binding slots. Alias N maps to Runs command N. Example: /home -> /warp home. Arguments after the alias are appended to the target.")
            ).build()
        )

        for (index in 0 until slotCount) {
            val slot = index
            category.addEntry(
                entries.startStrField(Component.literal("Alias ${index + 1}"), aliases[index])
                    .setDefaultValue("")
                    .setSaveConsumer {
                        aliases[slot] = it
                        saveBindings(config, aliases, targets)
                    }
                    .build()
            )
            category.addEntry(
                entries.startStrField(Component.literal("Runs command ${index + 1}"), targets[index])
                    .setDefaultValue("")
                    .setSaveConsumer {
                        targets[slot] = it
                        saveBindings(config, aliases, targets)
                    }
                    .build()
            )
        }
    }

    private fun saveBindings(config: Config, aliases: List<String>, targets: List<String>) {
        config.customCommandBindings = aliases.indices.mapNotNull { index ->
            val alias = aliases[index].trim()
            val target = targets[index].trim()
            if (alias.isEmpty() || target.isEmpty()) null else "$alias -> $target"
        }.toMutableList()
    }
}
