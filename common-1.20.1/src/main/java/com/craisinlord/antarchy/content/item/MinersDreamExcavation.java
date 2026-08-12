package com.craisinlord.antarchy.content.item;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public final class MinersDreamExcavation {
    private static final Vector3f TRIFFID_GOO_COLOR = new Vector3f(0.35F, 0.85F, 0.4F);
    private static final int SLICES_BETWEEN_RUMBLE = 4;

    private final UUID owner;
    private final ResourceKey<Level> dimension;
    private final List<MinersDreamCaveGenerator.CaveNode> nodes;
    private final Set<Integer> torchCheckpoints;
    private final LongOpenHashSet visited = new LongOpenHashSet();

    private int nodeIndex = 0;
    private List<BlockPos> currentNodePositions = null;
    private int positionCursor = 0;
    private int slicesSinceRumble = 0;
    private boolean finished = false;

    public MinersDreamExcavation(UUID owner, ResourceKey<Level> dimension, List<MinersDreamCaveGenerator.CaveNode> nodes, Set<Integer> torchCheckpoints) {
        this.owner = owner;
        this.dimension = dimension;
        this.nodes = nodes;
        this.torchCheckpoints = torchCheckpoints;
    }

    public UUID owner() {
        return owner;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public boolean isFinished() {
        return finished;
    }

    public void tick(ServerLevel level, int blockBudget) {
        RandomSource random = level.getRandom();
        int remaining = blockBudget;

        while (remaining > 0 && !finished) {
            if (currentNodePositions == null) {
                if (nodeIndex >= nodes.size()) {
                    finished = true;
                    playCompletionSound(level);
                    return;
                }

                MinersDreamCaveGenerator.CaveNode node = nodes.get(nodeIndex);
                if (!MinersDreamCaveGenerator.isChunkLoaded(level, node.center())) {
                    advanceNode(level, node);
                    continue;
                }

                currentNodePositions = new ArrayList<>();
                MinersDreamCaveGenerator.collectNodeCandidates(level, node, visited, random, currentNodePositions);
                positionCursor = 0;
            }

            if (currentNodePositions.isEmpty()) {
                MinersDreamCaveGenerator.CaveNode node = nodes.get(nodeIndex);
                advanceNode(level, node);
                continue;
            }

            int endIndex = Math.min(currentNodePositions.size(), positionCursor + remaining);
            for (; positionCursor < endIndex; positionCursor++) {
                BlockPos pos = currentNodePositions.get(positionCursor);
                BlockState removedState = level.getBlockState(pos);
                if (removedState.isAir()) {
                    continue;
                }
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                remaining--;
                if (random.nextFloat() < 0.12F) {
                    spawnBreakParticles(level, pos, removedState, random);
                }
            }

            if (positionCursor >= currentNodePositions.size()) {
                MinersDreamCaveGenerator.CaveNode node = nodes.get(nodeIndex);
                advanceNode(level, node);
            }
        }
    }

    private void advanceNode(ServerLevel level, MinersDreamCaveGenerator.CaveNode node) {
        RandomSource random = level.getRandom();
        spawnFrontDust(level, node, random);
        slicesSinceRumble++;
        if (slicesSinceRumble >= SLICES_BETWEEN_RUMBLE) {
            slicesSinceRumble = 0;
            playRumble(level, node);
        }

        if (torchCheckpoints.contains(nodeIndex)) {
            MinersDreamTorchPlacer.tryPlaceTorch(level, node.center(), random);
        }

        currentNodePositions = null;
        positionCursor = 0;
        nodeIndex++;
    }

    private void spawnBreakParticles(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 2, 0.25D, 0.25D, 0.25D, 0.02D);
        if (random.nextFloat() < 0.2F) {
            level.sendParticles(new DustParticleOptions(TRIFFID_GOO_COLOR, 1.0F),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1, 0.2D, 0.2D, 0.2D, 0.0D);
        }
    }

    private void spawnFrontDust(ServerLevel level, MinersDreamCaveGenerator.CaveNode node, RandomSource random) {
        BlockPos center = node.center();
        level.sendParticles(ParticleTypes.CLOUD, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D,
                3, node.horizontalRadius() * 0.4D, node.verticalRadius() * 0.3D, node.horizontalRadius() * 0.4D, 0.01D);
    }

    private void playRumble(ServerLevel level, MinersDreamCaveGenerator.CaveNode node) {
        BlockPos center = node.center();
        level.playSound(null, center, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.5F, 0.7F);
    }

    private void playCompletionSound(ServerLevel level) {
        if (nodes.isEmpty()) {
            return;
        }
        BlockPos center = nodes.get(nodes.size() - 1).center();
        level.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.4F, 1.4F);
    }
}
