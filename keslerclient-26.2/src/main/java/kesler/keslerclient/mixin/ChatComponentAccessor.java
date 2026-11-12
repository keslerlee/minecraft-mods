package kesler.keslerclient.mixin;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {
    @Accessor("allMessages")
    List<GuiMessage> kesler$getAllMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> kesler$getTrimmedMessages();

    @Accessor("chatScrollbarPos")
    int kesler$getChatScrollbarPos();

    @Invoker("getScale")
    double kesler$getScale();

    @Invoker("getWidth")
    int kesler$getChatWidth();

    @Invoker("refreshTrimmedMessages")
    void kesler$refreshTrimmedMessages();

}
