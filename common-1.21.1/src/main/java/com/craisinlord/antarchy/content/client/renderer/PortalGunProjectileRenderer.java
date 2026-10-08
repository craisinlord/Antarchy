package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class PortalGunProjectileRenderer extends EntityRenderer<PortalGunProjectileEntity> {
    private static final ResourceLocation SHOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_shot.png");
    private static final float CORE_SIZE = 0.16F;
    private static final float GLOW_SIZE = 0.34F;
    private static final float STREAK_WIDTH = 0.045F;
    private static final double STREAK_LENGTH = 2.2D;
    private static final float MUZZLE_TICKS = 4.0F;
    private static final double MUZZLE_FORWARD = 0.55D;
    private static final double MUZZLE_SIDE = 0.32D;
    private static final double MUZZLE_DOWN = 0.28D;
    private static final double HIDDEN_DISTANCE = 0.9D;
    private static final double FADE_DISTANCE = 1.8D;

    public PortalGunProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PortalGunProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 entityPos = entity.getPosition(partialTick);
        Vec3 renderPos = this.muzzleBlendedPosition(entity, entityPos, partialTick);
        Vec3 cameraPos = this.entityRenderDispatcher.camera.getPosition();
        double cameraDistance = renderPos.distanceTo(cameraPos);
        if (cameraDistance < HIDDEN_DISTANCE) {
            return;
        }
        float fade = (float) Mth.clamp((cameraDistance - HIDDEN_DISTANCE) / (FADE_DISTANCE - HIDDEN_DISTANCE), 0.0D, 1.0D);
        int color = entity.getVariant().color(entity.getPortalSide());
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        poseStack.pushPose();
        Vec3 offset = renderPos.subtract(entityPos);
        poseStack.translate(offset.x, offset.y, offset.z);
        this.renderStreak(entity, renderPos, cameraPos, poseStack, bufferSource, red, green, blue, fade, partialTick);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer glow = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(SHOT_TEXTURE));
        addQuad(glow, pose, matrix, red, green, blue, 0.55F * fade, GLOW_SIZE);
        addQuad(glow, pose, matrix, 1.0F, 1.0F, 1.0F, 0.9F * fade, CORE_SIZE);
        poseStack.popPose();
    }

    private void renderStreak(PortalGunProjectileEntity entity, Vec3 renderPos, Vec3 cameraPos, PoseStack poseStack, MultiBufferSource bufferSource,
                              float red, float green, float blue, float fade, float partialTick) {
        Vec3 velocity = entity.getSyncedVelocity();
        if (velocity.lengthSqr() < 1.0E-6D) {
            velocity = entity.getDeltaMovement();
        }
        if (velocity.lengthSqr() < 1.0E-6D) {
            return;
        }
        Vec3 direction = velocity.normalize();
        Vec3 toCamera = cameraPos.subtract(renderPos);
        Vec3 side = direction.cross(toCamera);
        if (side.lengthSqr() < 1.0E-6D) {
            return;
        }
        side = side.normalize().scale(STREAK_WIDTH);
        double travelled = entity.getSyncedSpawnPosition().distanceTo(renderPos);
        double length = Math.min(STREAK_LENGTH, Math.max(0.0D, travelled - HIDDEN_DISTANCE));
        if (length <= 0.05D) {
            return;
        }
        Vec3 tail = direction.scale(-length);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer streak = bufferSource.getBuffer(RenderType.lightning());
        float headAlpha = 0.7F * fade;
        Vec3 tailLeft = tail.add(side.scale(0.3D));
        Vec3 tailRight = tail.subtract(side.scale(0.3D));
        Vec3 headLeft = side;
        Vec3 headRight = side.scale(-1.0D);
        addStreakVertex(streak, matrix, headLeft, red, green, blue, headAlpha);
        addStreakVertex(streak, matrix, tailLeft, red, green, blue, 0.0F);
        addStreakVertex(streak, matrix, tailRight, red, green, blue, 0.0F);
        addStreakVertex(streak, matrix, headRight, red, green, blue, headAlpha);
        addStreakVertex(streak, matrix, headRight, red, green, blue, headAlpha);
        addStreakVertex(streak, matrix, tailRight, red, green, blue, 0.0F);
        addStreakVertex(streak, matrix, tailLeft, red, green, blue, 0.0F);
        addStreakVertex(streak, matrix, headLeft, red, green, blue, headAlpha);
    }

    private static void addStreakVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 position, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, (float) position.x, (float) position.y, (float) position.z).setColor(red, green, blue, alpha);
    }

    private Vec3 muzzleBlendedPosition(PortalGunProjectileEntity entity, Vec3 entityPos, float partialTick) {
        float age = entity.tickCount + partialTick;
        if (age >= MUZZLE_TICKS) {
            return entityPos;
        }
        Entity shooter = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(entity.getShooterId());
        if (!(shooter instanceof LivingEntity livingShooter)) {
            return entityPos;
        }
        Vec3 eye = livingShooter.getEyePosition(partialTick);
        Vec3 look = livingShooter.getViewVector(partialTick);
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        Vec3 down = right.cross(look).normalize().scale(-1.0D);
        boolean rightHanded = livingShooter.getMainArm() == HumanoidArm.RIGHT;
        boolean offhand = !(livingShooter.getMainHandItem().getItem() instanceof com.craisinlord.antarchy.content.item.PortalGunItem)
                && livingShooter.getOffhandItem().getItem() instanceof com.craisinlord.antarchy.content.item.PortalGunItem;
        double sideSign = rightHanded != offhand ? 1.0D : -1.0D;
        Vec3 muzzle = eye.add(look.scale(MUZZLE_FORWARD)).add(right.scale(MUZZLE_SIDE * sideSign)).add(down.scale(MUZZLE_DOWN));
        double blend = Mth.clamp(age / MUZZLE_TICKS, 0.0F, 1.0F);
        return muzzle.lerp(entityPos, blend * blend);
    }

    @Override
    public boolean shouldRender(PortalGunProjectileEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    protected int getBlockLightLevel(PortalGunProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(PortalGunProjectileEntity entity) {
        return SHOT_TEXTURE;
    }

    private static void addQuad(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix, float red, float green, float blue, float alpha, float halfSize) {
        addVertex(consumer, pose, matrix, -halfSize, -halfSize, 0.0F, 1.0F, red, green, blue, alpha);
        addVertex(consumer, pose, matrix, halfSize, -halfSize, 1.0F, 1.0F, red, green, blue, alpha);
        addVertex(consumer, pose, matrix, halfSize, halfSize, 1.0F, 0.0F, red, green, blue, alpha);
        addVertex(consumer, pose, matrix, -halfSize, halfSize, 0.0F, 0.0F, red, green, blue, alpha);
    }

    private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix, float x, float y, float u, float v, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
