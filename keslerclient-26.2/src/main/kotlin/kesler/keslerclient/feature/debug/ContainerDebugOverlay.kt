package kesler.keslerclient.feature.debug

import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.mixin.ClientTextTooltipAccessor
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

object ContainerDebugOverlay {
    @JvmStatic
    fun indexTooltipLines(components: List<ClientTooltipComponent>): List<ClientTooltipComponent> {
        if (!ConfigManager.get().containerDebugOverlayEnabled || components.isEmpty()) return components

        val indexed = ArrayList<ClientTooltipComponent>(components.size)
        var sawTitle = false
        var loreIndex = 0
        var changed = false

        for (component in components) {
            if (component !is ClientTextTooltip) {
                indexed.add(component)
                continue
            }

            val text = (component as ClientTextTooltipAccessor).text
            if (!sawTitle) {
                sawTitle = true
                indexed.add(component)
                continue
            }

            val blank = StringBuilder()
            text.accept { _, _, codePoint ->
                blank.appendCodePoint(codePoint)
                true
            }
            val line = blank.toString()
            if (line.isBlank()) {
                indexed.add(component)
                loreIndex++
                continue
            }

            val prefix = FormattedCharSequence.forward("[$loreIndex] ", Style.EMPTY)
            indexed.add(ClientTextTooltip(FormattedCharSequence.composite(prefix, text)))
            loreIndex++
            changed = true
        }

        return if (changed) indexed else components
    }
}
