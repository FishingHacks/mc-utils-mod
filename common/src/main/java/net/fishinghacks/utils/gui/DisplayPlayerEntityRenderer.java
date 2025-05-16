package net.fishinghacks.utils.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fishinghacks.utils.cosmetics.CosmeticModelHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DisplayPlayerEntityRenderer extends LivingEntityRenderer<LivingEntity, PlayerRenderState, PlayerModel> {
    final EntityRendererProvider.Context context;
    boolean slim;
    final ElytraModel elytra;

    public DisplayPlayerEntityRenderer(EntityRendererProvider.Context context, boolean slim) {
        super(context, new PlayerModel(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim),
            0.5f);
        this.context = context;
        this.slim = slim;
        elytra = new ElytraModel(context.getModelSet().bakeLayer(ModelLayers.ELYTRA));
    }

    public void setSlim(boolean slim) {
        this.slim = slim;
        this.model = new PlayerModel(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
    }

    public void render(PlaceholderEntity entity, PoseStack pose, MultiBufferSource bufferSource, int light) {
        setModelPose();
        pose.pushPose();

        pose.scale(0.9375f, 0.9375f, 0.9375f);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - entity.yaw));
        pose.scale(-1f, -1f, 1f);
        pose.translate(0f, -1.501f, 0f);

        if (entity.showBody) {
            RenderType renderType = this.model.renderType(entity.getSkinTexture());
            final var vertexConsumer = bufferSource.getBuffer(renderType);
            final var overlay = OverlayTexture.pack(OverlayTexture.u(0f), OverlayTexture.v(false));
            model.renderToBuffer(pose, vertexConsumer, light, overlay);

            var handler = CosmeticModelHandler.fromProfile(entity.gameProfile);
            for (var model : handler.models) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(model.texture()));
                model.render(pose, consumer, light, overlay, this.model);
            }
        }

        if (!entity.showElytra) {
            if (entity.getCapeTexture() != null) {
                pose.pushPose();

                pose.mulPose(Axis.XP.rotationDegrees(6.0f));
                final var vertexConsumer = bufferSource.getBuffer(
                    RenderType.armorCutoutNoCull(entity.getCapeTexture()));
                context.bakeLayer(ModelLayers.PLAYER_CAPE).getChild("body").getChild("cape")
                    .render(pose, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
        } else {
            final var identifier = entity.getElytraTexture();
            pose.pushPose();
            pose.translate(0.0f, 0.0f, 0.125f);

            final var vertexConsumer = ItemRenderer.getArmorFoilBuffer(bufferSource,
                RenderType.armorCutoutNoCull(identifier), false);
            this.elytra.renderToBuffer(pose, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        pose.popPose();
    }

    private void setModelPose() {
        final var options = Minecraft.getInstance().options;
        final var model = this.getModel();
        model.setAllVisible(true);
        model.hat.visible = options.isModelPartEnabled(PlayerModelPart.HAT);
        model.jacket.visible = options.isModelPartEnabled(PlayerModelPart.JACKET);
        model.leftPants.visible = options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);

    }

    @Override
    public ResourceLocation getTextureLocation(PlayerRenderState playerRenderState) {
        return playerRenderState.skin.texture();
    }

    @Override
    public PlayerRenderState createRenderState() {
        return new PlayerRenderState();
    }
}
