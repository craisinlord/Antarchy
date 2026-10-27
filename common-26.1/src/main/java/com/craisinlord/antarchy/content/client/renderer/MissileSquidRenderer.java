package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MissileSquidModel;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class MissileSquidRenderer extends GeoEntityRenderer<MissileSquidEntity, LivingEntityRenderState> {
    public MissileSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new MissileSquidModel());
        this.shadowRadius = 1.3F;
    }

    @Override
    public Vec3 getRenderOffset(LivingEntityRenderState renderState) {
        return new Vec3(0.0D, -2.0D, 0.0D);
    }
}
