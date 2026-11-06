package com.craisinlord.antarchy.content.client.model.glimmer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GlimmerFrogModel extends EntityModel<GlimmerFrogModel.State> {
    private static final float TONGUE_LENGTH_BLOCKS = 7.0F / 16.0F;
    private static final float MOUTH_OPEN_RADIANS = 0.6F;

    private final ModelPart head;
    private final ModelPart tongue;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public GlimmerFrogModel(ModelPart bakeRoot) {
        super(bakeRoot.getChild("root"));
        ModelPart body = this.root.getChild("body");
        this.head = body.getChild("head");
        this.tongue = body.getChild("tongue");
        this.leftArm = body.getChild("left_arm");
        this.rightArm = body.getChild("right_arm");
        this.leftLeg = this.root.getChild("left_leg");
        this.rightLeg = this.root.getChild("right_leg");
    }

    @Override
    public void setupAnim(State state) {
        this.head.yRot = state.netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = state.headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.sin(state.limbSwing * 0.6662F) * state.limbSwingAmount;
        this.leftLeg.xRot = legSwing;
        this.rightLeg.xRot = -legSwing;
        this.leftArm.xRot = -legSwing;
        this.rightArm.xRot = legSwing;

        this.tongue.xRot = 0.0F;
        this.tongue.zScale = 1.0F;
        if (state.hasTongueTarget) {
            float reach = state.tongueTargetDistance / Math.max(0.1F, state.growth);
            this.tongue.zScale = Math.max(1.0F, reach / TONGUE_LENGTH_BLOCKS);
            this.tongue.xRot = state.tongueTargetPitch;
            this.head.yRot = 0.0F;
            this.head.xRot = -MOUTH_OPEN_RADIANS;
        }
    }

    public static class State extends EntityRenderState {
        public float limbSwing;
        public float limbSwingAmount;
        public float netHeadYaw;
        public float headPitch;
        public float bodyYaw;
        public float growth = 1.0F;
        public int color = -1;
        public Identifier texture;
        public Identifier emissiveTexture;
        public boolean hasTongueTarget;
        public float tongueTargetDistance;
        public float tongueTargetPitch;
    }
}
