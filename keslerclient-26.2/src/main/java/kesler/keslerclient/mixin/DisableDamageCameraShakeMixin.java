package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public abstract class DisableDamageCameraShakeMixin {
    @Redirect(
            method = "bobHurt",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;hurtTime:F"
            )
    )
    private float kesler$removeDamageCameraShake(CameraEntityRenderState state) {
        return ConfigManager.get().disableDamageCameraShakeEnabled ? -1.0F : state.hurtTime;
    }
}
