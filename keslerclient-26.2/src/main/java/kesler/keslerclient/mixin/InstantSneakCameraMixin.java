package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class InstantSneakCameraMixin {
    @Shadow private Entity entity;
    @Shadow private Vec3 position;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "alignWithEntity(F)V", at = @At("TAIL"))
    private void kesler$instantSneakCamera(float partialTick, CallbackInfo ci) {
        if (!ConfigManager.get().instantSneakAnimationEnabled
                || !(entity instanceof LocalPlayer)
                || entity.isPassenger()) return;

        Pose targetPose = entity.getPose();
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() == null && (targetPose == Pose.STANDING || targetPose == Pose.CROUCHING)) {
            targetPose = client.options.keyShift.isDown() ? Pose.CROUCHING : Pose.STANDING;
        }

        float targetEyeHeight = entity.getEyeHeight(targetPose);
        float currentEyeHeight = Mth.lerp(partialTick, eyeHeightOld, eyeHeight);
        double correction = targetEyeHeight - currentEyeHeight;
        setPosition(position.x, position.y + correction, position.z);
    }
}
