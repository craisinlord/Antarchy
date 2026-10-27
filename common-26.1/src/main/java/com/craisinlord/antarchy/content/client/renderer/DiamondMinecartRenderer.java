package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DiamondMinecartRenderer extends EntityRenderer<DiamondMinecartEntity, MinecartRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/diamond_minecart.png");
    private final MinecartModel model;
    private final BlockModelResolver blockModelResolver;

    public DiamondMinecartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel(context.bakeLayer(ModelLayers.MINECART));
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }

    @Override
    public void submit(MinecartRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        long seed = state.offsetSeed;
        float offsetX = (((float) (seed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float offsetY = (((float) (seed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float offsetZ = (((float) (seed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(offsetX, offsetY, offsetZ);
        if (state.isNewRender) {
            newRender(state, poseStack);
        } else {
            oldRender(state, poseStack);
        }

        float hurt = state.hurtTime;
        if (hurt > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt) * hurt * state.damageTime / 10.0F * state.hurtDir));
        }

        BlockModelRenderState displayBlockModel = state.displayBlockModel;
        if (!displayBlockModel.isEmpty()) {
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5F, (state.displayOffset - 8) / 16.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            displayBlockModel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
    }

    private static void newRender(MinecartRenderState state, PoseStack poseStack) {
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
        poseStack.translate(0.0F, 0.375F, 0.0F);
    }

    private static void oldRender(MinecartRenderState state, PoseStack poseStack) {
        double entityX = state.x;
        double entityY = state.y;
        double entityZ = state.z;
        float xRot = state.xRot;
        float rotation = state.yRot;
        if (state.posOnRail != null && state.frontPos != null && state.backPos != null) {
            Vec3 frontPos = state.frontPos;
            Vec3 backPos = state.backPos;
            poseStack.translate(state.posOnRail.x - entityX, (frontPos.y + backPos.y) / 2.0D - entityY, state.posOnRail.z - entityZ);
            Vec3 direction = backPos.add(-frontPos.x, -frontPos.y, -frontPos.z);
            if (direction.length() != 0.0D) {
                direction = direction.normalize();
                rotation = (float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI);
                xRot = (float) (Math.atan(direction.y) * 73.0D);
            }
        }

        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
    }

    @Override
    public void extractRenderState(DiamondMinecartEntity entity, MinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (entity.getBehavior() instanceof NewMinecartBehavior behavior) {
            if (behavior.cartHasPosRotLerp()) {
                state.renderPos = behavior.getCartLerpPosition(partialTicks);
                state.xRot = behavior.getCartLerpXRot(partialTicks);
                state.yRot = behavior.getCartLerpYRot(partialTicks);
            } else {
                state.renderPos = null;
                state.xRot = entity.getXRot();
                state.yRot = entity.getYRot();
            }
            state.isNewRender = true;
        } else if (entity.getBehavior() instanceof OldMinecartBehavior behavior) {
            state.xRot = entity.getXRot(partialTicks);
            state.yRot = entity.getYRot(partialTicks);
            Vec3 pos = behavior.getPos(state.x, state.y, state.z);
            if (pos != null) {
                state.posOnRail = pos;
                Vec3 front = behavior.getPosOffs(state.x, state.y, state.z, 0.3F);
                Vec3 back = behavior.getPosOffs(state.x, state.y, state.z, -0.3F);
                state.frontPos = Objects.requireNonNullElse(front, pos);
                state.backPos = Objects.requireNonNullElse(back, pos);
            } else {
                state.posOnRail = null;
                state.frontPos = null;
                state.backPos = null;
            }
            state.isNewRender = false;
        }

        long seed = entity.getId() * 493286711L;
        state.offsetSeed = seed * seed * 4392167121L + seed * 98761L;
        state.hurtTime = entity.getHurtTime() - partialTicks;
        state.hurtDir = entity.getHurtDir();
        state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0F);
        state.displayOffset = entity.getDisplayOffset();
        this.blockModelResolver.update(state.displayBlockModel, entity.getDisplayBlockState(), AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT);
    }

    @Override
    protected AABB getBoundingBoxForCulling(DiamondMinecartEntity entity) {
        AABB aabb = super.getBoundingBoxForCulling(entity);
        return !entity.getDisplayBlockState().isAir() ? aabb.expandTowards(0.0D, entity.getDisplayOffset() * 0.75F / 16.0F, 0.0D) : aabb;
    }

    @Override
    public Vec3 getRenderOffset(MinecartRenderState state) {
        Vec3 offset = super.getRenderOffset(state);
        return state.isNewRender && state.renderPos != null
                ? offset.add(state.renderPos.x - state.x, state.renderPos.y - state.y, state.renderPos.z - state.z)
                : offset;
    }
}
