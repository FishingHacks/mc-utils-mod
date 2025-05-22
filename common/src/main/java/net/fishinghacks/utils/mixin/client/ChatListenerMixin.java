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
            if (!chatMod.enabled || !boundChatType.chatType().is(ChatType.CHAT)) return;
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
            if (chatMod.CUSTOM_PLAYER_FORMAT.get() || chatMessage.unsignedContent() == null) {
                var time = chatMod.TIME_FORMAT.get().format(localTime);
                var name = gameProfile.getName();
                var prefix = chatMod.PREFIX_FORMAT.get().replaceAll("%t", time).replaceAll("%p", name) + " ";
                message = Component.literal(prefix).append(Component.literal(chatMessage.signedContent()));
            } else if (chatMod.TIME_FORMAT.get() == Chat.TimeFormat.Disabled) message = chatMessage.unsignedContent();
            else {
                var time = chatMod.TIME_FORMAT.get().format(localTime);
                message = Component.literal(chatMod.PREFIX_FORMAT.get().replaceAll("%t", time)).append(" ")
                    .append(chatMessage.unsignedContent());
            }

            Minecraft.getInstance().gui.getChat().addMessage(message, signature, messageTag);
            ((ChatListenerInvoker) this).invokeNarrateChatMessage(boundChatType, chatMessage.decoratedContent());
            ((ChatListenerInvoker) this).invokeLogPlayerMessage(chatMessage, boundChatType, gameProfile, trustLevel);
            ((ChatListenerInvoker) this).setPreviousMessageTime(Util.getMillis());
        } catch (Exception e) {
            Constants.LOG.info("Error", e);
        }
    }
}
