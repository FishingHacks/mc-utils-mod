package net.fishinghacks.utils.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Source: https://github.com/comp500/ScreenshotToClipboard/blob/1.20-arch/common/src/main/java/link/infra/screenshotclipboard/common/mixin/NativeImagePointerAccessor.java
@Mixin(NativeImage.class)
public interface NativeImageAccessor {
    @Accessor("pixels")
    long getPointer();
}
