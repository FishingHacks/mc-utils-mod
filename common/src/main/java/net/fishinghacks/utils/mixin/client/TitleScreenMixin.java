package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.components.VanillaIconButton;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.fishinghacks.utils.platform.ClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Unique
    VanillaIconButton utils_mod_multiloader$cosmeticButton;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        utils_mod_multiloader$cosmeticButton.visible = utils_mod_multiloader$cosmeticButton.active =
            ClientConnectionHandler.getInstance()
            .isConnected();
    }

    @Inject(method = "createNormalMenuOptions", at = @At("RETURN"))
    public void createNormalMenuOptions(int ignored0, int rowHeight, CallbackInfoReturnable<Integer> ci) {
        int y = ci.getReturnValue();
        int x = this.width / 2 - 104 - VanillaIconButton.DEFAULT_WIDTH;
        utils_mod_multiloader$cosmeticButton = addRenderableWidget(
            new VanillaIconButton(x, ClientServices.PLATFORM.hasModlistScreen() ? y + rowHeight : y, Icons.COSMETICS,
                ignored -> Minecraft.getInstance().setScreen(new CosmeticsScreen(this)), Supplier::get));
        x = this.width / 2 + 104;
        assert minecraft != null;
        if (!ClientServices.PLATFORM.hasModlistScreen()) addRenderableWidget(
            new VanillaIconButton(x, y, Icons.SETTINGS,
                ignored -> ConfigSectionScreen.open(minecraft, Configs.clientConfig), Supplier::get));
    }
}
