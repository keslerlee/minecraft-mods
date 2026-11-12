package kesler.keslerclient

import kesler.keslerclient.command.KeslerCommand
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.feature.autoclicker.AutoClicker
import kesler.keslerclient.feature.anvilmerge.AnvilBookMerger
import kesler.keslerclient.feature.chat.CustomCommands
import kesler.keslerclient.feature.dungeonstopwatch.DungeonStopwatch
import kesler.keslerclient.feature.firefreeze.FireFreezeAutomation
import kesler.keslerclient.feature.gathering.AutoGatherer
import kesler.keslerclient.feature.holdrightclick.HoldRightClick
import kesler.keslerclient.feature.keybindings.AlternateKeyBindings
import kesler.keslerclient.feature.secrets.SecretClicker
import kesler.keslerclient.gui.KeslerConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class KeslerClient : ClientModInitializer {
	override fun onInitializeClient() {
		ConfigManager.load()
		AlternateKeyBindings.init()
		KeslerCommand.register()
		CustomCommands.init()
		DungeonStopwatch.init()
		FireFreezeAutomation.init()
		SecretClicker.init()
		AutoGatherer.init()
		HoldRightClick.init()
		AutoClicker.init()
		AnvilBookMerger.init()

		// Reapply saved aliases once vanilla has finished initializing its key mappings.
		var refreshedKeyMappingsAfterStartup = false
		ClientTickEvents.START_CLIENT_TICK.register {
			if (!refreshedKeyMappingsAfterStartup) {
				AlternateKeyBindings.refreshMappings()
				refreshedKeyMappingsAfterStartup = true
			}
		}

		// Open on the next tick, otherwise the closing chat screen overrides it
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (KeslerCommand.openRequested) {
				KeslerCommand.openRequested = false
				client.gui.setScreen(KeslerConfigScreen.create(null))
			}
		}
	}
}
