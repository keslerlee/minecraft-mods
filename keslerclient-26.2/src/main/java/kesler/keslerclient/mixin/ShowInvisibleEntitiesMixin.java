package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class ShowInvisibleEntitiesMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    private void kesler$showInvisibleEntities(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (ConfigManager.get().showInvisibleEntitiesEnabled && state.isInvisible) {
            // Keep the model opaque to avoid water sorting artifacts; retain the invisibility flag as a tint marker.
            state.isInvisible = false;
            state.isInvisibleToPlayer = true;
            state.outlineColor = 0;
        }
    }

    @Inject(method = "getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I", at = @At("HEAD"), cancellable = true)
    private void kesler$tintInvisibleEntity(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (ConfigManager.get().showInvisibleEntitiesEnabled && state.isInvisibleToPlayer) {
            cir.setReturnValue(0xFFBDE7FF);
        }
    }
}
