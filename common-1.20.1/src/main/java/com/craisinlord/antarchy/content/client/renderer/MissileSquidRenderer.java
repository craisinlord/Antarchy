package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MissileSquidModel;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MissileSquidRenderer extends GeoEntityRenderer<MissileSquidEntity> {
    public MissileSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new MissileSquidModel());
        this.shadowRadius = 1.3F;
    }

    @Override
    public Vec3 getRenderOffset(MissileSquidEntity entity, float partialTick) {
        return new Vec3(0.0D, -2.0D, 0.0D);
    }
}
