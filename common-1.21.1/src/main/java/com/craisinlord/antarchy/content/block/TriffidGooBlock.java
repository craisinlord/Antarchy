package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TriffidGooBlock extends Block {

    private static final double BOUNCE_VELOCITY_THRESHOLD = 0.6;
    private static final double MAX_SINK_SPEED = 0.05;
    private static final double SPRINT_VELOCITY_THRESHOLD = 0.1;

    public TriffidGooBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    public boolean canStickTo(BlockState state, BlockState other) {
        return !other.is(Blocks.SLIME_BLOCK) && !other.is(Blocks.HONEY_BLOCK);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ecc) {
            Entity entity = ecc.getEntity();
            if (entity != null) {
                Vec3 delta = entity.getDeltaMovement();
                double vy = delta.y;
                double hSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                if (vy <= 0.0 && vy > -BOUNCE_VELOCITY_THRESHOLD && hSpeed < SPRINT_VELOCITY_THRESHOLD) {
                    return Shapes.empty();
                }
            }
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Vec3 delta = entity.getDeltaMovement();
        if (delta.y < -MAX_SINK_SPEED) {
            entity.setDeltaMovement(delta.x * 0.9, -MAX_SINK_SPEED, delta.z * 0.9);
        } else {
            entity.setDeltaMovement(delta.x * 0.9, delta.y, delta.z * 0.9);
        }
        entity.fallDistance = 0.0F;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        Vec3 delta = entity.getDeltaMovement();
        if (delta.y < 0.0) {
            double coefficient = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setDeltaMovement(delta.x, -delta.y * coefficient, delta.z);
        }
    }
}
