package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.E4MCStore;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.PauseMenuScreen;
import net.fishinghacks.utils.gui.components.VanillaIconButton;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.fishinghacks.utils.platform.ClientServices;
import net.minecraft.client.Minecraft;
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

import java.util.function.Supplier;

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
        int posY = disconnectButton.getY() - 2 * Button.DEFAULT_HEIGHT - 2 * 4;
        int posX = width / 2 + 4 + 102;
        utils_mod_multiloader$inviteButton = addRenderableWidget(
            new VanillaIconButton(posX, posY, Icons.INVITE, PauseMenuScreen::invitePlayer, Supplier::get));
        utils_mod_multiloader$inviteButton.active = utils_mod_multiloader$inviteButton.visible = false;
        utils_mod_multiloader$cosmeticsButton = addRenderableWidget(
            new VanillaIconButton(width / 2 - 104 - VanillaIconButton.DEFAULT_WIDTH, posY, Icons.COSMETICS,
                ignored -> Minecraft.getInstance().setScreen(new CosmeticsScreen(Minecraft.getInstance().screen)),
                Supplier::get));
        if (!ClientServices.PLATFORM.hasModlistScreen()) addRenderableWidget(
            new VanillaIconButton(width / 2 - 104 - VanillaIconButton.DEFAULT_WIDTH,
                posY + 4 + VanillaIconButton.DEFAULT_HEIGHT, Icons.SETTINGS,
                ignored -> ConfigSectionScreen.open(Minecraft.getInstance(), Configs.clientConfig), Supplier::get));
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, int ignored0, int ignored1, float ignored2, CallbackInfo ci) {
        utils_mod_multiloader$inviteButton.active = utils_mod_multiloader$inviteButton.visible =
            ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();
        utils_mod_multiloader$cosmeticsButton.active = utils_mod_multiloader$cosmeticsButton.visible =
            ClientConnectionHandler.getInstance()
            .isConnected();
    }
}
