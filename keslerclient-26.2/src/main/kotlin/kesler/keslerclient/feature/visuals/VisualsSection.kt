package kesler.keslerclient.feature.visuals

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.gui.ConfigSection
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.network.chat.Component

object VisualsSection : ConfigSection {
    override val title = "Visuals"

    override fun build(category: ConfigCategory, entries: ConfigEntryBuilder) {
        val cfg = ConfigManager.get()
        val defaults = Config()

        category.addEntry(entries.startBooleanToggle(Component.literal("Fullbright"), cfg.fullbrightEnabled)
            .setDefaultValue(defaults.fullbrightEnabled)
            .setSaveConsumer { cfg.fullbrightEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Hide Mob Death Animation"), cfg.hideMobDeathAnimationEnabled)
            .setDefaultValue(defaults.hideMobDeathAnimationEnabled)
            .setSaveConsumer { cfg.hideMobDeathAnimationEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Container Debug Overlay"), cfg.containerDebugOverlayEnabled)
            .setDefaultValue(defaults.containerDebugOverlayEnabled)
            .setSaveConsumer { cfg.containerDebugOverlayEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Smaller Held Items"), cfg.smallerHeldItemsEnabled)
            .setDefaultValue(defaults.smallerHeldItemsEnabled)
            .setSaveConsumer { cfg.smallerHeldItemsEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Slow Attack Animation"), cfg.slowAttackAnimationEnabled)
            .setDefaultValue(defaults.slowAttackAnimationEnabled)
            .setSaveConsumer { cfg.slowAttackAnimationEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Hide Explosions"), cfg.hideExplosionsEnabled)
            .setDefaultValue(defaults.hideExplosionsEnabled)
            .setSaveConsumer { cfg.hideExplosionsEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Instant Sneak Animation"), cfg.instantSneakAnimationEnabled)
            .setDefaultValue(defaults.instantSneakAnimationEnabled)
            .setSaveConsumer { cfg.instantSneakAnimationEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Show Invisible Players and Mobs"), cfg.showInvisibleEntitiesEnabled)
            .setDefaultValue(defaults.showInvisibleEntitiesEnabled)
            .setSaveConsumer { cfg.showInvisibleEntitiesEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Disable Blindness Visuals"), cfg.disableBlindnessVisualsEnabled)
            .setDefaultValue(defaults.disableBlindnessVisualsEnabled)
            .setSaveConsumer { cfg.disableBlindnessVisualsEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Reduce Fire Overlay"), cfg.reduceFireOverlayEnabled)
            .setDefaultValue(defaults.reduceFireOverlayEnabled)
            .setSaveConsumer { cfg.reduceFireOverlayEnabled = it }
            .build())
        category.addEntry(entries.startBooleanToggle(Component.literal("Disable Damage Camera Shake"), cfg.disableDamageCameraShakeEnabled)
            .setDefaultValue(defaults.disableDamageCameraShakeEnabled)
            .setSaveConsumer { cfg.disableDamageCameraShakeEnabled = it }
            .build())
    }
}
