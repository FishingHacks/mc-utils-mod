package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.E4MCStore;
import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.Icons;
import net.fishinghacks.utils.client.gui.PauseMenuScreen;
import net.fishinghacks.utils.client.gui.components.VanillaIconButton;
import net.fishinghacks.utils.client.gui.cosmetics.CosmeticsScreen;
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
        addRenderableWidget(new VanillaIconButton(width / 2 - 104 - VanillaIconButton.DEFAULT_WIDTH, posY, Icons.INVITE,
            ignored -> Minecraft.getInstance().setScreen(new CosmeticsScreen(Minecraft.getInstance().screen)),
            Supplier::get));
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, int ignored0, int ignored1, float ignored2, CallbackInfo ci) {
        inviteButton.active = inviteButton.visible = ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();
    }
}
