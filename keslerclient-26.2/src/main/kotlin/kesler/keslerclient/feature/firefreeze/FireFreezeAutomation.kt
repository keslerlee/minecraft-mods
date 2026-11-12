package kesler.keslerclient.feature.firefreeze

import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand

object FireFreezeAutomation {
    private var fireFreezeUsed = false

    fun init() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!isInCatacombs()) reset()
        }
        ClientReceiveMessageEvents.GAME.register { message, _ -> onMessage(message.string) }
        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ -> onMessage(message.string) }
    }

    private fun onMessage(text: String) {
        if (!ConfigManager.get().fireFreezeCueEnabled || !isInCatacombs()) return
        if (fireFreezeUsed || !text.contains("[BOSS] The Professor", ignoreCase = true)) return
        if (!text.contains("Even if you took my barrier down, I can still fight.", ignoreCase = true)) return

        fireFreezeUsed = true
        useFireFreezeIfAvailable()
    }

    private fun useFireFreezeIfAvailable() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        if (client.gui.screen() != null) return

        val inventory = player.inventory
        val selectedSlot = inventory.selectedSlot
        val staffSlot = if (isFireFreezeStaff(inventory.getItem(selectedSlot))) {
            selectedSlot
        } else {
            (0 until 9).firstOrNull { isFireFreezeStaff(inventory.getItem(it)) } ?: return
        }

        if (staffSlot != selectedSlot) inventory.setSelectedSlot(staffSlot)
        client.gameMode?.useItem(player, InteractionHand.MAIN_HAND)
        player.swing(InteractionHand.MAIN_HAND)
    }

    private fun isFireFreezeStaff(stack: net.minecraft.world.item.ItemStack): Boolean =
        !stack.isEmpty && stack.hoverName.string.contains("Fire Freeze Staff", ignoreCase = true)

    private fun isInCatacombs(): Boolean {
        val players = Minecraft.getInstance().connection?.onlinePlayers ?: return false
        return players.any { it.tabListDisplayName?.string?.contains("Dungeon: Catacombs", ignoreCase = true) == true }
    }

    private fun reset() {
        fireFreezeUsed = false
    }
}
