package kesler.keslerclient.feature.chat

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object ChatSection : ConfigSection {
    override val title = "Chat"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(
            entries.startBooleanToggle(Component.literal("Custom Chat Height"), cfg.chatHeightEnabled)
                .setDefaultValue(defaults.chatHeightEnabled)
                .setSaveConsumer { cfg.chatHeightEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startIntSlider(Component.literal("Chat Height"), cfg.chatHeight, 20, 1000)
                .setDefaultValue(defaults.chatHeight)
                .setSaveConsumer { cfg.chatHeight = it }
                .build()
        )
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Extended Chat History (up to 10,000 entries)"), cfg.extendedChatHistoryEnabled)
                .setDefaultValue(defaults.extendedChatHistoryEnabled)
                .setSaveConsumer { cfg.extendedChatHistoryEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Right-Click Chat to Copy"), cfg.copyChatOnRightClickEnabled)
                .setDefaultValue(defaults.copyChatOnRightClickEnabled)
                .setSaveConsumer { cfg.copyChatOnRightClickEnabled = it }
                .build()
        )
        category.addEntry(
            entries.startBooleanToggle(Component.literal("Merge Consecutive Identical Chat"), cfg.mergeConsecutiveChatEnabled)
                .setDefaultValue(defaults.mergeConsecutiveChatEnabled)
                .setSaveConsumer { cfg.mergeConsecutiveChatEnabled = it }
                .build()
        )
    }
}
