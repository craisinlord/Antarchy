package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.DrTrayaurusEntity;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public class DrTrayaurusRenderer extends MobRenderer<DrTrayaurusEntity, VillagerRenderState, VillagerModel> {
    private final Identifier texture;

    public DrTrayaurusRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.texture = texture;
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(DrTrayaurusEntity entity, VillagerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isUnhappy = entity.getUnhappyCounter() > 0;
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return this.texture;
    }
}
