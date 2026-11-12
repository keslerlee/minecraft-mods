package kesler.keslerclient.feature.anvilmerge

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object AnvilBookMerger {
    private const val RESULT_SLOT = 13
    private val targetLevels = linkedMapOf(
        "Legion" to 5,
        "Rejuvenate" to 5,
        "Wisdom" to 5,
        "Ultimate Wise" to 5,
        "Last Stand" to 5,
        "Smarty Pants" to 5,
        "Feather Falling" to 10,
        "Overload" to 5,
        "Soul Eater" to 5,
        "Dedication" to 3,
        "Karma" to 5,
        "Petalfall" to 5,
        "First Impression" to 5
    )
    private val romanToInt = linkedMapOf(
        "I" to 1, "II" to 2, "III" to 3, "IV" to 4, "V" to 5,
        "VI" to 6, "VII" to 7, "VIII" to 8, "IX" to 9, "X" to 10
    )
    private val formattingCode = Regex("(?i)§[0-9A-FK-ORX]")
    private val stepDelayNanos = 200_000_000L
    private val stepTimeoutNanos = 10_000_000_000L

    private enum class Phase {
        WAIT_MENU, FIND_PAIR, WAIT_FIRST_BOOK, WAIT_SECOND_BOOK, WAIT_SIGN, WAIT_RESULT,
        RECOVER_INPUTS, WAIT_RECOVERY
    }
    private data class EnchantBook(val name: String, val level: Int)
    private data class BookPair(val enchant: EnchantBook, val firstSlot: Int, val secondSlot: Int)

    private class Run(val startedAt: Long) {
        var phase = Phase.WAIT_MENU
        var menu: AbstractContainerMenu? = null
        var notBefore = startedAt + stepDelayNanos
        var deadline = startedAt + stepTimeoutNanos
        var pair: BookPair? = null
        var recoverySlot = -1
        var recoveryAttempts = 0
    }

    private var run: Run? = null

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(::tick)
    }

    fun start(source: FabricClientCommandSource) {
        if (run != null) {
            source.sendFeedback(Component.literal("Anvil book merge is already running."))
            return
        }
        val player = Minecraft.getInstance().player
        if (player == null) {
            source.sendFeedback(Component.literal("You need to be in a world to merge books."))
            return
        }

        val now = System.nanoTime()
        run = Run(now)
        player.connection.sendCommand("av")
        source.sendFeedback(Component.literal("Merge start."))
    }

    private fun tick(client: Minecraft) {
        val activeRun = run ?: return
        val player = client.player ?: return abort(client, "Player unavailable; stopped the merge.")
        val now = System.nanoTime()

        val menu = activeRun.menu
        if (menu != null && (player.containerMenu !== menu || client.gui.screen() !is AbstractContainerScreen<*>)) {
            return abort(client, "Anvil closed; stopped the merge immediately.")
        }

        if (now > activeRun.deadline) {
            when (activeRun.phase) {
                Phase.WAIT_MENU -> return abort(client, "Anvil did not open in time; stopped safely.")
                Phase.RECOVER_INPUTS, Phase.WAIT_RECOVERY ->
                    return abort(client, "Could not return the input books safely; stopped the merge.")
                else -> {
                    if (activeRun.recoveryAttempts >= 3) {
                        return abort(client, "The anvil kept changing unexpectedly; stopped after three recovery attempts.")
                    }
                    beginRecovery(client, activeRun, now)
                    return
                }
            }
        }
        if (now < activeRun.notBefore) return

        when (activeRun.phase) {
            Phase.WAIT_MENU -> {
                val openedMenu = player.containerMenu
                if (openedMenu === player.inventoryMenu || client.gui.screen() !is AbstractContainerScreen<*> || openedMenu.slots.size < 90) return
                if (!isAnvilReady(openedMenu)) return

                activeRun.menu = openedMenu
                activeRun.phase = Phase.FIND_PAIR
                activeRun.notBefore = now + stepDelayNanos
                activeRun.deadline = now + stepTimeoutNanos
            }

            Phase.FIND_PAIR -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                if (!inputsAreEmpty(activeMenu)) return
                if (activeMenu.slots[RESULT_SLOT].item.item != Items.BARRIER || activeMenu.slots[22].item.item != Items.ANVIL) return
                val inventorySlots = inventorySideSlots(activeMenu, player)
                if (inventorySlots.isEmpty()) return abort(client, "Could not identify your inventory slots; stopped safely.")
                val pair = findPair(activeMenu, inventorySlots)
                if (pair == null) return finish(client, activeMenu, "Merge complete.")

                activeRun.pair = pair
                if (!click(client, player, activeMenu, pair.firstSlot, ContainerInput.QUICK_MOVE)) {
                    return abort(client, "Game interaction unavailable; stopped safely.")
                }
                waitFor(activeRun, Phase.WAIT_FIRST_BOOK, now)
            }

            Phase.WAIT_FIRST_BOOK -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                val pair = activeRun.pair ?: return abort(client, "Merge state was lost; stopped safely.")
                if (!isBook(activeMenu.slots[29].item, pair.enchant) || !activeMenu.slots[33].item.isEmpty) return

                if (!isBook(activeMenu.slots[pair.secondSlot].item, pair.enchant)) {
                    beginRecovery(client, activeRun, now)
                    return
                }
                if (!click(client, player, activeMenu, pair.secondSlot, ContainerInput.QUICK_MOVE)) {
                    return abort(client, "Game interaction unavailable; stopped safely.")
                }
                waitFor(activeRun, Phase.WAIT_SECOND_BOOK, now)
            }

            Phase.WAIT_SECOND_BOOK -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                val pair = activeRun.pair ?: return abort(client, "Merge state was lost; stopped safely.")
                val nextLevel = pair.enchant.level + 1
                if (!isBook(activeMenu.slots[29].item, pair.enchant) || !isBook(activeMenu.slots[33].item, pair.enchant)) return
                if (!isBook(activeMenu.slots[RESULT_SLOT].item, EnchantBook(pair.enchant.name, nextLevel))) return
                if (activeMenu.slots[22].item.item != Items.ANVIL) return

                if (!click(client, player, activeMenu, 22, ContainerInput.PICKUP)) {
                    return abort(client, "Game interaction unavailable; stopped safely.")
                }
                waitFor(activeRun, Phase.WAIT_SIGN, now)
            }

            Phase.WAIT_SIGN -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                val pair = activeRun.pair ?: return abort(client, "Merge state was lost; stopped safely.")
                if (!isSign(activeMenu.slots[22].item)) return
                if (!isBook(activeMenu.slots[RESULT_SLOT].item, EnchantBook(pair.enchant.name, pair.enchant.level + 1))) return

                if (!click(client, player, activeMenu, RESULT_SLOT, ContainerInput.QUICK_MOVE)) {
                    return abort(client, "Game interaction unavailable; stopped safely.")
                }
                waitFor(activeRun, Phase.WAIT_RESULT, now)
            }

            Phase.WAIT_RESULT -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                val pair = activeRun.pair ?: return abort(client, "Merge state was lost; stopped safely.")
                if (!activeMenu.slots[29].item.isEmpty || !activeMenu.slots[33].item.isEmpty) return
                if (isBook(activeMenu.slots[RESULT_SLOT].item, EnchantBook(pair.enchant.name, pair.enchant.level + 1))) return

                activeRun.phase = Phase.FIND_PAIR
                activeRun.pair = null
                activeRun.recoveryAttempts = 0
                activeRun.notBefore = now + stepDelayNanos
                activeRun.deadline = now + stepTimeoutNanos
            }

            Phase.RECOVER_INPUTS -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                val slot = listOf(29, 33).firstOrNull { !activeMenu.slots[it].item.isEmpty }
                if (slot == null) {
                    activeRun.phase = Phase.FIND_PAIR
                    activeRun.pair = null
                    activeRun.recoverySlot = -1
                    activeRun.notBefore = now
                    activeRun.deadline = now + stepTimeoutNanos
                    return
                }

                if (!click(client, player, activeMenu, slot, ContainerInput.QUICK_MOVE)) {
                    return abort(client, "Game interaction unavailable while returning input books; stopped safely.")
                }
                activeRun.recoverySlot = slot
                waitFor(activeRun, Phase.WAIT_RECOVERY, now)
            }

            Phase.WAIT_RECOVERY -> {
                val activeMenu = activeRun.menu ?: return abort(client, "Anvil menu was lost; stopped the merge.")
                val slot = activeRun.recoverySlot
                if (slot < 0) return abort(client, "Recovery state was lost; stopped safely.")
                if (!activeMenu.slots[slot].item.isEmpty) return

                activeRun.recoverySlot = -1
                activeRun.phase = Phase.RECOVER_INPUTS
                activeRun.notBefore = now + stepDelayNanos
                activeRun.deadline = now + stepTimeoutNanos
            }
        }
    }

    private fun isAnvilReady(menu: AbstractContainerMenu): Boolean =
        menu.slots[22].item.item == Items.ANVIL && menu.slots[RESULT_SLOT].item.item == Items.BARRIER

    private fun inputsAreEmpty(menu: AbstractContainerMenu): Boolean =
        menu.slots[29].item.isEmpty && menu.slots[33].item.isEmpty

    private fun inventorySideSlots(menu: AbstractContainerMenu, player: Player): List<Int> =
        menu.slots.indices.filter { menu.slots[it].container === player.inventory }

    private fun findPair(menu: AbstractContainerMenu, inventorySlots: List<Int>): BookPair? {
        val books = inventorySlots.mapNotNull { slotIndex ->
            val enchant = readEnchant(menu.slots[slotIndex].item) ?: return@mapNotNull null
            if (enchant.level >= (targetLevels[enchant.name] ?: 0)) return@mapNotNull null
            enchant to slotIndex
        }

        for ((name, maxLevel) in targetLevels) {
            for (level in 1 until maxLevel) {
                val matches = books.filter { it.first.name.equals(name, ignoreCase = true) && it.first.level == level }
                if (matches.size >= 2) return BookPair(matches[0].first, matches[0].second, matches[1].second)
            }
        }
        return null
    }

    private fun readEnchant(stack: ItemStack): EnchantBook? {
        if (stack.isEmpty || stack.item != Items.ENCHANTED_BOOK) return null
        val lore = stack.get(DataComponents.LORE)?.lines().orEmpty()
        for (line in lore) {
            val plainLine = line.string.replace(formattingCode, "").trim()
            for (name in targetLevels.keys) {
                val prefix = "$name "
                if (!plainLine.startsWith(prefix, ignoreCase = true)) continue
                val roman = plainLine.substring(prefix.length).trim().uppercase()
                val level = romanToInt[roman] ?: continue
                return EnchantBook(name, level)
            }
        }
        return null
    }

    private fun isBook(stack: ItemStack, expected: EnchantBook): Boolean = readEnchant(stack) == expected

    private fun isSign(stack: ItemStack): Boolean =
        !stack.isEmpty && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.item).path.endsWith("_sign")

    private fun click(client: Minecraft, player: Player, menu: AbstractContainerMenu, slot: Int, input: ContainerInput): Boolean =
        client.gameMode?.let {
            it.handleContainerInput(menu.containerId, slot, 0, input, player)
            true
        } ?: false

    private fun waitFor(activeRun: Run, phase: Phase, now: Long) {
        activeRun.phase = phase
        activeRun.notBefore = now + stepDelayNanos
        activeRun.deadline = now + stepTimeoutNanos
    }

    private fun beginRecovery(client: Minecraft, activeRun: Run, now: Long) {
        activeRun.recoveryAttempts++
        activeRun.pair = null
        activeRun.recoverySlot = -1
        activeRun.phase = Phase.RECOVER_INPUTS
        activeRun.notBefore = now + stepDelayNanos
        activeRun.deadline = now + stepTimeoutNanos
        client.gui.hud.getChat().addClientSystemMessage(
            Component.literal("Anvil state changed or lagged; returning input books and retrying.")
        )
    }

    private fun abort(client: Minecraft, message: String) {
        run = null
        client.gui.hud.getChat().addClientSystemMessage(Component.literal(message))
    }

    private fun finish(client: Minecraft, menu: AbstractContainerMenu, message: String) {
        run = null
        client.player?.connection?.send(ServerboundContainerClosePacket(menu.containerId))
        client.gui.setScreen(null)
        client.gui.hud.getChat().addClientSystemMessage(Component.literal(message))
    }
}
