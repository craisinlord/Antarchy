package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class OuranwoodSquirrelNestBlock extends Block {
    public static final IntegerProperty SQUIRREL_COUNT = IntegerProperty.create("squirrels", 1, 3);

    public OuranwoodSquirrelNestBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SQUIRREL_COUNT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SQUIRREL_COUNT);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int count = state.getValue(SQUIRREL_COUNT);
        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = findSpawnPos(level, pos, random);
            if (spawnPos == null) continue;

            FlyingSquirrelEntity squirrel = AntarchyObjects.FLYING_SQUIRREL.get().create(level, EntitySpawnReason.TRIGGERED);
            if (squirrel == null) continue;

            squirrel.snapTo(
                    spawnPos.getX() + 0.2D + random.nextDouble() * 0.6D,
                    spawnPos.getY() + 0.05D,
                    spawnPos.getZ() + 0.2D + random.nextDouble() * 0.6D,
                    random.nextFloat() * 360.0F,
                    0.0F
            );
            level.addFreshEntity(squirrel);
        }
        level.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static BlockPos findSpawnPos(ServerLevel level, BlockPos nestPos, RandomSource random) {
        if (level.isEmptyBlock(nestPos.above())) {
            return nestPos.above();
        }
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos candidate = nestPos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(2),
                    random.nextInt(3) - 1
            );
            if (level.isEmptyBlock(candidate) && !level.getBlockState(candidate.below()).canBeReplaced()) {
                return candidate;
            }
        }
        return null;
    }
}
