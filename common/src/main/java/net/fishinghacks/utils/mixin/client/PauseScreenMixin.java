package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.E4MCStore;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.MacrosScreen;
import net.fishinghacks.utils.gui.PauseMenuScreen;
import net.fishinghacks.utils.gui.components.VanillaIconButton;
import net.fishinghacks.utils.gui.components.VanillaIconTextButton;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.fishinghacks.utils.gui.screenshots.ScreenshotsScreen;
import net.fishinghacks.utils.modules.ClickUi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    @Shadow
    private Button disconnectButton;
    @Unique
    private Button utils_mod_multiloader$inviteButton;
    @Unique
    private Button utils_mod_multiloader$cosmeticsButton;

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    public void createPauseMenu(CallbackInfo ci) {
        int y = disconnectButton.getY() - 6 * Button.DEFAULT_HEIGHT - 2 * 6;
        int x = width / 2;

        if (x + 300 + net.fishinghacks.utils.gui.components.Button.DEFAULT_WIDTH + 5 < width)
            utils_mod_multiloader$buildSidebarBig(x, y);
        else utils_mod_multiloader$buildSidebarSmall(y);

    }

    @Unique
    private void utils_mod_multiloader$buildSidebarSmall(int y) {
        assert minecraft != null;
        int x = width - 5 - net.fishinghacks.utils.gui.components.Button.CUBE_WIDTH;

        utils_mod_multiloader$inviteButton = addRenderableWidget(
            new VanillaIconButton(x, y, Icons.INVITE, PauseMenuScreen::invitePlayer));
        y += utils_mod_multiloader$inviteButton.getHeight() + 4;
        y += addRenderableWidget(
            new VanillaIconButton(x, y, Icons.MACROS, ignored -> MacrosScreen.open())).getHeight() + 4;
        y += addRenderableWidget(new VanillaIconButton(x, y, Icons.SCREENSHOTS,
            ignored -> minecraft.setScreen(new ScreenshotsScreen(this)))).getHeight() + 4;
        utils_mod_multiloader$cosmeticsButton = addRenderableWidget(
            new VanillaIconButton(x, y, Icons.COSMETICS, ignored -> minecraft.setScreen(new CosmeticsScreen(this))));
        y += utils_mod_multiloader$cosmeticsButton.getHeight() + 4;
        y += addRenderableWidget(new VanillaIconButton(x, y, Icons.SETTINGS,
            ignored -> minecraft.setScreen(new ConfigSectionScreen(this)))).getHeight() + 4;
        addRenderableWidget(
            new VanillaIconButton(x, y, Icons.MODULES, ignored -> minecraft.setScreen(new ClickUi(this)))).getHeight();
    }

    @Unique
    private void utils_mod_multiloader$buildSidebarBig(int x, int y) {
        x += 300;
        assert minecraft != null;

        utils_mod_multiloader$inviteButton = addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.INVITE, Translation.Invite.get(), PauseMenuScreen::invitePlayer));
        y += utils_mod_multiloader$inviteButton.getHeight() + 4;
        y += addRenderableWidget(new VanillaIconTextButton(x, y, Icons.MACROS, Translation.MainGuiButtonMacros.get(),
            ignored -> MacrosScreen.open())).getHeight() + 4;
        y += addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.SCREENSHOTS, Translation.ScreenshotGuiTitle.get(),
                ignored -> minecraft.setScreen(new ScreenshotsScreen(this)))).getHeight() + 4;
        utils_mod_multiloader$cosmeticsButton = addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.COSMETICS, Translation.CosmeticGuiTitle.get(),
                ignored -> minecraft.setScreen(new CosmeticsScreen(this))));
        y += utils_mod_multiloader$cosmeticsButton.getHeight() + 4;
        y += addRenderableWidget(
            new VanillaIconTextButton(x, y, Icons.SETTINGS, Translation.MainGuiButtonSettings.get(),
                ignored -> minecraft.setScreen(new ConfigSectionScreen(this)))).getHeight() + 4;
        addRenderableWidget(new VanillaIconTextButton(x, y, Icons.MODULES, Translation.MainGuiButtonModules.get(),
            ignored -> minecraft.setScreen(new ClickUi(this)))).getHeight();
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, int ignored0, int ignored1, float ignored2, CallbackInfo ci) {
        utils_mod_multiloader$inviteButton.active = utils_mod_multiloader$inviteButton.visible =
            ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();
        utils_mod_multiloader$cosmeticsButton.active = ClientConnectionHandler.getInstance().isConnected();
    }
}
