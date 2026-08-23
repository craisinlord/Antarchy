package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.PortalGunPortalModel;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalGunPortalRenderer extends GeoEntityRenderer<PortalGunPortalEntity> {
    public PortalGunPortalRenderer(EntityRendererProvider.Context context) {
        super(context, new PortalGunPortalModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(PortalGunPortalEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 0xF000F0);
    }

    @Override
    protected void applyRotations(PortalGunPortalEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        Vec3 width = animatable.getWidthVec();
        Vec3 up = animatable.getUpVec();
        Vec3 normal = animatable.getNormalVec();
        Matrix3f basis = new Matrix3f(
                (float) width.x, (float) up.x, (float) -normal.x,
                (float) width.y, (float) up.y, (float) -normal.y,
                (float) width.z, (float) up.z, (float) -normal.z
        );
        poseStack.mulPose(basis.getNormalizedRotation(new Quaternionf()));
    }

    @Override
    public @Nullable RenderType getRenderType(PortalGunPortalEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
