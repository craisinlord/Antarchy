package com.craisinlord.antarchy.content.fluid;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class BileLiquidBlock extends LiquidBlock {
    private static final int EFFECT_DURATION_TICKS = 100;
    private static final Vec3 SLOW_MOVEMENT = new Vec3(0.85D, 0.95D, 0.85D);

    public BileLiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (entity instanceof LivingEntity livingEntity) {
            if (!level.isClientSide) {
                livingEntity.addEffect(new MobEffectInstance(AntarchyObjects.STINKY_EFFECT.get(), EFFECT_DURATION_TICKS, 0, false, true, true));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0, false, true, true));
            }

            if (livingEntity.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                return;
            }
        }

        entity.makeStuckInBlock(state, SLOW_MOVEMENT);
    }
}
