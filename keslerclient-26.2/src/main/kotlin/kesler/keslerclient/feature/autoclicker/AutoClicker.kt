package kesler.keslerclient.feature.autoclicker

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.mixin.KeyMappingAccessor
import kesler.keslerclient.mixin.MinecraftAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import java.util.concurrent.ThreadLocalRandom

object AutoClicker {
    private var wasDown = false
    private var nextClickAt = 0L

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register { tick(it) }
    }

    private fun tick(mc: Minecraft) {
        val cfg = ConfigManager.get()
        val key = mc.options.keyAttack
        val active = cfg.autoClickerEnabled && mc.player != null && mc.gui.screen() == null && key.isDown

        if (!active) {
            wasDown = false
            return
        }

        // Clear vanilla's post-miss attack delay so queued clicks aren't swallowed
        (mc as MinecraftAccessor).keslerSetMissTime(0)

        val now = System.nanoTime()
        if (!wasDown) {
            wasDown = true
            nextClickAt = now + nextInterval(cfg)
            return
        }

        if (now >= nextClickAt) {
            val accessor = key as KeyMappingAccessor
            accessor.keslerSetClickCount(accessor.keslerGetClickCount() + 1)
            nextClickAt = now + nextInterval(cfg)
        }
    }

    private fun nextInterval(cfg: Config): Long {
        val spread = cfg.autoClickerSpread.coerceAtLeast(0)
        val low = (cfg.autoClickerCps - spread).coerceAtLeast(1)
        val high = (cfg.autoClickerCps + spread).coerceAtLeast(low)
        val cps = if (high > low) ThreadLocalRandom.current().nextDouble(low.toDouble(), high.toDouble())
        else low.toDouble()
        return (1_000_000_000L / cps).toLong()
    }
}