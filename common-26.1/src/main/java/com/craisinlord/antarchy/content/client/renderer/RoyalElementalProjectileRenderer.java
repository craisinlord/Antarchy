package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalElementalProjectileModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalElementalProjectileEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RoyalElementalProjectileRenderer extends GeoEntityRenderer<RoyalElementalProjectileEntity, EntityRenderState> {
    public static final DataTicket<Float> RENDER_YAW =
            DataTicket.create("antarchy_royal_elemental_projectile_render_yaw", Float.class);
    public static final DataTicket<Float> RENDER_PITCH =
            DataTicket.create("antarchy_royal_elemental_projectile_render_pitch", Float.class);

    public RoyalElementalProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalElementalProjectileModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return isIceball(renderState)
                ? RenderTypes.entityCutout(texture)
                : RenderTypes.entityTranslucentEmissive(texture);
    }

    @Override
    public void addRenderData(RoyalElementalProjectileEntity entity, Void relatedObject, EntityRenderState renderState,
                              float partialTick) {
        super.addRenderData(entity, relatedObject, renderState, partialTick);
        Vec3 travel = entity.getDeltaMovement();
        float yaw;
        float pitch;
        if (!entity.isIceball() && travel.lengthSqr() > 1.0E-6D) {
            double horizontal = travel.horizontalDistance();
            yaw = (float) (Mth.atan2(travel.z, travel.x) * Mth.RAD_TO_DEG) - 90.0F;
            pitch = (float) (Mth.atan2(horizontal, travel.y) * Mth.RAD_TO_DEG) - 90.0F;
        } else {
            yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        }
        renderState.addGeckolibData(RENDER_YAW, yaw);
        renderState.addGeckolibData(RENDER_PITCH, pitch);
    }

    @Override
    public void preRenderPass(RenderPassInfo<EntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        float scale = isIceball(renderPassInfo.renderState()) ? 0.75F : 0.72F;
        renderPassInfo.poseStack().scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        float yaw = renderPassInfo.getOrDefaultGeckolibData(RENDER_YAW, 0.0F);
        float pitch = renderPassInfo.getOrDefaultGeckolibData(RENDER_PITCH, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        if (!isIceball(renderPassInfo.renderState())) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    private static boolean isIceball(EntityRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(RoyalElementalProjectileModel.ICEBALL, false);
    }

    @Override
    protected boolean affectedByCulling(RoyalElementalProjectileEntity entity) {
        return false;
    }
}
