package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.VanillaIconButton;
import net.fishinghacks.utils.gui.components.VanillaIconTextButton;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.fishinghacks.utils.gui.screenshots.ScreenshotsScreen;
import net.fishinghacks.utils.modules.ClickUi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Unique
    AbstractWidget utils_mod_multiloader$cosmeticButton;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        utils_mod_multiloader$cosmeticButton.active = ClientConnectionHandler.getInstance().isConnected();
    }

    @Inject(method = "createNormalMenuOptions", at = @At("HEAD"))
    public void createNormalMenuOptions(int y, int rowHeight, CallbackInfoReturnable<Integer> ci) {
        int x = this.width / 2 - 100;

        if (x + 300 + Button.DEFAULT_WIDTH + 5 < width) utils_mod_multiloader$buildSidebarBig(x, y);
        else utils_mod_multiloader$buildSidebarSmall(y);
    }

    @Unique
    private void utils_mod_multiloader$buildSidebarSmall(int y) {
        assert minecraft != null;
        int x = width - 5 - Button.CUBE_WIDTH;

        y += addRenderableWidget(new VanillaIconButton(x, y, Icons.MACROS)).getHeight() + 4;
        y += addRenderableWidget(new VanillaIconButton(x, y, Icons.SCREENSHOTS,
            ignored -> minecraft.setScreen(new ScreenshotsScreen(this)))).getHeight() + 4;
        utils_mod_multiloader$cosmeticButton = addRenderableWidget(
            new VanillaIconButton(x, y, Icons.COSMETICS, ignored -> minecraft.setScreen(new CosmeticsScreen(this))));
        y += utils_mod_multiloader$cosmeticButton.getHeight() + 4;
        y += addRenderableWidget(new VanillaIconButton(x, y, Icons.SETTINGS,
            ignored -> minecraft.setScreen(new ConfigSectionScreen(this)))).getHeight() + 4;
        addRenderableWidget(
            new VanillaIconButton(x, y, Icons.MODULES, ignored -> minecraft.setScreen(new ClickUi(this)))).getHeight();
    }

    @Unique
    private void utils_mod_multiloader$buildSidebarBig(int x, int y) {
        x += 300;
        assert minecraft != null;
        y += addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.MACROS, Translation.MainGuiButtonMacros.get())).getHeight() + 4;
        y += addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.SCREENSHOTS, Translation.ScreenshotGuiTitle.get(),
                ignored -> minecraft.setScreen(new ScreenshotsScreen(this)))).getHeight() + 4;
        utils_mod_multiloader$cosmeticButton = addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.COSMETICS, Translation.CosmeticGuiTitle.get(),
                ignored -> minecraft.setScreen(new CosmeticsScreen(this))));
        y += utils_mod_multiloader$cosmeticButton.getHeight() + 4;
        y += addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.SETTINGS, Translation.MainGuiButtonSettings.get(),
                ignored -> minecraft.setScreen(new ConfigSectionScreen(this)))).getHeight() + 4;
        addRenderableWidget(new VanillaIconTextButton(x, y, Icons.MODULES, Translation.MainGuiButtonModules.get(),
            ignored -> minecraft.setScreen(new ClickUi(this)))).getHeight();
    }
}
