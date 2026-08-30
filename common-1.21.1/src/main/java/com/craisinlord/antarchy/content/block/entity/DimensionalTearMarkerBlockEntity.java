package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DimensionalTearMarkerBlockEntity extends BlockEntity {
    private static final int MAX_ATTEMPT_TICKS = 100;
    private static final int MIN_LINK_RANGE = 50;
    private static final int MAX_LINK_RANGE = 500;
    private static final int PARTNER_ORIGIN_ATTEMPTS = 12;
    private static final int PARTNER_SEARCH_ATTEMPTS = 48;
    private static final int LOCAL_SEARCH_ATTEMPTS = 18;

    private int attemptTicks;

    public DimensionalTearMarkerBlockEntity(
            BlockPos pos,
            BlockState blockState,
            Supplier<? extends BlockEntityType<DimensionalTearMarkerBlockEntity>> blockEntityTypeSupplier
    ) {
        super(blockEntityTypeSupplier.get(), pos, blockState);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, DimensionalTearMarkerBlockEntity blockEntity) {
        blockEntity.attemptTicks++;
        boolean spawned = blockEntity.trySpawnLinkedPair(level, pos);
        if (spawned || blockEntity.attemptTicks >= MAX_ATTEMPT_TICKS) {
            level.removeBlock(pos, false);
        }
    }

    private boolean trySpawnLinkedPair(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        BlockPos first = findOpenPocket(level, origin, random, LOCAL_SEARCH_ATTEMPTS);
        if (first == null) {
            return false;
        }

        BlockPos second = null;
        for (int i = 0; i < PARTNER_ORIGIN_ATTEMPTS && second == null; i++) {
            second = findOpenPocket(level, linkedOrigin(first, random), random, PARTNER_SEARCH_ATTEMPTS);
        }
        if (second == null) {
            return false;
        }

        int lifetime = Math.max(1200, AntarchySettings.dimensionalTearLifetimeTicks());
        float yawA = random.nextFloat() * 360.0F;
        float yawB = Mth.wrapDegrees(yawA + 140.0F + random.nextFloat() * 80.0F);
        DimensionalTearEntity tearA = DimensionalTearEntity.create(level, center(first), yawA, lifetime);
        DimensionalTearEntity tearB = DimensionalTearEntity.create(level, center(second), yawB, lifetime);
        tearA.linkTo(tearB);
        tearB.linkTo(tearA);
        level.addFreshEntity(tearA);
        level.addFreshEntity(tearB);
        return true;
    }

    private static BlockPos linkedOrigin(BlockPos first, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        int distance = Mth.nextInt(random, MIN_LINK_RANGE, MAX_LINK_RANGE);
        int dx = Mth.floor(Math.cos(angle) * distance);
        int dz = Mth.floor(Math.sin(angle) * distance);
        int dy = Mth.nextInt(random, -36, 36);
        return first.offset(dx, dy, dz);
    }

    @Nullable
    private static BlockPos findOpenPocket(ServerLevel level, BlockPos origin, RandomSource random, int attempts) {
        int minY = level.getMinBuildHeight() + 8;
        int maxY = level.getMaxBuildHeight() - 8;
        for (int i = 0; i < attempts; i++) {
            BlockPos candidate = origin.offset(
                    Mth.nextInt(random, -16, 16),
                    Mth.nextInt(random, -24, 24),
                    Mth.nextInt(random, -16, 16)
            );
            candidate = new BlockPos(candidate.getX(), Mth.clamp(candidate.getY(), minY, maxY), candidate.getZ());
            if (isUsablePocket(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isUsablePocket(ServerLevel level, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (!level.isLoaded(checkPos)) {
                        return false;
                    }
                    if (!level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Vec3 center(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("attempt_ticks", this.attemptTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.attemptTicks = tag.getInt("attempt_ticks");
    }
}
