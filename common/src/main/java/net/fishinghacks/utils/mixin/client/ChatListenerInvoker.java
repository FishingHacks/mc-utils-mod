package net.fishinghacks.utils.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.time.Instant;

@Mixin(ChatListener.class)
public interface ChatListenerInvoker {
    @Invoker("evaluateTrustLevel")
    ChatTrustLevel invokeEvaluateTrustLevel(PlayerChatMessage message, Component decoratedServerContent, Instant timestamp);

    @Invoker("narrateChatMessage")
    void invokeNarrateChatMessage(@NotNull ChatType.Bound boundChatType, Component message);

    @Invoker("logPlayerMessage")
    void invokeLogPlayerMessage(PlayerChatMessage message, ChatType.Bound boundChatType, GameProfile gameProfile,
                                ChatTrustLevel trustLevel);

    @Accessor("previousMessageTime")
    void setPreviousMessageTime(long time);
}
