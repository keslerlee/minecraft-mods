package kesler.keslerclient.mixin;

import kesler.keslerclient.feature.tooltip.TooltipScrollState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
    private void kesler$scrollTooltip(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (TooltipScrollState.scroll(vertical)) ci.cancel();
    }
}
