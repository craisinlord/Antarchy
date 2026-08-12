package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.DrTrayaurusEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DrTrayaurusRenderer extends MobRenderer<DrTrayaurusEntity, VillagerModel<DrTrayaurusEntity>> {
    private final ResourceLocation texture;

    public DrTrayaurusRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(DrTrayaurusEntity entity) {
        return this.texture;
    }
}
