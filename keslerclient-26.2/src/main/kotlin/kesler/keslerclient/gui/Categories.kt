package kesler.keslerclient.gui

import kesler.keslerclient.feature.autoclicker.AutoClickerSection
import kesler.keslerclient.feature.chat.ChatSection
import kesler.keslerclient.feature.chat.CommandsSection
import kesler.keslerclient.feature.dungeonstopwatch.DungeonStopwatchSection
import kesler.keslerclient.feature.gathering.GatheringSection
import kesler.keslerclient.feature.keybindings.ControlsSection
import kesler.keslerclient.feature.secrets.SecretsSection
import kesler.keslerclient.feature.tooltip.TooltipSection
import kesler.keslerclient.feature.visuals.VisualsSection

object Categories {
    val all: List<ConfigSection> = listOf(
        ChatSection,
        ControlsSection,
        DungeonStopwatchSection,
        CommandsSection,
        TooltipSection,
        VisualsSection,
        SecretsSection,
        GatheringSection,
        AutoClickerSection
        // add future sections here
    )
}
