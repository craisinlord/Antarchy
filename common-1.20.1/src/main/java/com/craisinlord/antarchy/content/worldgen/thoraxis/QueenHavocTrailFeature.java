package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Generates the deterministic underside Queen trail and its one-shot spawn marker. */
public final class QueenHavocTrailFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation DREAM_SAND = id("dream_sand");
    private static final ResourceLocation DREAM_SANDSTONE = id("dream_sandstone");
    private static final ResourceLocation CUT_DREAM_SANDSTONE = id("cut_dream_sandstone");
    private static final ResourceLocation DREAM_FIRE = id("dream_fire_ceiling");
    private static final ResourceLocation SPAWN_MARKER = id("queen_trail_spawn_marker");

    public QueenHavocTrailFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());
        int cellX = Math.floorDiv(chunk.getMiddleBlockX(), QueenTrailGrid.SPACING);
        int cellZ = Math.floorDiv(chunk.getMiddleBlockZ(), QueenTrailGrid.SPACING);
        boolean placed = false;
        for (int gx = cellX - 1; gx <= cellX + 1; gx++) for (int gz = cellZ - 1; gz <= cellZ + 1; gz++) {
            QueenTrailGrid.Site site = QueenTrailGrid.site(level.getSeed(), gx, gz);
            int reach = site.length() + 30;
            if (site.terminalX() + reach < chunk.getMinBlockX() || site.terminalX() - reach > chunk.getMaxBlockX()
                    || site.terminalZ() + reach < chunk.getMinBlockZ() || site.terminalZ() - reach > chunk.getMaxBlockZ()) continue;
            for (int i = 0; i < site.footprints(); i++) {
                double distance = 18.0D + (site.footprints() - 1 - i) * site.stride();
                double side = (i & 1) == 0 ? -2.5D : 2.5D;
                double cx = site.terminalX() - site.directionX() * distance - site.directionZ() * side;
                double cz = site.terminalZ() - site.directionZ() * distance + site.directionX() * side;
                placed |= print(level, chunk, site, i, cx, cz);
            }
            if (site.terminalX() >= chunk.getMinBlockX() && site.terminalX() <= chunk.getMaxBlockX()
                    && site.terminalZ() >= chunk.getMinBlockZ() && site.terminalZ() <= chunk.getMaxBlockZ()) placed |= marker(level, site);
        }
        return placed;
    }

    private static boolean print(WorldGenLevel level, ChunkPos chunk, QueenTrailGrid.Site site, int index, double cx, double cz) {
        boolean placed = false;
        for (int x = Math.max(chunk.getMinBlockX(), (int) cx - 4); x <= Math.min(chunk.getMaxBlockX(), (int) cx + 4); x++)
            for (int z = Math.max(chunk.getMinBlockZ(), (int) cz - 4); z <= Math.min(chunk.getMaxBlockZ(), (int) cz + 4); z++) {
                double dx = x + .5D - cx, dz = z + .5D - cz;
                double forward = dx * site.directionX() + dz * site.directionZ();
                double lateral = -dx * site.directionZ() + dz * site.directionX();
                boolean pad = sq((forward + .5D) / 2.5D) + sq(lateral / 2.0D) <= 1.0D;
                boolean toes = toe(forward, lateral, -1.5D) || toe(forward, lateral, 0.0D) || toe(forward, lateral, 1.5D);
                if (pad || toes) placed |= carve(level, x, z, index > site.footprints() * .78D ? 3 : index > site.footprints() * .35D ? 2 : 1,
                        index % 3 == 0 ? CUT_DREAM_SANDSTONE : DREAM_SANDSTONE);
            }
        return placed;
    }

    private static boolean marker(WorldGenLevel level, QueenTrailGrid.Site site) {
        BlockPos ceiling = findCeiling(level, site.terminalX(), site.terminalZ());
        if (ceiling == null) return false;
        BlockPos markerPos = ceiling.above(4);
        level.setBlock(markerPos, block(SPAWN_MARKER, Blocks.BARRIER).defaultBlockState(), 2);
        BlockEntity entity = level.getBlockEntity(markerPos);
        if (!(entity instanceof QueenTrailSpawnMarkerBlockEntity marker)) return false;
        marker.configure(site.id(), new BlockPos(site.terminalX(), ceiling.getY() - 17, site.terminalZ()),
                new BlockPos(site.terminalX(), ceiling.getY() - 2, site.terminalZ()),
                (float) Math.toDegrees(Math.atan2(site.directionZ(), site.directionX())) - 90.0F);
        return true;
    }

    private static boolean carve(WorldGenLevel level, int x, int z, int depth, ResourceLocation backingId) {
        BlockPos ceiling = findCeiling(level, x, z);
        if (ceiling == null) return false;
        level.setBlock(ceiling.above(depth), block(backingId, Blocks.SANDSTONE).defaultBlockState(), 2);
        for (int i = 0; i < depth; i++) level.setBlock(ceiling.above(i), Blocks.AIR.defaultBlockState(), 2);
        return true;
    }

    private static BlockPos findCeiling(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, -8, z);
        int minimum = Math.max(level.getMinBuildHeight() + 20, -240);
        for (int y = -8; y >= minimum; y--) {
            cursor.setY(y);
            if (isDreamSurface(level.getBlockState(cursor)) && level.getBlockState(cursor.below()).isAir()) return cursor.immutable();
        }
        return null;
    }

    private static boolean isDreamSurface(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return DREAM_SAND.equals(id) || DREAM_SANDSTONE.equals(id) || CUT_DREAM_SANDSTONE.equals(id);
    }
    private static boolean toe(double forward, double lateral, double center) { return sq((forward - 2.2D) / 1.25D) + sq((lateral - center) / .8D) <= 1.0D; }
    private static double sq(double value) { return value * value; }
    private static Block block(ResourceLocation id, Block fallback) { return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback); }
    private static ResourceLocation id(String path) { return new ResourceLocation(Antarchy.MODID, path); }
}
