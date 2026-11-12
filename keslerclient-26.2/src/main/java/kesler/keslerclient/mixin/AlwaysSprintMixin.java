package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class AlwaysSprintMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void kesler$alwaysSprint(CallbackInfo ci) {
        if (!ConfigManager.get().alwaysSprintEnabled) return;

        ClientInput input = (ClientInput) (Object) this;
        Input current = input.keyPresses;
        if (!input.hasForwardImpulse() || current.shift() || current.sprint()) return;

        input.keyPresses = new Input(
                current.forward(),
                current.backward(),
                current.left(),
                current.right(),
                current.jump(),
                current.shift(),
                true
        );
    }
}
