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
    private static final int SEARCH_ATTEMPTS_PER_TICK = 2;

    private int attemptTicks;
    private int localAttempts;
    private int partnerOriginAttempts;
    private int partnerSearchAttempts;
    @Nullable
    private BlockPos firstPocket;
    @Nullable
    private BlockPos partnerOrigin;

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
        for (int i = 0; i < SEARCH_ATTEMPTS_PER_TICK; i++) {
            if (firstPocket == null) {
                if (localAttempts >= LOCAL_SEARCH_ATTEMPTS) {
                    return false;
                }
                localAttempts++;
                BlockPos candidate = randomCandidate(origin, random);
                if (isUsablePocket(level, candidate)) {
                    firstPocket = candidate;
                }
                continue;
            }

            if (partnerOrigin == null || partnerSearchAttempts >= PARTNER_SEARCH_ATTEMPTS) {
                if (partnerOriginAttempts >= PARTNER_ORIGIN_ATTEMPTS) {
                    return false;
                }
                partnerOrigin = linkedOrigin(firstPocket, random);
                partnerOriginAttempts++;
                partnerSearchAttempts = 0;
            }

            partnerSearchAttempts++;
            BlockPos candidate = randomCandidate(partnerOrigin, random);
            if (!isUsablePocket(level, candidate)) {
                continue;
            }
            return spawnPair(level, random, firstPocket, candidate);
        }

        return false;
    }

    private boolean spawnPair(ServerLevel level, RandomSource random, BlockPos first, BlockPos second) {

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

    private static BlockPos randomCandidate(BlockPos origin, RandomSource random) {
        return origin.offset(
                Mth.nextInt(random, -16, 16),
                Mth.nextInt(random, -24, 24),
                Mth.nextInt(random, -16, 16)
        );
    }

    private static BlockPos linkedOrigin(BlockPos first, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        int distance = Mth.nextInt(random, MIN_LINK_RANGE, MAX_LINK_RANGE);
        int dx = Mth.floor(Math.cos(angle) * distance);
        int dz = Mth.floor(Math.sin(angle) * distance);
        int dy = Mth.nextInt(random, -36, 36);
        return first.offset(dx, dy, dz);
    }

    private static boolean isUsablePocket(ServerLevel level, BlockPos pos) {
        int minY = level.getMinBuildHeight() + 8;
        int maxY = level.getMaxBuildHeight() - 8;
        if (pos.getY() < minY || pos.getY() > maxY) {
            return false;
        }
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
        tag.putInt("local_attempts", this.localAttempts);
        tag.putInt("partner_origin_attempts", this.partnerOriginAttempts);
        tag.putInt("partner_search_attempts", this.partnerSearchAttempts);
        if (this.firstPocket != null) {
            tag.putLong("first_pocket", this.firstPocket.asLong());
        }
        if (this.partnerOrigin != null) {
            tag.putLong("partner_origin", this.partnerOrigin.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.attemptTicks = tag.getInt("attempt_ticks");
        this.localAttempts = tag.getInt("local_attempts");
        this.partnerOriginAttempts = tag.getInt("partner_origin_attempts");
        this.partnerSearchAttempts = tag.getInt("partner_search_attempts");
        this.firstPocket = tag.contains("first_pocket") ? BlockPos.of(tag.getLong("first_pocket")) : null;
        this.partnerOrigin = tag.contains("partner_origin") ? BlockPos.of(tag.getLong("partner_origin")) : null;
    }
}
