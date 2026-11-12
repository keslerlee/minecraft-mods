package kesler.keslerclient.feature.gathering

import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.FishingRodItem

object AutoFishing {
    private const val RECAST_DELAY_TICKS = 6
    private const val MAX_RECAST_WAIT_TICKS = 200

    private var trackedHook: FishingHook? = null
    private var lastBiteStandId: Int? = null
    private var recastDelay = -1
    private var recastWaitTicks = 0

    fun init() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
        ClientTickEvents.START_CLIENT_TICK.register(::tick)
    }

    private fun reset() {
        trackedHook = null
        lastBiteStandId = null
        recastDelay = -1
        recastWaitTicks = 0
    }

    @JvmStatic
    fun onArmorStandUpdated(level: ClientLevel, stand: ArmorStand) {
        val client = Minecraft.getInstance()
        if (!ConfigManager.get().autoFishingEnabled || client.level !== level || client.gui.screen() != null) return
        val player = client.player ?: return
        if (fishingRodHand(player) == null || player.fishing == null) return
        if (!stand.isInvisible || !stand.hasCustomName() || !stand.isCustomNameVisible || stand.customName?.string != "!!!") return

        val hook = player.fishing ?: return
        if (!hook.boundingBox.inflate(4.0).contains(stand.position()) || stand.id == lastBiteStandId) return

        lastBiteStandId = stand.id
        client.gameMode?.useItem(player, fishingRodHand(player) ?: return)
        recastDelay = RECAST_DELAY_TICKS
        recastWaitTicks = 0
    }

    private fun tick(client: Minecraft) {
        if (!ConfigManager.get().autoFishingEnabled) {
            reset()
            return
        }

        val player = client.player
        val level = client.level
        if (player == null || level == null || client.gui.screen() != null) {
            reset()
            return
        }

        val hand = fishingRodHand(player)
        if (hand == null) {
            reset()
            return
        }

        val hook = findOwnedHook(level, player)
        if (hook != null && hook !== trackedHook) {
            trackedHook = hook
            lastBiteStandId = null
        } else if (hook == null) {
            trackedHook = null
            lastBiteStandId = null
        }

        if (recastDelay >= 0) {
            if (recastDelay > 0) recastDelay--
            if (recastDelay == 0) {
                if (hook == null) {
                    client.gameMode?.useItem(player, hand)
                    reset()
                } else if (++recastWaitTicks >= MAX_RECAST_WAIT_TICKS) {
                    recastDelay = -1
                    recastWaitTicks = 0
                }
            }
            return
        }

    }

    private fun fishingRodHand(player: net.minecraft.world.entity.player.Player): InteractionHand? = when {
        player.getItemInHand(InteractionHand.MAIN_HAND).item is FishingRodItem -> InteractionHand.MAIN_HAND
        player.getItemInHand(InteractionHand.OFF_HAND).item is FishingRodItem -> InteractionHand.OFF_HAND
        else -> null
    }

    private fun findOwnedHook(level: ClientLevel, player: net.minecraft.world.entity.player.Player): FishingHook? =
        player.fishing ?: level.getEntitiesOfClass(
            FishingHook::class.java,
            player.boundingBox.inflate(64.0)
        ) { it.getPlayerOwner() === player }.firstOrNull()
}
