package kesler.keslerclient.mixin;

import kesler.keslerclient.feature.tooltip.TooltipScrollState;
import kesler.keslerclient.feature.debug.ContainerDebugOverlay;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Vector2i;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class TooltipScrollMixin {
    @Inject(method = "extractDeferredElements(IIF)V", at = @At("HEAD"))
    private void kesler$beginTooltipFrame(int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TooltipScrollState.beginFrame();
    }

    @Inject(
            method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kesler$scrollTooltip(
            Font font,
            List<ClientTooltipComponent> components,
            int mouseX,
            int mouseY,
            ClientTooltipPositioner positioner,
            Identifier background,
            CallbackInfo ci
    ) {
        if (TooltipScrollState.isForwardingTooltip()) return;

        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        List<ClientTooltipComponent> indexed = ContainerDebugOverlay.indexTooltipLines(components);
        List<ClientTooltipComponent> visible = TooltipScrollState.prepare(font, indexed, graphics.guiHeight());
        if (visible == components) return;

        boolean scrollingChangedTooltip = visible != indexed;
        ClientTooltipPositioner renderPositioner = positioner;
        if (scrollingChangedTooltip) {
            renderPositioner = (screenWidth, screenHeight, x, y, width, height) -> {
                int tooltipX = positioner.positionTooltip(screenWidth, screenHeight, x, y, width, height).x();
                return new Vector2i(tooltipX, 12);
            };
        }

        TooltipScrollState.setForwardingTooltip(true);
        try {
            graphics.tooltip(font, visible, mouseX, mouseY, renderPositioner, background);
        } finally {
            TooltipScrollState.setForwardingTooltip(false);
        }
        ci.cancel();
    }
}
