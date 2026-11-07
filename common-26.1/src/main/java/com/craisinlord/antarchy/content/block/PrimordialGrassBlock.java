package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PrimordialGrassBlock extends VegetationBlock {
    public static final MapCodec<PrimordialGrassBlock> CODEC = simpleCodec(PrimordialGrassBlock::new);
    private static final TagKey<Block> GROUND = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("antarchy", "primordial_ground"));
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 18, 14);
    private static volatile boolean chunkModel;

    public static boolean usesChunkModel() {
        return chunkModel;
    }

    public static void setUsesChunkModel(boolean value) {
        chunkModel = value;
    }

    public PrimordialGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(GROUND) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return chunkModel ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
