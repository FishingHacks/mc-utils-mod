package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.cosmetics.CosmeticModelLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        var self = (PlayerRenderer) (Object) this;
        self.addLayer(new CosmeticModelLayer(self));
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void extractRenderState(AbstractClientPlayer player, PlayerRenderState state, float v, CallbackInfo ci) {
        CosmeticModelLayer.lastPlayer = player.getGameProfile();
    }
}