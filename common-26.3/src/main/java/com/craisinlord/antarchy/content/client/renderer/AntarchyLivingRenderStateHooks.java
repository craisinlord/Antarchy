package com.craisinlord.antarchy.content.client.renderer;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public final class AntarchyLivingRenderStateHooks {
    private AntarchyLivingRenderStateHooks() {
    }

    public static <T extends LivingEntity, S extends LivingEntityRenderState> void extract(LivingEntityRenderer<T, S, ?> renderer, T entity, S state, float partialTick) {
        ParalyzedStoneLivingLayer.extract(entity, state);
        GoopedLivingLayer.extract(renderer, entity, state);
        TigerEyeCamouflagePlayerLayer.extract(entity, state);
    }
}
