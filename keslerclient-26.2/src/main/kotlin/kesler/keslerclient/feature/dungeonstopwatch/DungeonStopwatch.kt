package kesler.keslerclient.feature.dungeonstopwatch

import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.util.Locale

object DungeonStopwatch {
    private var startedAtNanos: Long? = null

    // Only start on the floor boss's opening dialogue. Other [BOSS] lines can
    // come from Blood Room mobs spawned by the Watcher before the boss room.
    private val bossIntroLines = listOf(
        "gratz for making it this far", // Bonzo
        "this is where the journey ends for you", // Scarf
        "i was burdened with terrible news recently", // The Professor
        "welcome adventurers! i am thorn", // Thorn
        "welcome, you arrived right on time", // Livid
        "so you made it all the way here", // Sadan
        "well well well look who's here" // Maxor
    )

    fun init() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (startedAtNanos != null && (client.player == null || !isInCatacombs())) {
                reset()
            }
        }
        ClientReceiveMessageEvents.GAME.register { message, _ -> onBossMessage(message.string) }
        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ -> onBossMessage(message.string) }

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("keslerclient", "dungeon_stopwatch"),
            HudElement { graphics, _ -> render(graphics) }
        )
    }

    private fun onBossMessage(text: String) {
        if (!isInCatacombs()) return
        if (startedAtNanos != null) return
        if (!text.contains("[BOSS]", ignoreCase = true)) return
        val normalized = text.lowercase(Locale.ROOT)
        if (bossIntroLines.none { intro -> normalized.contains(intro) }) return
        startedAtNanos = System.nanoTime()
    }

    private fun reset() {
        startedAtNanos = null
    }

    private fun isInCatacombs(): Boolean {
        val players = Minecraft.getInstance().connection?.onlinePlayers ?: return false
        return players.any { it.tabListDisplayName?.string?.contains("Dungeon: Catacombs", ignoreCase = true) == true }
    }

    private fun render(graphics: GuiGraphicsExtractor) {
        val started = startedAtNanos ?: return
        val cfg = ConfigManager.get()
        if (!cfg.dungeonStopwatchEnabled) return

        val elapsedHundredths = ((System.nanoTime() - started) / 10_000_000L).coerceAtLeast(0)
        val hours = elapsedHundredths / 360_000
        val minutes = (elapsedHundredths / 6_000) % 60
        val seconds = (elapsedHundredths / 100) % 60
        val hundredths = elapsedHundredths % 100
        val time = if (hours > 0) String.format(Locale.ROOT, "%d:%02d:%02d.%02d", hours, minutes, seconds, hundredths)
        else String.format(Locale.ROOT, "%02d:%02d.%02d", minutes, seconds, hundredths)
        val label = time
        val font = Minecraft.getInstance().font
        val scale = (cfg.dungeonStopwatchScale.coerceIn(50, 200) / 100f)
        val boxWidth = font.width(label) + 12
        val boxHeight = 20
        val availableWidth = (graphics.guiWidth() - boxWidth * scale).coerceAtLeast(0f)
        val availableHeight = (graphics.guiHeight() - boxHeight * scale).coerceAtLeast(0f)
        val x = availableWidth * (cfg.dungeonStopwatchX.coerceIn(0, 100) / 100f)
        val y = availableHeight * (cfg.dungeonStopwatchY.coerceIn(0, 100) / 100f)

        val pose = graphics.pose()
        pose.pushMatrix()
        pose.translate(x, y)
        pose.scale(scale, scale)
        graphics.fill(0, 0, boxWidth, boxHeight, 0xA0000000.toInt())
        graphics.text(font, Component.literal(label), 6, 6, 0xFFFFFFFF.toInt())
        pose.popMatrix()
    }
}
