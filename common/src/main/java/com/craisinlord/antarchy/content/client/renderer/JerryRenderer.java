package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.JerryModel;
import com.craisinlord.antarchy.content.entity.JerryEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class JerryRenderer extends GeoEntityRenderer<JerryEntity> {
    public JerryRenderer(EntityRendererProvider.Context context) {
        super(context, new JerryModel());
        this.shadowRadius = 0.7F;
    }

    @Override
    public RenderType getRenderType(JerryEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        JerryEntity.Stage stage = animatable.getStage();
        return stage == JerryEntity.Stage.INFANT || stage == JerryEntity.Stage.MATURE
                ? RenderType.entityTranslucent(texture)
                : RenderType.entityCutoutNoCull(texture);
    }
}
