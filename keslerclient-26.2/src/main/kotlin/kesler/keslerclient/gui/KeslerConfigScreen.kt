package kesler.keslerclient.gui

import kesler.keslerclient.config.ConfigManager
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object KeslerConfigScreen {
    fun create(parent: Screen?): Screen {
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("KeslerClient"))
            .setSavingRunnable { ConfigManager.save() }

        val entries = builder.entryBuilder()
        Categories.all.forEach { section ->
            section.build(builder.getOrCreateCategory(Component.literal(section.title)), entries)
        }
        return builder.build()
    }
}
