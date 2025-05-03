package net.fishinghacks.utils.client.gui.mcsettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;

import java.util.List;

public class Accessibility implements OptionSubscreen {
    private static List<OptionInstance<?>> options(Options options) {
        return List.of(options.narrator(), options.showSubtitles(), options.highContrast(), options.autoJump(),
            options.menuBackgroundBlurriness(), options.textBackgroundOpacity(), options.backgroundForChatOnly(),
            options.chatOpacity(), options.chatLineSpacing(), options.chatDelay(), options.notificationDisplayTime(),
            options.bobView(), options.toggleCrouch(), options.toggleSprint(), options.screenEffectScale(),
            options.fovEffectScale(), options.darknessEffectScale(), options.damageTiltStrength(), options.glintSpeed(),
            options.glintStrength(), options.hideLightningFlash(), options.darkMojangStudiosBackground(),
            options.panoramaSpeed(), options.hideSplashTexts(), options.narratorHotkey(), options.rotateWithMinecart(),
            options.highContrastBlockOutline());
    }

    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        Minecraft mc = Minecraft.getInstance();

        OptionInstance<?> highContrast = options.highContrast();
        OptionInstance<?> rotateWithMinecart = options.rotateWithMinecart();
        OptionInstance<?> narrator = options.narrator();
        boolean highContrastUnavailable = mc.getResourcePackRepository().getAvailableIds().contains("high_contrast");
        boolean rotateWithMinecartAvailable = mc.level != null && mc.level.enabledFeatures()
            .contains(FeatureFlags.MINECART_IMPROVEMENTS);
        boolean narratorAvailable = mc.getNarrator().isActive();

        for (var opt : options(options)) {
            var widget = layout.addChild(OptionUtils.configFromOptionInstance(opt, configWidth));

            if (opt == rotateWithMinecart) widget.getWidget().active = rotateWithMinecartAvailable;
            else if (highContrastUnavailable && opt == highContrast) {
                widget.getWidget().active = false;
                widget.getWidget().setTooltip(
                    Tooltip.create(Component.translatable("options.accessibility.high_contrast.error.tooltip")));
            } else if (opt == narrator) widget.getWidget().active = narratorAvailable;
        }
    }
}
