package net.fishinghacks.utils.gui.mcsettings;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import net.fishinghacks.utils.gui.components.GuiDropdown;
import net.fishinghacks.utils.gui.components.Slider;
import net.fishinghacks.utils.gui.components.ConfigSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class VideoSettings implements OptionSubscreen {
    int oldMipmaps;
    private static final Component RESOLUTION_UNAVAILABLE = Component.translatable("options.fullscreen.unavailable");
    private static final Component RESOLUTION_CURRENT = Component.translatable("options.fullscreen.current");

    private static Component fromVideomode(VideoMode videoMode) {
        return Component.translatable("options.fullscreen.entry", videoMode.getWidth(), videoMode.getHeight(),
            videoMode.getRefreshRate(), videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits());
    }

    private static List<OptionInstance<?>> options(Options options) {
        return List.of(options.biomeBlendRadius(), options.graphicsMode(), options.renderDistance(),
            options.prioritizeChunkUpdates(), options.simulationDistance(), options.ambientOcclusion(),
            options.framerateLimit(), options.enableVsync(), options.inactivityFpsLimit(), options.guiScale(),
            options.attackIndicator(), options.gamma(), options.cloudStatus(), options.fullscreen(),
            options.particles(), options.mipmapLevels(), options.entityShadows(), options.screenEffectScale(),
            options.entityDistanceScaling(), options.fovEffectScale(), options.showAutosaveIndicator(),
            options.glintSpeed(), options.glintStrength(), options.menuBackgroundBlurriness(), options.bobView());
    }

    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        this.oldMipmaps = options.mipmapLevels().get();
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        Monitor monitor = window.findBestMonitor();
        int monitorMode = -1;
        if (monitor != null) {
            var preferredFullscreenVideoMode = window.getPreferredFullscreenVideoMode();
            Objects.requireNonNull(monitor);
            monitorMode = preferredFullscreenVideoMode.map(monitor::getVideoModeIndex).orElse(-1);
        }

        int modeCount = monitor != null ? monitor.getModeCount() : 0;
        List<Integer> monitorModes = new ArrayList<>();
        for (int i = -1; i < modeCount; ++i) monitorModes.add(i);
        var widget = new GuiDropdown<>(monitorMode, new MonitorDisplayer(monitor), (ignored, i) -> {
            if (monitor != null) {
                window.setPreferredFullscreenVideoMode(i == -1 ? Optional.empty() : Optional.of(monitor.getMode(i)));
                window.changeFullscreenVideoMode();
            }
        }, monitorModes);
        widget.active = monitor != null;
        widget.setWidth(Slider.DEFAULT_WIDTH);
        layout.addChild(
            new ConfigSection(Component.translatable("options.fullscreen.resolution"), widget, configWidth));

        for (var opt : options(options)) {
            layout.addChild(OptionUtils.configFromOptionInstance(opt, configWidth));
        }
    }

    public void onClose(Options options) {
        Minecraft mc = Minecraft.getInstance();
        if (options.mipmapLevels().get() != this.oldMipmaps) {
            mc.updateMaxMipLevel(options.mipmapLevels().get());
            mc.delayTextureReload();
        }
    }

    private record MonitorDisplayer(@Nullable Monitor monitor) implements Function<Integer, Component> {
        @Override
        public Component apply(Integer index) {
            if (monitor == null) return RESOLUTION_UNAVAILABLE;
            else if (index == -1) return RESOLUTION_CURRENT;
            else return fromVideomode(monitor.getMode(index));
        }
    }
}
