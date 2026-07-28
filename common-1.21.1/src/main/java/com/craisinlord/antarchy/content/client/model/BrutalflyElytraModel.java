package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class BrutalflyElytraModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "brutalfly_elytra"), "main");

    private final ModelPart root;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public BrutalflyElytraModel(ModelPart root) {
        this.root = root;
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        root.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create().texOffs(22, 0).addBox(-10.0F, 0.0F, -1.0F, 10.0F, 20.0F, 2.0F),
                PartPose.offset(5.0F, 0.0F, -1.5F)
        );
        root.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create().mirror().texOffs(22, 0).addBox(0.0F, 0.0F, -1.0F, 10.0F, 20.0F, 2.0F),
                PartPose.offset(-5.0F, 0.0F, -1.5F)
        );
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public void setupAnimation(float progress, float strength, boolean crouching) {
        float wave = Mth.sin(progress * Mth.PI);
        float flap = wave * strength;
        float basePitch = crouching ? -0.18F : -0.05F;
        float wingSpread = 0.55F;
        this.leftWing.xRot = basePitch - flap * 1.2F;
        this.rightWing.xRot = basePitch - flap * 1.2F;
        this.leftWing.yRot = -wingSpread;
        this.rightWing.yRot = -wingSpread;
        this.leftWing.zRot = -0.35F - flap * 0.7F;
        this.rightWing.zRot = -0.35F - flap * 0.7F;
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int overlay) {
        this.root.render(poseStack, consumer, packedLight, overlay);
    }
}
