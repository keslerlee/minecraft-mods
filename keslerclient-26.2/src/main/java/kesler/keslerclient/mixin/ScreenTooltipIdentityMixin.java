package kesler.keslerclient.mixin;

import kesler.keslerclient.feature.tooltip.TooltipScrollState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenTooltipIdentityMixin {
    @Inject(
            method = "getTooltipFromItem(Lnet/minecraft/client/Minecraft;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD")
    )
    private static void kesler$captureItemTooltipIdentity(
            Minecraft minecraft,
            ItemStack item,
            CallbackInfoReturnable<List<Component>> cir
    ) {
        TooltipScrollState.setNextItemIdentity(Integer.toHexString(System.identityHashCode(item)));
    }
}
