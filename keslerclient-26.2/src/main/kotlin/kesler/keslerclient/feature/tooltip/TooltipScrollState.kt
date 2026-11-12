package kesler.keslerclient.feature.tooltip

import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.mixin.ClientTextTooltipAccessor
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

object TooltipScrollState {
    private const val TOOLTIP_FRAME_PADDING = 24

    private var signature: String? = null
    private var scrollOffset = 0
    private var maxScrollOffset = 0
    private var tooltipActive = false
    private var forwardingTooltip = false
    private var nextItemIdentity: String? = null
    private var frameItemIdentity: String? = null

    @JvmStatic
    fun beginFrame() {
        tooltipActive = false
        frameItemIdentity = nextItemIdentity
        nextItemIdentity = null
    }

    @JvmStatic
    fun setNextItemIdentity(identity: String) {
        nextItemIdentity = identity
    }

    @JvmStatic
    fun prepare(
        font: Font,
        components: List<ClientTooltipComponent>,
        guiHeight: Int
    ): List<ClientTooltipComponent> {
        val itemIdentity = frameItemIdentity
        frameItemIdentity = null

        if (!ConfigManager.get().tooltipScrollingEnabled || components.isEmpty()) {
            tooltipActive = false
            return components
        }

        val heights = components.map { it.getHeight(font) }
        val viewportHeight = (guiHeight - TOOLTIP_FRAME_PADDING).coerceAtLeast(1)
        if (heights.sum() <= viewportHeight) {
            tooltipActive = false
            signature = null
            scrollOffset = 0
            maxScrollOffset = 0
            return components
        }

        val nextSignature = itemIdentity?.let { "item:$it" } ?: buildString {
            components.forEachIndexed { index, component ->
                append(component.javaClass.name).append(',')
                append(component.getWidth(font)).append('x').append(heights[index]).append(';')
                if (component is ClientTextTooltip) {
                    val text = (component as ClientTextTooltipAccessor).text
                    text.accept { _, _, codePoint ->
                        appendCodePoint(codePoint)
                        true
                    }
                    append('|')
                }
            }
        }
        if (nextSignature != signature) {
            signature = nextSignature
            scrollOffset = 0
        }

        maxScrollOffset = findLastPage(heights, viewportHeight)
        scrollOffset = scrollOffset.coerceIn(0, maxScrollOffset)
        tooltipActive = true

        var end = scrollOffset
        var usedHeight = 0
        while (end < components.size) {
            val nextHeight = heights[end]
            if (usedHeight + nextHeight > viewportHeight && end > scrollOffset) break
            usedHeight += nextHeight
            end++
        }
        val visible = ArrayList(components.subList(scrollOffset, end))
        val paddingHeight = viewportHeight - usedHeight
        if (paddingHeight > 0) visible.add(TooltipPaddingComponent(paddingHeight))
        return visible
    }

    @JvmStatic
    fun scroll(vertical: Double): Boolean {
        if (!tooltipActive || maxScrollOffset <= 0 || vertical == 0.0) return false

        scrollOffset = if (vertical > 0.0) {
            (scrollOffset - 1).coerceAtLeast(0)
        } else {
            (scrollOffset + 1).coerceAtMost(maxScrollOffset)
        }
        return true
    }

    @JvmStatic
    fun isForwardingTooltip(): Boolean = forwardingTooltip

    @JvmStatic
    fun setForwardingTooltip(forwarding: Boolean) {
        forwardingTooltip = forwarding
    }

    private fun findLastPage(heights: List<Int>, viewportHeight: Int): Int {
        for (start in heights.indices) {
            if (heights.subList(start, heights.size).sum() <= viewportHeight) return start
        }
        return (heights.size - 1).coerceAtLeast(0)
    }
}

private class TooltipPaddingComponent(private val height: Int) : ClientTooltipComponent {
    override fun getHeight(font: Font): Int = height

    override fun getWidth(font: Font): Int = 0
}
