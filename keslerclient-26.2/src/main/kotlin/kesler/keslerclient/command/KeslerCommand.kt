package kesler.keslerclient.command

import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.feature.secrets.SecretClicker
import kesler.keslerclient.feature.anvilmerge.AnvilBookMerger
import kesler.keslerclient.feature.holdrightclick.HoldRightClick
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object KeslerCommand {
    @JvmStatic
    var openRequested = false

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            for (name in listOf("kesler", "k")) {
                dispatcher.register(
                    ClientCommands.literal(name)
                    .executes {
                        openRequested = true
                        1
                    }
                    .then(ClientCommands.literal("merge").executes {
                        AnvilBookMerger.start(it.source)
                        1
                    })
                    .then(ClientCommands.literal("rightclick").executes { context ->
                        val enabled = HoldRightClick.toggle(Minecraft.getInstance())
                        context.source.sendFeedback(Component.literal(
                            if (enabled) "Holding right click. Open any screen to stop."
                            else "Stopped holding right click."
                        ))
                        1
                    })
                    .then(
                        ClientCommands.literal("secrets")
                            .then(ClientCommands.literal("setwither").executes { setSkull(it.source, wither = true) })
                            .then(ClientCommands.literal("setredstone").executes { setSkull(it.source, wither = false) })
                    )
                )
            }
        }

    }

    private fun setSkull(source: FabricClientCommandSource, wither: Boolean): Int {
        val id = SecretClicker.lookedAtSkullUuid(Minecraft.getInstance())
        if (id == null) {
            source.sendFeedback(Component.literal("Look at a player head first."))
            return 0
        }
        val cfg = ConfigManager.get()
        if (wither) cfg.witherSkullUuid = id.toString() else cfg.redstoneSkullUuid = id.toString()
        ConfigManager.save()
        source.sendFeedback(Component.literal("Saved ${if (wither) "wither" else "redstone"} skull UUID: $id"))
        return 1
    }
}
