package kesler.keslerclient.mixin;

import kesler.keslerclient.config.ConfigManager;
import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenCopyMixin {
    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
    private void kesler$copyChatLine(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigManager.get().copyChatOnRightClickEnabled || event.button() != 1) return;

        Minecraft minecraft = Minecraft.getInstance();
        ChatComponent chat = minecraft.gui.hud.getChat();
        ChatComponentAccessor access = (ChatComponentAccessor) chat;
        double scale = access.kesler$getScale();
        if (scale <= 0 || event.x() < 0 || event.x() > access.kesler$getChatWidth() * scale) return;

        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int chatBottom = Mth.floor((screenHeight - 40) / scale);
        int entryHeight = (int) Math.round(9.0 * (1.0 + minecraft.options.chatLineSpacing().get()));
        int row = Mth.floor((chatBottom - event.y() / scale) / entryHeight);
        int scroll = access.kesler$getChatScrollbarPos();
        List<GuiMessage.Line> lines = access.kesler$getTrimmedMessages();
        int visibleCount = Math.min(lines.size() - scroll, chat.getLinesPerPage());
        // row 0 is the bottom-most visible row, and trimmedMessages stores newest first.
        int lineIndex = row + scroll;
        if (row < 0 || row >= visibleCount || lineIndex < 0 || lineIndex >= lines.size()) return;

        String message = lines.get(lineIndex).parent().content().getString()
                .replaceAll("(?i)§[0-9A-FK-ORX]", "");
        if (message.isEmpty()) return;
        minecraft.keyboardHandler.setClipboard(message);
        chat.addClientSystemMessage(Component.literal("Copied chat message to clipboard."));
        cir.setReturnValue(true);
    }
}
