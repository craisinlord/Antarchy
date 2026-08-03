package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.mixins.AccessorFireBlock;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelReader;

public class DreamFireBlock extends FireBlock {
    @SuppressWarnings("rawtypes")
    public static final MapCodec<FireBlock> CODEC = (MapCodec) simpleCodec(DreamFireBlock::new);

    public DreamFireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.copyVanillaFlammability();
    }

    @Override
    public MapCodec<FireBlock> codec() {
        return CODEC;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.below()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS)) {
            if (!this.canSurvive(state, level, pos)) {
                level.removeBlock(pos, false);
                return;
            }

            level.scheduleTick(pos, this, 30 + random.nextInt(10));
            return;
        }

        super.tick(state, level, pos, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS)
                || super.canSurvive(state, level, pos);
    }

    private void copyVanillaFlammability() {
        AccessorFireBlock vanillaFire = (AccessorFireBlock) Blocks.FIRE;
        Object2IntMap<net.minecraft.world.level.block.Block> vanillaIgniteOdds = vanillaFire.antarchy$getIgniteOdds();
        Object2IntMap<net.minecraft.world.level.block.Block> vanillaBurnOdds = vanillaFire.antarchy$getBurnOdds();
        for (Object2IntMap.Entry<net.minecraft.world.level.block.Block> entry : vanillaIgniteOdds.object2IntEntrySet()) {
            this.setFlammable(entry.getKey(), entry.getIntValue(), vanillaBurnOdds.getInt(entry.getKey()));
        }
    }
}
