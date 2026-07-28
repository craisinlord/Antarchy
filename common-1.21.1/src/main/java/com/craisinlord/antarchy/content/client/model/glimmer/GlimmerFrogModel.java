package com.craisinlord.antarchy.content.client.model.glimmer;

import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Reuses vanilla Frog geometry ({@link FrogModel}/{@code ModelLayers.FROG}) for the Frog
 * Glimmer variant instead of an ill-fitting GeckoLib placeholder mesh, with a simplified
 * walk/idle animation (GlimmerEntity has no Frog-specific jump/tongue state).
 */
public class GlimmerFrogModel extends EntityModel<GlimmerEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public GlimmerFrogModel(ModelPart bakeRoot) {
        this.root = bakeRoot.getChild("root");
        ModelPart body = this.root.getChild("body");
        this.head = body.getChild("head");
        this.leftArm = body.getChild("left_arm");
        this.rightArm = body.getChild("right_arm");
        this.leftLeg = this.root.getChild("left_leg");
        this.rightLeg = this.root.getChild("right_leg");
    }

    @Override
    public void setupAnim(GlimmerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;
        this.leftLeg.xRot = legSwing;
        this.rightLeg.xRot = -legSwing;
        this.leftArm.xRot = -legSwing;
        this.rightArm.xRot = legSwing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
