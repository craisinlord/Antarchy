package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BrutalflyCocoonSpawnerBlock extends Block {

    public BrutalflyCocoonSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        BrutalflyEntity brutalfly = AntarchyObjects.BRUTALFLY.get().create(level);
        if (brutalfly == null) {
            return;
        }

        BlockPos anchor = pos.above();
        brutalfly.moveTo(
                anchor.getX() + 0.5D,
                anchor.getY() - 1.5D,
                anchor.getZ() + 0.5D,
                random.nextFloat() * 360.0F,
                0.0F
        );
        brutalfly.setCocooned(true, anchor);
        brutalfly.setHealth(brutalfly.getMaxHealth());
        level.addFreshEntity(brutalfly);
    }
}
