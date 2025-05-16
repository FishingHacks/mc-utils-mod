package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.gui.MainScreen;
import net.fishinghacks.utils.gui.PauseMenuScreen;
import net.fishinghacks.utils.gui.mcsettings.McSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(@Nullable Screen guiScreen, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        boolean defaultScreen = Minecraft.getInstance().isDemo() || !Configs.clientConfig.CUSTOM_MENUS.get();
        if (defaultScreen && guiScreen instanceof MainScreen) {
            mc.setScreen(new TitleScreen());
            ci.cancel();
        }
        if (defaultScreen) return;
        if (guiScreen instanceof TitleScreen) {
            mc.setScreen(new MainScreen());
            ci.cancel();
        } else if (guiScreen instanceof OptionsScreen optionsScreen) {
            try {
                var lastScreenField = OptionsScreen.class.getDeclaredField("lastScreen");
                lastScreenField.setAccessible(true);
                Screen lastScreen = (Screen) lastScreenField.get(optionsScreen);
                lastScreenField.setAccessible(false);

                var optionsField = OptionsScreen.class.getDeclaredField("options");
                optionsField.setAccessible(true);
                Options options = (Options) optionsField.get(optionsScreen);
                optionsField.setAccessible(false);
                mc.setScreen(new McSettingsScreen(lastScreen, options));
                ci.cancel();
            } catch (Exception ignored) {
            }
        } else if (guiScreen instanceof PauseScreen pauseScreen) {
            mc.setScreen(new PauseMenuScreen(pauseScreen.showsPauseMenu()));
            ci.cancel();
        }
    }
}
