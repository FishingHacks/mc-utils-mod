package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.E4MCStore;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.PauseMenuScreen;
import net.fishinghacks.utils.gui.components.VanillaIconButton;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    @Shadow
    private Button disconnectButton;
    private Button inviteButton;
    private Button cosmeticsButton;

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    public void createPauseMenu(CallbackInfo ci) {
        int posY = disconnectButton.getY() - 2 * Button.DEFAULT_HEIGHT - 2 * 4;
        int posX = width / 2 + 4 + 102;
        inviteButton = addRenderableWidget(
            new VanillaIconButton(posX, posY, Icons.INVITE, PauseMenuScreen::invitePlayer, Supplier::get));
        inviteButton.active = inviteButton.visible = false;
        cosmeticsButton = addRenderableWidget(
            new VanillaIconButton(width / 2 - 104 - VanillaIconButton.DEFAULT_WIDTH, posY, Icons.COSMETICS,
                ignored -> Minecraft.getInstance().setScreen(new CosmeticsScreen(Minecraft.getInstance().screen)),
                Supplier::get));
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, int ignored0, int ignored1, float ignored2, CallbackInfo ci) {
        inviteButton.active = inviteButton.visible = ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();
        cosmeticsButton.active = cosmeticsButton.visible = ClientConnectionHandler.getInstance().isConnected();
    }
}
