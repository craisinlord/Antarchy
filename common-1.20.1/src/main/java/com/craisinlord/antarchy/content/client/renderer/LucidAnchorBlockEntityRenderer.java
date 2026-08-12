package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class LucidAnchorBlockEntityRenderer implements BlockEntityRenderer<LucidAnchorBlockEntity> {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("textures/entity/beacon_beam.png");
    private static final float INNER_RADIUS = 0.14F;
    private static final float OUTER_RADIUS = 0.28F;
    private static final float START_OFFSET = -0.02F;

    public LucidAnchorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LucidAnchorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!blockEntity.isActive() || blockEntity.getBeamLength() <= 0 || blockEntity.getBeamSections().isEmpty() || blockEntity.getLevel() == null) {
            return;
        }

        long gameTime = blockEntity.getLevel().getGameTime();
        float scroll = (gameTime + partialTick) * 0.02F;
        float startY = START_OFFSET;
        float endY = START_OFFSET - blockEntity.getBeamLength();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);

        float currentTop = startY;
        for (LucidAnchorBlockEntity.BeamSection section : blockEntity.getBeamSections()) {
            float sectionBottom = currentTop - section.length();
            renderBeamPrism(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE)), currentTop, sectionBottom, INNER_RADIUS, section.color(), 1.0F, scroll);
            renderBeamPrism(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE)), currentTop, sectionBottom, OUTER_RADIUS, darken(section.color()), 0.45F, -scroll);
            currentTop = sectionBottom;
        }

        if (currentTop > endY) {
            renderBeamPrism(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE)), currentTop, endY, INNER_RADIUS, 0xFFFF1A1A, 1.0F, scroll);
            renderBeamPrism(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE)), currentTop, endY, OUTER_RADIUS, 0xFF7A0000, 0.45F, -scroll);
        }

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static void renderBeamPrism(PoseStack poseStack, VertexConsumer consumer, float topY, float bottomY, float radius, int color, float alphaScale, float scroll) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >>> 24) & 0xFF) / 255.0F * alphaScale;
        float vTop = scroll + topY * 0.25F;
        float vBottom = scroll + bottomY * 0.25F;

        addFace(consumer, matrix, normal, -radius, topY, -radius, radius, topY, -radius, radius, bottomY, -radius, -radius, bottomY, -radius, red, green, blue, alpha, vTop, vBottom);
        addFace(consumer, matrix, normal, radius, topY, radius, -radius, topY, radius, -radius, bottomY, radius, radius, bottomY, radius, red, green, blue, alpha, vTop, vBottom);
        addFace(consumer, matrix, normal, -radius, topY, radius, -radius, topY, -radius, -radius, bottomY, -radius, -radius, bottomY, radius, red, green, blue, alpha, vTop, vBottom);
        addFace(consumer, matrix, normal, radius, topY, -radius, radius, topY, radius, radius, bottomY, radius, radius, bottomY, -radius, red, green, blue, alpha, vTop, vBottom);
    }

    private static void addFace(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float red,
            float green,
            float blue,
            float alpha,
            float vTop,
            float vBottom
    ) {
        addVertex(consumer, matrix, normal, x0, y0, z0, red, green, blue, alpha, 0.0F, vTop);
        addVertex(consumer, matrix, normal, x1, y1, z1, red, green, blue, alpha, 1.0F, vTop);
        addVertex(consumer, matrix, normal, x2, y2, z2, red, green, blue, alpha, 1.0F, vBottom);
        addVertex(consumer, matrix, normal, x3, y3, z3, red, green, blue, alpha, 0.0F, vBottom);

        addVertex(consumer, matrix, normal, x3, y3, z3, red, green, blue, alpha, 0.0F, vBottom);
        addVertex(consumer, matrix, normal, x2, y2, z2, red, green, blue, alpha, 1.0F, vBottom);
        addVertex(consumer, matrix, normal, x1, y1, z1, red, green, blue, alpha, 1.0F, vTop);
        addVertex(consumer, matrix, normal, x0, y0, z0, red, green, blue, alpha, 0.0F, vTop);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static int darken(int color) {
        int red = (int) (((color >> 16) & 0xFF) * 0.45F);
        int green = (int) (((color >> 8) & 0xFF) * 0.2F);
        int blue = (int) ((color & 0xFF) * 0.2F);
        return (color & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }
}
