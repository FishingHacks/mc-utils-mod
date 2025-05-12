package net.fishinghacks.utils.client.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.PartPose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class CosmeticModelPart {
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float xScale = 1.0F;
    public float yScale = 1.0F;
    public float zScale = 1.0F;
    private final List<CosmeticModelLoader.Cube> cubes;
    private final Map<String, CosmeticModelPart> children;

    private PartPose initialPose;

    public CosmeticModelPart(List<CosmeticModelLoader.Cube> cubes, Map<String, CosmeticModelPart> children) {
        this.cubes = cubes;
        this.children = children;
    }


    public void setInitialPose(PartPose initialPose) {
        this.initialPose = initialPose;
    }

    public void resetPose() {
        this.loadPose(this.initialPose);
        for (var child : this.children.values()) child.resetPose();
    }

    public void loadPose(PartPose partPose) {
        this.x = partPose.x();
        this.y = partPose.y();
        this.z = partPose.z();
        this.xRot = partPose.xRot();
        this.yRot = partPose.yRot();
        this.zRot = partPose.zRot();
        this.xScale = partPose.xScale();
        this.yScale = partPose.yScale();
        this.zScale = partPose.zScale();
    }


    public void rotateAndTranslate(PoseStack poseStack) {
        poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
            poseStack.mulPose((new Quaternionf()).rotationZYX(this.zRot, this.yRot, this.xRot));
        }

        if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
            poseStack.scale(this.xScale, this.yScale, this.zScale);
        }

    }

    public void offsetPos(Vector3f offset) {
        this.x += offset.x();
        this.y += offset.y();
        this.z += offset.z();
    }

    public void offsetRotation(Vector3f offset) {
        this.xRot += offset.x();
        this.yRot += offset.y();
        this.zRot += offset.z();
    }


    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (this.cubes.isEmpty() && this.children.isEmpty()) return;
        poseStack.pushPose();
        rotateAndTranslate(poseStack);
        for (var cube : cubes) cube.render(poseStack.last(), buffer, packedLight, packedOverlay, color);

        for (var child : children.values()) child.render(poseStack, buffer, packedLight, packedOverlay, color);

        poseStack.popPose();
    }
}
