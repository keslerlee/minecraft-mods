package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class FullbrightMixin {
    @Shadow private boolean needsUpdate;
    @Unique private boolean kesler$lastFullbright;
    @Unique private boolean kesler$initialized;

    @Inject(method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V", at = @At("HEAD"))
    private void kesler$refreshOnToggle(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        boolean enabled = ConfigManager.get().fullbrightEnabled;
        if (!kesler$initialized || enabled != kesler$lastFullbright) needsUpdate = true;
        kesler$lastFullbright = enabled;
        kesler$initialized = true;
    }

    @Inject(method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V", at = @At("RETURN"))
    private void kesler$fullbright(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (ConfigManager.get().fullbrightEnabled) state.brightness = 1000.0F;
    }
}
