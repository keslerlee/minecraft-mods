package kesler.keslerclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class SmallerHeldItemsMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void kesler$shrinkHeldItem(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        if (!ConfigManager.get().smallerHeldItemsEnabled) return;
        poseStack.pushPose();
        poseStack.scale(0.50F, 0.50F, 0.50F);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void kesler$restoreHeldItemScale(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        if (ConfigManager.get().smallerHeldItemsEnabled) poseStack.popPose();
    }
}
