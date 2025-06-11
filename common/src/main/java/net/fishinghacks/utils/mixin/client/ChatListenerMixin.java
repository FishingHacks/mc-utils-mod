package net.fishinghacks.utils.mixin.client;

import com.mojang.authlib.GameProfile;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.modules.misc.Chat;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.time.ZoneId;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @Inject(method = "showMessageToPlayer", at = @At("HEAD"), cancellable = true)
    public void showMessageToPlayer(ChatType.Bound boundChatType, PlayerChatMessage chatMessage,
                                    Component decoratedServerContent, GameProfile gameProfile,
                                    boolean onlyShowSecureChat, Instant timestamp, CallbackInfoReturnable<Boolean> ci) {
        try {

            var chatMod = Chat.instance;
            if (!chatMod.isEnabled() || !boundChatType.chatType().is(ChatType.CHAT)) return;
            var trustLevel = ((ChatListenerInvoker) this).invokeEvaluateTrustLevel(chatMessage, decoratedServerContent,
                timestamp);
            if ((onlyShowSecureChat && trustLevel.isNotSecure()) || Minecraft.getInstance()
                .isBlocked(chatMessage.sender()) || chatMessage.isFullyFiltered()) {
                ci.setReturnValue(false);
                return;
            }

            var localTime = java.time.LocalTime.ofInstant(timestamp, ZoneId.systemDefault());
            ci.setReturnValue(true);
            GuiMessageTag messageTag = trustLevel.createTag(chatMessage);
            var signature = chatMessage.signature();
            Component message;
            if (chatMod.CUSTOM_PLAYER_FORMAT.get()) {
                var prefixFormat = chatMod.PREFIX_FORMAT.get();
                var prefix = prefixFormat.replaceAll("%t", chatMod.TIME_FORMAT.get().format(localTime));
                var prefixes = prefix.split("%p");
                var msg = Component.literal(prefixes[0]);
                for (int i = 1; i < prefixes.length; ++i)
                    msg.append(boundChatType.name()).append(Component.literal(prefixes[i]));
                message = msg.append(" ").append(chatMessage.decoratedContent());
            } else if (chatMod.TIME_FORMAT.get() == Chat.TimeFormat.Disabled)
                message = boundChatType.decorate(chatMessage.decoratedContent());
            else message = Component.literal(
                        chatMod.PREFIX_FORMAT.get().replaceAll("%t", chatMod.TIME_FORMAT.get().format(localTime)))
                    .append(" ").append(boundChatType.decorate(chatMessage.decoratedContent()));

            Minecraft.getInstance().gui.getChat().addMessage(message, signature, messageTag);
            ((ChatListenerInvoker) this).invokeNarrateChatMessage(boundChatType, chatMessage.decoratedContent());
            ((ChatListenerInvoker) this).invokeLogPlayerMessage(chatMessage, boundChatType, gameProfile, trustLevel);
            ((ChatListenerInvoker) this).setPreviousMessageTime(Util.getMillis());
        } catch (Exception e) {
            Constants.LOG.info("Error", e);
        }
    }
}
