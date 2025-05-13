package net.fishinghacks.utils.client.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record CosmeticModel(CosmeticModelPart root, ResourceLocation texture, String id) {
    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                       PlayerModel playerModel) {
        this.root.resetPose();
        var attachTo = switch (root.attachment) {
            case Head -> playerModel.head;
            case LeftArm -> playerModel.leftArm;
            case RightArm -> playerModel.rightArm;
            case LeftLeg -> playerModel.leftLeg;
            case RightLeg -> playerModel.rightLeg;
            case Body -> playerModel.body;
        };
        this.root.offsetPos(new Vector3f(attachTo.x, attachTo.y, attachTo.z));
        this.root.offsetRotation(new Vector3f(attachTo.xRot, attachTo.yRot, attachTo.zRot));
        this.root.render(poseStack, buffer, packedLight, packedOverlay, -1);
    }
}
