package kesler.keslerclient.feature.holdrightclick

import kesler.keslerclient.mixin.KeyMappingAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft

object HoldRightClick {
    @Volatile
    private var holding = false

    @JvmStatic
    fun isHolding(): Boolean = holding

    @JvmStatic
    fun toggle(client: Minecraft): Boolean {
        if (holding) {
            stop()
            return false
        }

        val useKey = client.options.keyUse
        val accessor = useKey as KeyMappingAccessor
        accessor.keslerSetClickCount(accessor.keslerGetClickCount() + 1)
        holding = true
        return true
    }

    @JvmStatic
    fun stop() {
        holding = false
    }

    fun init() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> stop() }
        ClientTickEvents.START_CLIENT_TICK.register { client ->
            if (holding && (client.player == null || client.level == null || client.gui.screen() != null)) {
                stop()
            }
        }
    }
}
