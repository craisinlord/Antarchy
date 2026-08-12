package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MucusBlock extends GlowLichenBlock {
    private static final VoxelShape DOWN_COLLISION_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D);
    private static final double SLIDE_ACCELERATION = 0.025D;
    private static final double SLIDE_MAX_SPEED = 0.6D;
    private static final double SLIDE_MIN_SPEED = 0.02D;

    public MucusBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(MultifaceBlock.getFaceProperty(Direction.DOWN)) ? DOWN_COLLISION_SHAPE : Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !state.getValue(MultifaceBlock.getFaceProperty(Direction.DOWN))) {
            return;
        }

        Vec3 delta = entity.getDeltaMovement();
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        double speed = horizontal.length();
        if (speed < SLIDE_MIN_SPEED) {
            return;
        }

        Vec3 direction = horizontal.scale(1.0D / speed);
        double boostedSpeed = Math.min(speed + SLIDE_ACCELERATION, SLIDE_MAX_SPEED);
        entity.setDeltaMovement(direction.x * boostedSpeed, delta.y, direction.z * boostedSpeed);
        entity.hurtMarked = true;
        entity.hasImpulse = true;
    }
}
