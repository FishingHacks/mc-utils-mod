package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.modules.misc.Chat;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Mutable
    @Shadow
    private static int MAX_CHAT_HISTORY = 100;

    @Unique
    private static double utils_mod_multiloader$getTimeFactor(int counter) {
        double d0 = (double) counter / (double) 200.0F;
        d0 = (double) 1.0F - d0;
        d0 *= 10;
        d0 = Mth.clamp(d0, 0, 1);
        return d0 * d0;
    }

    @Inject(method = "render", cancellable = true, at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused,
                       CallbackInfo ci) {
        var chatMod = Chat.instance;
        MAX_CHAT_HISTORY = chatMod.enabled ? chatMod.INFINITE_HISTORY.get() ? Integer.MAX_VALUE :
            chatMod.HISTORY_LENGTH.get() : 100;
        if (!chatMod.enabled) return;
        ci.cancel();
        var self = ((ChatComponentAccessor) this);
        if (self.invokeIsChatHidden()) return;
        var mc = Minecraft.getInstance();
        int linesPerPage = self.invokeGetLinesPerPage();
        int trimmedMessages = self.getTrimmedMessages().size();
        if (trimmedMessages > 0) {
            ProfilerFiller profilerfiller = Profiler.get();
            profilerfiller.push("chat");
            float scale = (float) self.invokeGetScale();
            int width = Mth.ceil((float) self.invokeGetWidth() / scale);
            int height = guiGraphics.guiHeight();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.pose().translate(4.0F, 0.0F, 0.0F);
            int scaledHeight = Mth.floor((float) (height - 40) / scale);
            int lastMessageIndex = self.invokeGetMessageEndIndexAt(self.invokeScreenToChatX(mouseX),
                self.invokeScreenToChatY(mouseY));
            double chatOpacity = (double) chatMod.BACKGROUND.get().a() / 255.0;
            double backgroundOpacity = mc.options.textBackgroundOpacity().get();
            double lineSpacing = mc.options.chatLineSpacing().get();
            int lineHeight = self.invokeGetLineHeight();
            int messageHeight = (int) Math.round(
                (double) -8.0F * (lineSpacing + (double) 1.0F) + (double) 4.0F * lineSpacing);
            int lineIndex = 0;

            for (int relativeMessageIndex = 0; relativeMessageIndex + self.getChatScrollbarPos() < self.getTrimmedMessages()
                .size() && relativeMessageIndex < linesPerPage; ++relativeMessageIndex) {
                int messageIndex = relativeMessageIndex + self.getChatScrollbarPos();
                GuiMessage.Line line = self.getTrimmedMessages().get(messageIndex);
                if (line == null) continue;
                int messageAge = tickCount - line.addedTime();
                if (!focused && messageAge >= 200) continue;
                double ageOpacity = focused ? (double) 1.0F : utils_mod_multiloader$getTimeFactor(messageAge);
                int chatAlpha = (int) ((double) 255.0F * ageOpacity * chatOpacity);
                int backgroundAlpha = (int) ((double) 255.0F * ageOpacity * backgroundOpacity);
                int background = chatMod.BACKGROUND.get().withAlpha(backgroundAlpha).argb();
                int foreground = chatMod.FOREGROUND.get().withAlpha(chatAlpha).argb();
                ++lineIndex;
                if (chatAlpha <= 3) continue;
                int messageTop = scaledHeight - relativeMessageIndex * lineHeight;
                int messageY = messageTop + messageHeight;
                guiGraphics.fill(-4, messageTop - lineHeight, width + 8, messageTop, background);
                GuiMessageTag guimessagetag = line.tag();
                if (guimessagetag != null) {
                    int k4 = guimessagetag.indicatorColor() | chatAlpha << 24;
                    guiGraphics.fill(-4, messageTop - lineHeight, -2, messageTop, k4);
                    if (messageIndex == lastMessageIndex && guimessagetag.icon() != null) {
                        int iconLeft = self.invokeGetTagIconLeft(line);
                        int iconBottom = messageY + 9;
                        self.invokeDrawTagIcon(guiGraphics, iconLeft, iconBottom, guimessagetag.icon());
                    }
                }

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                guiGraphics.drawString(mc.font, line.content(), 0, messageY, foreground);
                guiGraphics.pose().popPose();
            }

            long j5 = mc.getChatListener().queueSize();
            if (j5 > 0L) {
                int k5 = (int) ((double) 128.0F * chatOpacity);
                int i6 = (int) ((double) 255.0F * backgroundOpacity);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, (float) scaledHeight, 0.0F);
                guiGraphics.fill(-2, 0, width + 4, 9, i6 << 24);
                guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                guiGraphics.drawString(mc.font, Component.translatable("chat.queue", j5), 0, 1, 16777215 + (k5 << 24));
                guiGraphics.pose().popPose();
            }

            if (focused) {
                int l5 = self.invokeGetLineHeight();
                int j6 = trimmedMessages * l5;
                int k6 = lineIndex * l5;
                int i3 = self.getChatScrollbarPos() * k6 / trimmedMessages - scaledHeight;
                int l6 = k6 * k6 / j6;
                if (j6 != k6) {
                    int i7 = i3 > 0 ? 170 : 96;
                    int j7 = self.getNewMessageSinceScroll() ? 13382451 : 3355562;
                    int k7 = width + 4;
                    guiGraphics.fill(k7, -i3, k7 + 2, -i3 - l6, 100, j7 + (i7 << 24));
                    guiGraphics.fill(k7 + 2, -i3, k7 + 1, -i3 - l6, 100, 13421772 + (i7 << 24));
                }
            }

            guiGraphics.pose().popPose();
            profilerfiller.pop();
        }
    }

    @Inject(method = "addMessageToQueue", at = @At("HEAD"), cancellable = true)
    private void addMessageToQueue(GuiMessage message, CallbackInfo ci) {
        var chatMod = Chat.instance;
        if (!chatMod.enabled) return;
        ci.cancel();
        var allMessages = ((ChatComponentAccessor) this).getMessages();
        allMessages.add(message);

        if (chatMod.INFINITE_HISTORY.get()) return;
        while (allMessages.size() > chatMod.HISTORY_LENGTH.get()) allMessages.removeLast();
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("HEAD"), cancellable = true)
    private void addMessageToDisplayQueue(GuiMessage message, CallbackInfo ci) {
        var chatMod = Chat.instance;
        if (!chatMod.enabled) return;
        ci.cancel();
        var self = ((ChatComponentAccessor) this);

        int i = Mth.floor((double) self.invokeGetWidth() / self.invokeGetScale());
        GuiMessageTag.Icon guimessagetag$icon = message.icon();
        if (guimessagetag$icon != null) {
            i -= guimessagetag$icon.width + 4 + 2;
        }

        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(message.content(), i,
            Minecraft.getInstance().font);
        boolean flag = self.invokeIsChatFocused();

        var messages = self.getTrimmedMessages();
        for (int j = 0; j < list.size(); ++j) {
            FormattedCharSequence formattedcharsequence = list.get(j);
            if (flag && self.getChatScrollbarPos() > 0) {
                self.setNewMessageSinceScroll(true);
                self.invokedScrollChat(1);
            }

            boolean flag1 = j == list.size() - 1;
            messages.addFirst(new GuiMessage.Line(message.addedTime(), formattedcharsequence, message.tag(), flag1));
        }

        if (chatMod.INFINITE_HISTORY.get()) return;
        while (messages.size() > chatMod.HISTORY_LENGTH.get()) messages.removeLast();
    }
}
