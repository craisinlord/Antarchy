package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.PortalGunBlackHoleModel;
import com.craisinlord.antarchy.content.portalgun.PortalGunBlackHoleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalGunBlackHoleRenderer extends GeoEntityRenderer<PortalGunBlackHoleEntity> {
    public PortalGunBlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context, new PortalGunBlackHoleModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(PortalGunBlackHoleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 0xF000F0);
    }

    @Override
    public @Nullable RenderType getRenderType(PortalGunBlackHoleEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
