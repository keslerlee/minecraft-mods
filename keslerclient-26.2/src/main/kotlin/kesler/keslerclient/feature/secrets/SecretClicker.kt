package kesler.keslerclient.feature.secrets

import kesler.keslerclient.config.Config
import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.SkullBlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import java.util.UUID

object SecretClicker {
    private const val SCAN_LIMIT_TICKS = 600 // keep checking the tab list for 30s after joining a world
    private const val CHEST_SCREEN_CLOSE_WINDOW_TICKS = 60

    private val clicked = HashSet<Long>()
    private var inDungeon = false
    private var scanTicks = 0
    private var chestScreenCloseTicks = 0

    fun init() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
        ClientTickEvents.END_CLIENT_TICK.register { tick(it) }
    }

    private fun reset() {
        clicked.clear()
        inDungeon = false
        scanTicks = 0
        chestScreenCloseTicks = 0
    }

    private fun tick(mc: Minecraft) {
        closeDelayedChestScreen(mc)

        val player = mc.player ?: return
        val level = mc.level ?: return

        if (!inDungeon) {
            if (scanTicks < SCAN_LIMIT_TICKS) {
                scanTicks++
                if (scanTicks % 20 == 0) inDungeon = tabHasDungeon(mc)
            }
            return
        }

        val cfg = ConfigManager.get()
        if (!cfg.secretClickerEnabled) return
        if (mc.gui.screen() != null) return

        val hit = mc.hitResult as? BlockHitResult ?: return
        if (hit.type != HitResult.Type.BLOCK) return

        val key = hit.blockPos.asLong()
        if (key in clicked) return
        if (!isSecret(level, hit.blockPos, cfg)) return

        val isChest = level.getBlockState(hit.blockPos).block == Blocks.CHEST
        mc.gameMode?.useItemOn(player, InteractionHand.MAIN_HAND, hit)
        if (isChest) chestScreenCloseTicks = CHEST_SCREEN_CLOSE_WINDOW_TICKS
        clicked.add(key)
    }

    private fun closeDelayedChestScreen(mc: Minecraft) {
        if (chestScreenCloseTicks <= 0) return

        if (mc.gui.screen() is ContainerScreen) {
            mc.gui.setScreen(null)
            chestScreenCloseTicks = 0
        } else {
            chestScreenCloseTicks--
        }
    }

    private fun isSecret(level: Level, pos: BlockPos, cfg: Config): Boolean {
        val block = level.getBlockState(pos).block
        return when (block) {
            Blocks.LEVER, Blocks.CHEST -> true
            Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD -> {
                val id = skullUuid(level, pos)?.toString() ?: return false
                id.equals(cfg.witherSkullUuid, ignoreCase = true) ||
                        id.equals(cfg.redstoneSkullUuid, ignoreCase = true)
            }
            else -> false
        }
    }

    private fun tabHasDungeon(mc: Minecraft): Boolean {
        val players = mc.connection?.onlinePlayers ?: return false
        return players.any { it.tabListDisplayName?.string?.contains("Dungeon: Catacombs") == true }
    }

    fun skullUuid(level: Level, pos: BlockPos): UUID? {
        val skull = level.getBlockEntity(pos) as? SkullBlockEntity ?: return null
        return skull.ownerProfile?.partialProfile()?.id()
    }

    fun lookedAtSkullUuid(mc: Minecraft): UUID? {
        val hit = mc.hitResult as? BlockHitResult ?: return null
        if (hit.type != HitResult.Type.BLOCK) return null
        val level = mc.level ?: return null
        return skullUuid(level, hit.blockPos)
    }
}
