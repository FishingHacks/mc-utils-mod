package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.cosmetics.CosmeticModelLayer;
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
        ((PlayerRenderer) (Object) this).addLayer(new CosmeticModelLayer((PlayerRenderer) (Object) this));
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void extractRenderState(AbstractClientPlayer player, PlayerRenderState state, float v, CallbackInfo ci) {
        CosmeticModelLayer.lastPlayer = player.getGameProfile();
    }
}
