package kesler.keslerclient.mixin;

import kesler.keslerclient.config.Config;
import kesler.keslerclient.config.ConfigManager;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    private static final int KESLER_EXTENDED_CHAT_LIMIT = 10000;

    @Unique private Component kesler$lastOriginalMessage;
    @Unique private GuiMessage kesler$lastMergedMessage;
    @Unique private int kesler$repeatCount;

    @Inject(method = "addMessage", at = @At("HEAD"), cancellable = true)
    private void kesler$mergeConsecutiveMessages(
            Component content,
            MessageSignature signature,
            GuiMessageSource source,
            GuiMessageTag tag,
            CallbackInfo ci
    ) {
        if (!ConfigManager.get().mergeConsecutiveChatEnabled) {
            kesler$clearMergeState();
            return;
        }

        ChatComponentAccessor accessor = (ChatComponentAccessor) this;
        List<GuiMessage> messages = accessor.kesler$getAllMessages();
        if (!messages.isEmpty()) {
            GuiMessage latest = messages.get(0);
            if (latest == kesler$lastMergedMessage
                    && kesler$lastOriginalMessage != null
                    && kesler$lastOriginalMessage.equals(content)
                    && latest.source() == source
                    && Objects.equals(latest.tag(), tag)) {
                kesler$repeatCount++;
                Component mergedContent = kesler$lastOriginalMessage.copy()
                        .append(Component.literal(" (x" + kesler$repeatCount + ")").withStyle(ChatFormatting.GRAY));
                GuiMessage merged = new GuiMessage(
                        Minecraft.getInstance().gui.hud.getGuiTicks(),
                        mergedContent,
                        signature,
                        source,
                        tag
                );
                messages.set(0, merged);
                accessor.kesler$refreshTrimmedMessages();
                kesler$lastMergedMessage = merged;
                ci.cancel();
                return;
            }
        }

        kesler$lastOriginalMessage = content;
        kesler$repeatCount = 1;
        kesler$lastMergedMessage = null;
    }

    @Inject(method = "addMessage", at = @At("RETURN"))
    private void kesler$rememberDisplayedMessage(
            Component content,
            MessageSignature signature,
            GuiMessageSource source,
            GuiMessageTag tag,
            CallbackInfo ci
    ) {
        if (!ConfigManager.get().mergeConsecutiveChatEnabled || kesler$lastOriginalMessage != content) return;

        List<GuiMessage> messages = ((ChatComponentAccessor) this).kesler$getAllMessages();
        if (messages.isEmpty()) return;
        GuiMessage latest = messages.get(0);
        if (latest.content().equals(content) && latest.source() == source && Objects.equals(latest.tag(), tag)) {
            kesler$lastMergedMessage = latest;
        }
    }

    @Unique
    private void kesler$clearMergeState() {
        kesler$lastOriginalMessage = null;
        kesler$lastMergedMessage = null;
        kesler$repeatCount = 0;
    }

    @ModifyConstant(method = "addMessageToDisplayQueue", constant = @Constant(intValue = 100))
    private int kesler$displayHistoryLimit(int vanillaLimit) {
        return ConfigManager.get().extendedChatHistoryEnabled ? KESLER_EXTENDED_CHAT_LIMIT : vanillaLimit;
    }

    @ModifyConstant(method = "addMessageToQueue", constant = @Constant(intValue = 100))
    private int kesler$messageHistoryLimit(int vanillaLimit) {
        return ConfigManager.get().extendedChatHistoryEnabled ? KESLER_EXTENDED_CHAT_LIMIT : vanillaLimit;
    }

    @Inject(method = "getHeight()I", at = @At("RETURN"), cancellable = true)
    private void kesler$height(CallbackInfoReturnable<Integer> cir) {
        Config cfg = ConfigManager.get();
        if (cfg.chatHeightEnabled && Minecraft.getInstance().gui.screen() instanceof ChatScreen) {
            cir.setReturnValue(cfg.chatHeight);
        }
    }
}
