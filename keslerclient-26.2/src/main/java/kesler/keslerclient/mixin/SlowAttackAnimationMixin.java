package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SlowAttackAnimationMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void kesler$slowLocalAttackAnimation(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof LocalPlayer && ConfigManager.get().slowAttackAnimationEnabled) {
            cir.setReturnValue(Math.max(1, cir.getReturnValue() * 20));
        }
    }
}
