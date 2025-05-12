package net.fishinghacks.utils.client.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record CosmeticModel(CosmeticModelPart root, ResourceLocation texture) {
    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                       PlayerModel playerModel) {
        this.root.resetPose();
        this.root.offsetPos(new Vector3f(playerModel.head.x, playerModel.head.y, playerModel.head.z));
        this.root.offsetRotation(new Vector3f(playerModel.head.xRot, playerModel.head.yRot, playerModel.head.zRot));
        this.root.render(poseStack, buffer, packedLight, packedOverlay, -1);
    }
}
