package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.Icons;
import net.fishinghacks.utils.client.gui.components.VanillaIconButton;
import net.fishinghacks.utils.client.gui.cosmetics.CosmeticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    VanillaIconButton cosmeticButton;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        cosmeticButton.visible = cosmeticButton.active = ClientConnectionHandler.getInstance().isConnected();
    }

    @Inject(method = "createNormalMenuOptions", at = @At("RETURN"))
    public void createNormalMenuOptions(int ignored0, int rowHeight, CallbackInfoReturnable<Integer> ci) {
        int y = ci.getReturnValue();
        int x = this.width / 2 - 100 - 4 - VanillaIconButton.DEFAULT_WIDTH;
        cosmeticButton = addRenderableWidget(new VanillaIconButton(x, y + rowHeight, Icons.COSMETICS,
            ignored -> Minecraft.getInstance().setScreen(new CosmeticsScreen(this)), Supplier::get));
    }
}
