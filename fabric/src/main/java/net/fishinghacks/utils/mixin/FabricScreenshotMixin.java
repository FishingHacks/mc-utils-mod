package net.fishinghacks.utils.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.gui.screenshots.ScreenshotsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;

@Mixin(Screenshot.class)
public class FabricScreenshotMixin {
    @Redirect(method = "method_22691", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;"
        + "writeToFile(Ljava/io/File;)V"))
    private static void writeToFile(NativeImage instance, File file) throws IOException {
        instance.writeToFile(file);
        Minecraft.getInstance().schedule(() -> {
            if (Minecraft.getInstance().screen instanceof ScreenshotsScreen s) s.refreshScreenshots();
        });
    }

}
