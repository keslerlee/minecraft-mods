package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerDebugOverlayMixin {
    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void kesler$drawSlotIndex(
            GuiGraphicsExtractor graphics,
            Slot slot,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        if (!ConfigManager.get().containerDebugOverlayEnabled) return;

        String label = Integer.toString(slot.index);
        // extractContents has already translated the pose to the container's top-left.
        int x = slot.x + 1;
        int y = slot.y + 1;
        int width = Minecraft.getInstance().font.width(label) + 2;
        graphics.fill(x, y, x + width, y + 9, 0xC0000000);
        graphics.text(Minecraft.getInstance().font, label, x + 1, y, 0xFFFFFFFF);
    }
}
