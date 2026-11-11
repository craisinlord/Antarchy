package com.craisinlord.antarchy.content.block;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
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
    private static final ThreadLocal<boolean[]> MESHING_DYNAMIC = ThreadLocal.withInitial(() -> new boolean[1]);
    private static volatile LongSet dynamicSections = LongSets.EMPTY_SET;

    public static void setDynamicSections(LongSet sections) {
        dynamicSections = sections;
    }

    public static void beginMeshing(int sectionX, int sectionY, int sectionZ) {
        LongSet sections = dynamicSections;
        MESHING_DYNAMIC.get()[0] = !sections.isEmpty() && sections.contains(SectionPos.asLong(sectionX, sectionY, sectionZ));
    }

    public static void endMeshing() {
        MESHING_DYNAMIC.get()[0] = false;
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
        return MESHING_DYNAMIC.get()[0] ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
