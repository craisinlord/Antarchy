package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PrimordialGrassBlock extends BushBlock {
    private static volatile boolean chunkModel;
    public static boolean usesChunkModel() {
        return chunkModel;
    }
    public static void setUsesChunkModel(boolean value) {
        chunkModel = value;
    }
    private static final TagKey<Block> GROUND = TagKey.create(Registries.BLOCK, new ResourceLocation("antarchy", "primordial_ground"));
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 18, 14);

    public PrimordialGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(GROUND) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return chunkModel ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
