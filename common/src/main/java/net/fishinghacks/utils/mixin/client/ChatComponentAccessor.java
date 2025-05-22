package net.fishinghacks.utils.mixin.client;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {
    @Invoker("isChatHidden")
    boolean invokeIsChatHidden();

    @Invoker("getLinesPerPage")
    int invokeGetLinesPerPage();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getTrimmedMessages();

    @Accessor("allMessages")
    List<GuiMessage> getMessages();

    @Accessor("chatScrollbarPos")
    int getChatScrollbarPos();

    @Invoker("getScale")
    double invokeGetScale();

    @Invoker("screenToChatX")
    double invokeScreenToChatX(double x);

    @Invoker("screenToChatY")
    double invokeScreenToChatY(double y);

    @Invoker("getLineHeight")
    int invokeGetLineHeight();

    @Invoker("getWidth")
    int invokeGetWidth();

    @Invoker("getMessageEndIndexAt")
    int invokeGetMessageEndIndexAt(double mouseX, double mouseY);

    @Invoker("getTagIconLeft")
    int invokeGetTagIconLeft(GuiMessage.Line line);

    @Invoker("drawTagIcon")
    void invokeDrawTagIcon(GuiGraphics guiGraphics, int left, int bottom, GuiMessageTag.Icon tagIcon);

    @Accessor("newMessageSinceScroll")
    boolean getNewMessageSinceScroll();

    @Accessor("newMessageSinceScroll")
    void setNewMessageSinceScroll(boolean value);

    @Invoker("scrollChat")
    void invokedScrollChat(int amount);

    @Invoker("isChatFocused")
    boolean invokeIsChatFocused();
}
