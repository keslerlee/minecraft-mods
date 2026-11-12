package kesler.keslerclient.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingMapAccessor {
    @Accessor("MAP")
    static Map<InputConstants.Key, List<KeyMapping>> getMap() {
        throw new AssertionError();
    }
}
