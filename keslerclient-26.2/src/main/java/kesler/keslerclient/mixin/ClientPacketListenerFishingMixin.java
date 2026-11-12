package kesler.keslerclient.mixin;

import kesler.keslerclient.feature.gathering.AutoFishing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerFishingMixin {
    @Shadow
    public abstract ClientLevel getLevel();

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void kesler$checkFishingBiteCue(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        ClientLevel level = getLevel();
        Entity entity = level.getEntity(packet.id());
        if (entity instanceof ArmorStand armorStand) {
            AutoFishing.onArmorStandUpdated(level, armorStand);
        }
    }
}
