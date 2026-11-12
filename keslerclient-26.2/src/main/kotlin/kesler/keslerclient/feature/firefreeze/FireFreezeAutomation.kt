package kesler.keslerclient.feature.firefreeze

import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object FireFreezeAutomation {
    private var walkStarted = false
    private var fireFreezeUsed = false
    private var fireFreezePending = false
    private var fireFreezePendingSinceNanos = 0L
    private var walkStartedAtNanos = 0L
    private var walkTarget: Vec3? = null

    private val standPositions = listOf(
        Vec3(4.5, 71.0, 1.5),
        Vec3(1.5, 71.0, -1.5),
        Vec3(-1.5, 71.0, 1.5),
        Vec3(1.5, 71.0, 4.5)
    )

    fun init() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!isInCatacombs()) {
                if (walkStarted || fireFreezeUsed || fireFreezePending || walkTarget != null) reset()
            } else if (walkTarget != null) {
                tickWalk(client)
            }
        }
        ClientReceiveMessageEvents.GAME.register { message, _ -> onMessage(message.string) }
        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ -> onMessage(message.string) }
    }

    private fun onMessage(text: String) {
        if (!ConfigManager.get().fireFreezeCueEnabled || !isInCatacombs()) return
        if (!text.contains("[BOSS] The Professor", ignoreCase = true)) return

        if (!walkStarted && text.contains("Oh? You found my Guardians' one weakness", ignoreCase = true)) {
            startWalking()
        }
        if (!fireFreezeUsed && text.contains("Even if you took my barrier down, I can still fight.", ignoreCase = true)) {
            fireFreezeUsed = true
            fireFreezePending = true
            fireFreezePendingSinceNanos = System.nanoTime()
            tryUseIfReady()
        }
    }

    private fun startWalking() {
        walkStarted = true
        val player = Minecraft.getInstance().player ?: return
        walkTarget = standPositions.minByOrNull { target ->
            val dx = target.x - player.x
            val dz = target.z - player.z
            dx * dx + dz * dz
        }
        if (walkTarget == null) return
        walkStartedAtNanos = System.nanoTime()
    }

    private fun tickWalk(client: Minecraft) {
        val player = client.player ?: run {
            stopWalking()
            return
        }
        val target = walkTarget ?: return
        if (client.gui.screen() != null || !ConfigManager.get().fireFreezeCueEnabled) {
            stopWalking()
            fireFreezePending = false
            return
        }

        val dx = target.x - player.x
        val dz = target.z - player.z
        if (dx * dx + dz * dz <= 0.20 * 0.20) {
            stopWalking()
            tryUseIfReady()
            return
        }

        val now = System.nanoTime()
        // Keep one chosen stand, but continuously correct the heading toward that stand.
        val desiredYaw = Math.toDegrees(atan2(-dx, dz))
        val yawDifference = Math.toRadians(wrapDegrees(desiredYaw - player.yRot.toDouble()))
        val forward = cos(yawDifference)
        val strafe = -sin(yawDifference)
        client.options.keyUp.setDown(forward > 0.38)
        client.options.keyDown.setDown(forward < -0.38)
        client.options.keyLeft.setDown(strafe > 0.38)
        client.options.keyRight.setDown(strafe < -0.38)

        // Jump as soon as horizontal collision shows the current heading hit a wall.
        client.options.keyJump.setDown(player.horizontalCollision && player.onGround())

        // Stop if blocked or if the Professor cue arrives before we reach the stand.
        if (now - walkStartedAtNanos > 3_000_000_000L ||
            (fireFreezePending && now - fireFreezePendingSinceNanos > 2_000_000_000L)
        ) {
            stopWalking()
            tryUseIfReady()
        }
    }

    private fun wrapDegrees(degrees: Double): Double {
        var wrapped = degrees % 360.0
        if (wrapped >= 180.0) wrapped -= 360.0
        if (wrapped < -180.0) wrapped += 360.0
        return wrapped
    }

    private fun stopWalking() {
        val options = Minecraft.getInstance().options
        options.keyUp.setDown(false)
        options.keyDown.setDown(false)
        options.keyLeft.setDown(false)
        options.keyRight.setDown(false)
        options.keyJump.setDown(false)
        walkTarget = null
    }

    private fun tryUseIfReady() {
        if (!fireFreezePending || walkTarget != null) return
        fireFreezePending = false
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        if (client.gui.screen() != null || !isWithinFireFreezeRange(player)) return
        if (!selectFireFreezeStaff()) return
        client.gameMode?.useItem(player, InteractionHand.MAIN_HAND)
        player.swing(InteractionHand.MAIN_HAND)
    }

    private fun isWithinFireFreezeRange(player: net.minecraft.client.player.LocalPlayer): Boolean =
        standPositions.any { stand -> player.position().distanceToSqr(stand) <= 1.0 }

    private fun selectFireFreezeStaff(): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val inventory = player.inventory
        val selectedSlot = inventory.selectedSlot
        val staffSlot = if (isFireFreezeStaff(inventory.getItem(selectedSlot))) selectedSlot
        else (0 until 9).firstOrNull { isFireFreezeStaff(inventory.getItem(it)) } ?: return false

        if (staffSlot != selectedSlot) inventory.setSelectedSlot(staffSlot)
        return true
    }

    private fun isFireFreezeStaff(stack: net.minecraft.world.item.ItemStack): Boolean =
        !stack.isEmpty && stack.hoverName.string.contains("Fire Freeze Staff", ignoreCase = true)

    private fun isInCatacombs(): Boolean {
        val players = Minecraft.getInstance().connection?.onlinePlayers ?: return false
        return players.any { it.tabListDisplayName?.string?.contains("Dungeon: Catacombs", ignoreCase = true) == true }
    }

    private fun reset() {
        stopWalking()
        walkStarted = false
        fireFreezeUsed = false
        fireFreezePending = false
    }
}
