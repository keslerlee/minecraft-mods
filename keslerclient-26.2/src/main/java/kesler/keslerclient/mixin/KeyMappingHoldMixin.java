package kesler.keslerclient.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import kesler.keslerclient.feature.keybindings.AlternateKeyBindings;
import kesler.keslerclient.feature.holdrightclick.HoldRightClick;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class KeyMappingHoldMixin {
    @Inject(method = "set", at = @At("HEAD"))
    private static void kesler$trackAlternateKeyState(InputConstants.Key key, boolean down, CallbackInfo ci) {
        AlternateKeyBindings.onKeyMappingStateChanged(key, down);
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void kesler$ignoreAlternateKeyRepeats(InputConstants.Key key, CallbackInfo ci) {
        if (!AlternateKeyBindings.shouldAllowKeyClick(key)) ci.cancel();
    }

    @Inject(method = "releaseAll", at = @At("TAIL"))
    private static void kesler$clearAlternateKeyStates(CallbackInfo ci) {
        AlternateKeyBindings.clearSecondaryKeyStates();
    }

    @Inject(method = "setAll", at = @At("TAIL"))
    private static void kesler$applyAlternateKeys(CallbackInfo ci) {
        AlternateKeyBindings.applySecondaryKeyStates();
    }

    @Inject(method = "resetMapping", at = @At("TAIL"))
    private static void kesler$refreshAlternateKeyMappings(CallbackInfo ci) {
        AlternateKeyBindings.onVanillaMappingsReset();
    }

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void kesler$holdUseKey(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if ((Object) this == client.options.keyUse && HoldRightClick.isHolding() && client.gui.screen() == null) {
            cir.setReturnValue(true);
        }
    }
}
