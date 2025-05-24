package net.fishinghacks.utils.gui.screenshots;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.mixin.client.NativeImageAccessor;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.*;
import java.nio.ByteBuffer;

// Source: https://github.com/comp500/ScreenshotToClipboard/blob/1.20-arch/common/src/main/java/link/infra/screenshotclipboard/common/MacOSCompat.java
// Source: https://github.com/comp500/ScreenshotToClipboard/blob/1.20-arch/common/src/main/java/link/infra/screenshotclipboard/common/ScreenshotToClipboard.java
public class ScreenshotCopier {
    public static boolean onMac() {
        return Util.getPlatform() == Util.OS.OSX;
    }

    // macOS requires some ugly hacks to get it to work, because it doesn't allow GLFW and AWT to load at the same time
    // See: https://github.com/MinecraftForge/MinecraftForge/pull/5591#issuecomment-470805491
    // Thanks to @juliand665 for writing and testing most of this code, I don't have a Mac!

    public static void doCopyMacOS(String path) {
        if (!onMac()) {
            return;
        }

        Client client = Client.getInstance();
        Proxy url = client.sendProxy("NSURL", "fileURLWithPath:", path);

        Proxy image = client.sendProxy("NSImage", "alloc");
        image.send("initWithContentsOfURL:", url);

        Proxy array = client.sendProxy("NSArray", "array");
        array = array.sendProxy("arrayByAddingObject:", image);

        Proxy pasteboard = client.sendProxy("NSPasteboard", "generalPasteboard");
        pasteboard.send("clearContents");
        boolean wasSuccessful = pasteboard.sendBoolean("writeObjects:", array);
        if (!wasSuccessful) {
            Constants.LOG.error("Failed to write image to pasteboard!");
        }
    }

    public void init() {
        if (onMac()) return;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
            Constants.LOG.info("java.awt.headless property was not set properly!");
        }
    }

    public static void handleScreenshotAWT(NativeImage img) {
        if (onMac()) return;

        // Only allow RGBA
        if (img.format() != NativeImage.Format.RGBA) {
            Constants.LOG.warn("Failed to capture screenshot: wrong format");
            return;
        }

        //noinspection ConstantConditions
        long imagePointer = ((NativeImageAccessor) (Object) img).getPointer();
        ByteBuffer buf = MemoryUtil.memByteBufferSafe(imagePointer, img.getWidth() * img.getHeight() * 4);
        if (buf == null) {
            throw new RuntimeException("Invalid image");
        }

        byte[] array;
        if (buf.hasArray()) {
            array = buf.array();
        } else {
            // can't use .array() as the buffer is not array-backed
            array = new byte[img.getWidth() * img.getHeight() * 4];
            buf.get(array);
        }

        doCopy(array, img.getWidth(), img.getHeight());
    }

    private static void doCopy(byte[] imageData, int width, int height) {
        new Thread(() -> {
            DataBufferByte buf = new DataBufferByte(imageData, imageData.length);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            // Ignore the alpha channel, due to JDK-8204187
            int[] nBits = {8, 8, 8};
            int[] bOffs = {0, 1, 2}; // is this efficient, no transformation is being done?
            ColorModel cm = new ComponentColorModel(cs, nBits, false, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
            BufferedImage bufImg = new BufferedImage(cm,
                Raster.createInterleavedRaster(buf, width, height, width * 4, 4, bOffs, null), false,
                null);

            Transferable trans = getTransferableImage(bufImg);
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(trans, null);
        }, "Screenshot to Clipboard Copy").start();
    }

    private static Transferable getTransferableImage(final BufferedImage bufferedImage) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            @Override
            public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (DataFlavor.imageFlavor.equals(flavor)) {
                    return bufferedImage;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }
}
