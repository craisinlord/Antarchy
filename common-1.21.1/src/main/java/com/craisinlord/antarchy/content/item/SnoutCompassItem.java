package com.craisinlord.antarchy.content.item;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class SnoutCompassItem extends Item {
    public static final int HORIZONTAL_RANGE = 64;
    public static final int VERTICAL_RANGE = 8;
    public static final int SECTION_BUDGET = 8;
    public static final int POSITION_BUDGET = 2048;
    private static final String TARGET = "SnoutTarget";
    private static final String DIMENSION = "SnoutDimension";
    private static final Map<ServerPlayer, Search> SEARCHES = new WeakHashMap<>();

    public SnoutCompassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel serverLevel = player.serverLevel();
        if (!serverLevel.dimension().location().equals(ResourceLocation.fromNamespaceAndPath("antarchy", "elythia"))) {
            SEARCHES.remove(player);
            sync(stack, null, serverLevel);
            return;
        }
        Search search = SEARCHES.computeIfAbsent(player, ignored -> new Search());
        if (search.tick != serverLevel.getGameTime()) {
            search.tick = serverLevel.getGameTime();
            search.advance(player);
        }
        sync(stack, search.target, serverLevel);
    }

    private static void sync(ItemStack stack, BlockPos target, ServerLevel level) {
        CompoundTag existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (target == null) {
            if (existing.contains(TARGET) || existing.contains(DIMENSION)) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                    tag.remove(TARGET);
                    tag.remove(DIMENSION);
                });
            }
        } else if (!existing.contains(TARGET) || existing.getLong(TARGET) != target.asLong()
                || !existing.getString(DIMENSION).equals(level.dimension().location().toString())) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putLong(TARGET, target.asLong());
                tag.putString(DIMENSION, level.dimension().location().toString());
            });
        }
    }

    public static BlockPos target(ItemStack stack, Level level) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains(TARGET) && data.getString(DIMENSION).equals(level.dimension().location().toString())
                ? BlockPos.of(data.getLong(TARGET)) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.antarchy.snout_compass").withStyle(ChatFormatting.GRAY));
    }

    private static BlockState loadedState(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    private static boolean buried(ServerLevel level, BlockPos pos, Block truffled) {
        BlockState state = loadedState(level, pos);
        if (state == null || !state.is(truffled)) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = pos.relative(direction);
            BlockState neighbor = loadedState(level, adjacent);
            if (neighbor == null || !neighbor.getFluidState().isEmpty()
                    || !neighbor.isFaceSturdy(EmptyBlockGetter.INSTANCE, adjacent, direction.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    private static boolean inRange(BlockPos pos, BlockPos origin) {
        long dx = pos.getX() - origin.getX();
        long dz = pos.getZ() - origin.getZ();
        return dx * dx + dz * dz <= HORIZONTAL_RANGE * HORIZONTAL_RANGE
                && Math.abs(pos.getY() - origin.getY()) <= VERTICAL_RANGE;
    }

    private static final class Search {
        private long tick = Long.MIN_VALUE;
        private long nextSearch;
        private ServerLevel level;
        private BlockPos origin;
        private BlockPos target;
        private BlockPos best;
        private double bestDistance;
        private int sectionCursor;
        private int blockCursor = -1;
        private boolean running;

        private void advance(ServerPlayer player) {
            ServerLevel currentLevel = player.serverLevel();
            BlockPos current = player.blockPosition();
            Block truffled = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("antarchy", "truffled_loam"));
            if (level != currentLevel) {
                target = null;
                running = false;
                origin = null;
                nextSearch = 0;
                level = currentLevel;
            }
            if (target != null && (!inRange(target, current) || !buried(level, target, truffled))) {
                target = null;
                nextSearch = 0;
            }
            if (origin != null && SnoutCompassSearchPolicy.shouldRestart(running, origin.distSqr(current))) {
                running = false;
                nextSearch = 0;
            }
            if (!running && tick >= nextSearch) {
                origin = current.immutable();
                best = null;
                bestDistance = Double.MAX_VALUE;
                sectionCursor = 0;
                blockCursor = -1;
                running = true;
            }
            if (!running) {
                return;
            }
            int minChunkX = (origin.getX() - HORIZONTAL_RANGE) >> 4;
            int minChunkZ = (origin.getZ() - HORIZONTAL_RANGE) >> 4;
            int width = ((origin.getX() + HORIZONTAL_RANGE) >> 4) - minChunkX + 1;
            int depth = ((origin.getZ() + HORIZONTAL_RANGE) >> 4) - minChunkZ + 1;
            int minSection = Math.max(level.getMinSection(), (origin.getY() - VERTICAL_RANGE) >> 4);
            int maxSection = Math.min(level.getMaxSection() - 1, (origin.getY() + VERTICAL_RANGE) >> 4);
            int sections = Math.max(0, maxSection - minSection + 1);
            int total = width * depth * sections;
            int probes = 0;
            int positions = 0;
            while (sectionCursor < total && probes < SECTION_BUDGET && positions < POSITION_BUDGET) {
                int sectionY = minSection + sectionCursor % sections;
                int chunkIndex = sectionCursor / sections;
                int chunkX = minChunkX + chunkIndex % width;
                int chunkZ = minChunkZ + chunkIndex / width;
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                probes++;
                LevelChunkSection section = chunk == null ? null : chunk.getSection(level.getSectionIndexFromSectionY(sectionY));
                if (section == null || !section.maybeHas(state -> state.is(truffled))) {
                    sectionCursor++;
                    blockCursor = -1;
                    continue;
                }
                if (blockCursor < 0) {
                    blockCursor = 0;
                }
                while (blockCursor < 4096 && positions < POSITION_BUDGET) {
                    int index = blockCursor++;
                    positions++;
                    int x = index & 15;
                    int z = index >> 4 & 15;
                    int y = index >> 8;
                    BlockPos pos = new BlockPos((chunkX << 4) + x, (sectionY << 4) + y, (chunkZ << 4) + z);
                    if (!inRange(pos, origin) || !section.getBlockState(x, y, z).is(truffled)) {
                        continue;
                    }
                    double distance = pos.distSqr(origin);
                    if (distance < bestDistance && buried(level, pos, truffled)) {
                        best = pos;
                        bestDistance = distance;
                    }
                }
                if (blockCursor == 4096) {
                    sectionCursor++;
                    blockCursor = -1;
                }
            }
            if (sectionCursor >= total) {
                target = best != null && inRange(best, current) && buried(level, best, truffled) ? best : null;
                nextSearch = tick + SnoutCompassSearchPolicy.refreshDelay(origin.distSqr(current));
                running = false;
            }
        }
    }

}
