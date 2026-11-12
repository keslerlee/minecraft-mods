package kesler.keslerclient.feature.gathering

import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SeaPickleBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object AutoGatherer {
    private const val BERRY_RETRY_TICKS = 5
    private const val PICKLE_RETRY_TICKS = 5

    private val cooldowns = HashMap<BlockPos, Int>()
    private var miningPos: BlockPos? = null
    private var miningDirection: Direction? = null
    private var ticksSinceSwing = 0

    fun init() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
        ClientTickEvents.END_CLIENT_TICK.register { tick(it) }
    }

    private fun reset() {
        miningPos = null
        miningDirection = null
        ticksSinceSwing = 0
        cooldowns.clear()
    }

    private fun tick(mc: Minecraft) {
        cooldowns.entries.removeIf { entry ->
            val remaining = entry.value - 1
            if (remaining <= 0) true else {
                entry.setValue(remaining)
                false
            }
        }

        val cfg = ConfigManager.get()
        val player = mc.player
        val level = mc.level
        if (player == null || level == null || mc.gui.screen() != null) {
            stopMining(mc)
            return
        }
        val hit = mc.hitResult as? BlockHitResult
        if (hit == null || hit.type != HitResult.Type.BLOCK) {
            stopMining(mc)
            return
        }

        val target = hit.blockPos
        val state = level.getBlockState(target)
        val pickleEligible = cfg.seaLumieAutoHarvestEnabled &&
                state.block == Blocks.SEA_PICKLE && state.getValue(SeaPickleBlock.PICKLES) >= 3

        val currentMiningPos = miningPos
        if (currentMiningPos != null) {
            if (pickleEligible && currentMiningPos == target) {
                val direction = miningDirection ?: hit.direction
                val stillPickle = level.getBlockState(currentMiningPos).let {
                    it.block == Blocks.SEA_PICKLE && it.getValue(SeaPickleBlock.PICKLES) >= 3
                }
                if (stillPickle) {
                    mc.gameMode?.continueDestroyBlock(currentMiningPos, direction)
                    ticksSinceSwing++
                    if (ticksSinceSwing >= 6) {
                        player.swing(InteractionHand.MAIN_HAND)
                        ticksSinceSwing = 0
                    }
                }
                if (!stillPickle) {
                    stopMining(mc)
                    cooldowns[currentMiningPos.immutable()] = PICKLE_RETRY_TICKS
                }
            } else {
                stopMining(mc)
                return
            }
            return
        }

        if (target in cooldowns) return

        if (cfg.lushllilacBushAutoHarvestEnabled && state.block == Blocks.FLOWERING_AZALEA) {
            mc.gameMode?.useItemOn(player, InteractionHand.MAIN_HAND, hit)
            cooldowns[target.immutable()] = BERRY_RETRY_TICKS
            return
        }

        if (pickleEligible) {
            val pos = target.immutable()
            val direction = hit.direction
            if (mc.gameMode?.startDestroyBlock(pos, direction) == true) {
                miningPos = pos
                miningDirection = direction
                player.swing(InteractionHand.MAIN_HAND)
                ticksSinceSwing = 0
            }
        }
    }

    private fun stopMining(mc: Minecraft) {
        if (miningPos != null) mc.gameMode?.stopDestroyBlock()
        miningPos = null
        miningDirection = null
        ticksSinceSwing = 0
    }
}
