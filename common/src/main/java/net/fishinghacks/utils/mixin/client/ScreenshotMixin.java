package net.fishinghacks.utils.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.gui.components.Notification;
import net.fishinghacks.utils.gui.screenshots.ScreenshotsScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    @Redirect(method = "lambda$grab$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform" +
        "/NativeImage;writeToFile(Ljava/io/File;)V"))
    private static void writeToFile(NativeImage instance, File file) throws IOException {
        instance.writeToFile(file);
        Minecraft.getInstance().schedule(() -> {
            if (Minecraft.getInstance().screen instanceof ScreenshotsScreen s) s.refreshScreenshots();
        });
    }

    @Inject(method = "grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"), cancellable = true)
    private static void changeMessageConsumer(File gameDirectory, RenderTarget buffer,
                                              Consumer<Component> messageConsumer, CallbackInfo ci) {
        Constants.LOG.info("Modifying argument");
        ci.cancel();
        Screenshot.grab(gameDirectory, null, buffer, component -> {
            if (component.getContents() instanceof TranslatableContents t && "screenshot.success".equals(t.getKey())) {
                Component comp = (Component) t.getArgs()[0];
                File file = comp.getStyle()
                    .getClickEvent() instanceof ClickEvent.OpenFile openFile ? openFile.file() : null;

                if (!(comp instanceof MutableComponent)) comp = comp.copy();
                comp = ((MutableComponent) comp).withStyle(style -> Style.EMPTY);
                t.getArgs()[0] = comp;
                if (file != null) {
                    GuiOverlayManager.addNotification(component, new Notification.NotifyButton(Translation.Open.get(),
                        ignored -> Util.getPlatform().openFile(file)));
                    return;
                }
            }
            GuiOverlayManager.addNotification(component);
        });
    }
}
