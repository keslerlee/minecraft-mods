package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(ScreenEffectRenderer.class)
public abstract class FireOverlayMixin {
    @ModifyConstant(
            method = "lambda$submitFire$0",
            constant = @Constant(floatValue = -0.3F)
    )
    private static float kesler$lowerFireOverlay(float vanillaOffset) {
        return ConfigManager.get().reduceFireOverlayEnabled ? -0.65F : vanillaOffset;
    }
}
