package com.craisinlord.antarchy.content.client.model.glimmer;

import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class GlimmerFrogModel extends EntityModel<GlimmerEntity> {
    private static final float TONGUE_LENGTH_BLOCKS = 7.0F / 16.0F;
    private static final float MOUTH_OPEN_RADIANS = 0.6F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tongue;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public GlimmerFrogModel(ModelPart bakeRoot) {
        this.root = bakeRoot.getChild("root");
        ModelPart body = this.root.getChild("body");
        this.head = body.getChild("head");
        this.tongue = body.getChild("tongue");
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

        this.tongue.xRot = 0.0F;
        this.tongue.zScale = 1.0F;
        Entity target = entity.getAbilityTarget();
        if (target != null) {
            Vec3 mouth = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(mouth);
            float reach = (float) toTarget.length() / Math.max(0.1F, entity.getGrowthScale());
            this.tongue.zScale = Math.max(1.0F, reach / TONGUE_LENGTH_BLOCKS);
            this.tongue.xRot = (float) -Mth.atan2(toTarget.y, toTarget.horizontalDistance());
            this.head.yRot = 0.0F;
            this.head.xRot = -MOUTH_OPEN_RADIANS;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
