package net.fishinghacks.utils.cosmetics;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.NotNull;

public class CosmeticModelLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    public static GameProfile lastPlayer;

    public CosmeticModelLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int packedLight,
                       @NotNull PlayerRenderState playerRenderState, float v, float v1) {
        var handler = CosmeticModelHandler.fromProfile(lastPlayer);
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(playerRenderState, 0f);
        for (var model : handler.models) {
            VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(model.texture()));
            model.render(poseStack, consumer, packedLight, packedOverlay, getParentModel());
        }
    }
}
