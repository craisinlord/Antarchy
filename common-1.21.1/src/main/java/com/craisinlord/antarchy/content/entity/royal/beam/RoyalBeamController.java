package com.craisinlord.antarchy.content.entity.royal.beam;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class RoyalBeamController {
    private final Mob owner;
    private Vec3 serverTarget;
    private Vec3 beamEndPosition;
    private int beamTicks;
    private int terrainMutationsThisTick;
    private static final int ICE_MUTATIONS_PER_TICK = 3;
    private static final float ICE_IMPACT_RADIUS = 2.0F;

    public RoyalBeamController(Mob owner) {
        this.owner = owner;
    }

    public boolean isFiring() {
        return this.beamTicks > 0;
    }

    public int beamTicks() {
        return this.beamTicks;
    }

    @Nullable
    public Vec3 beamEndPosition() {
        return this.beamEndPosition;
    }

    public void start(Vec3 shootFrom, Vec3 target, int durationTicks) {
        this.serverTarget = target;
        this.beamEndPosition = target;
        this.beamTicks = Math.max(1, durationTicks);
    }

    public void stop() {
        this.serverTarget = null;
        this.beamEndPosition = null;
        this.beamTicks = 0;
    }

    public void tick(
            Vec3 shootFrom,
            @Nullable LivingEntity target,
            RoyalBeamSettings settings,
            RoyalBeamTerrainMode terrainMode,
            Consumer<@Nullable Vec3> syncedEndPosition
    ) {
        if (this.beamTicks <= 0) {
            syncedEndPosition.accept(null);
            return;
        }

        this.beamTicks--;
        this.terrainMutationsThisTick = 0;
        if (target != null && target.isAlive()) {
            Vec3 targetEye = target.getEyePosition();
            this.serverTarget = this.serverTarget == null
                    ? targetEye
                    : this.serverTarget.add(targetEye.subtract(this.serverTarget).scale(settings.targetTracking()));
        }
        if (this.serverTarget == null) {
            this.stop();
            syncedEndPosition.accept(null);
            return;
        }

        Vec3 direction = this.serverTarget.subtract(shootFrom).normalize();
        if (direction.lengthSqr() < 1.0E-7D) {
            direction = this.owner.getLookAngle();
        }
        Vec3 clipEnd = shootFrom.add(direction.scale(settings.range()));
        HitResult hit = this.owner.level().clip(new ClipContext(
                shootFrom,
                clipEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.owner
        ));
        this.beamEndPosition = hit.getLocation();
        syncedEndPosition.accept(this.beamEndPosition);

        if (!this.owner.level().isClientSide && this.beamEndPosition != null) {
            boolean impactedBlock = hit.getType() == HitResult.Type.BLOCK;
            marchDamageAndTerrain(shootFrom, this.beamEndPosition, settings, terrainMode, impactedBlock);
        }

        if (this.beamTicks <= 0) {
            this.stop();
            syncedEndPosition.accept(null);
        }
    }

    private void marchDamageAndTerrain(
            Vec3 shootFrom,
            Vec3 beamEnd,
            RoyalBeamSettings settings,
            RoyalBeamTerrainMode terrainMode,
            boolean impactedBlock
    ) {
        Vec3 direction = beamEnd.subtract(shootFrom).normalize();
        double distance = shootFrom.distanceTo(beamEnd);
        DamageSource damageSource = this.owner.damageSources().mobAttack(this.owner);
        Set<Integer> damagedThisTick = new HashSet<>();
        boolean pathMutates = terrainMode == RoyalBeamTerrainMode.DESTROY;
        for (double walked = settings.pathStep(); walked < Math.min(distance, settings.range()); walked += settings.pathStep()) {
            Vec3 sample = shootFrom.add(direction.scale(walked));
            hurtEntitiesAround(sample, settings.pathDamageRadius(), settings.damage(), settings.knockback(), damageSource, settings.requireLineOfSightForDamage(), damagedThisTick);
            if (pathMutates && shouldMutateTerrain(settings, walked)) {
                mutateTerrainAround(sample, settings.pathTerrainRadius(), settings, terrainMode);
            }
        }

        hurtEntitiesAround(beamEnd, settings.impactDamageRadius(), settings.damage(), settings.knockback(), damageSource, settings.requireLineOfSightForDamage(), damagedThisTick);

        if (!shouldMutateTerrain(settings, distance)) {
            return;
        }
        if (terrainMode == RoyalBeamTerrainMode.BUILD_ICE) {
            if (impactedBlock) {
                mutateTerrainAround(beamEnd, ICE_IMPACT_RADIUS, settings, terrainMode);
            }
        } else {
            mutateTerrainAround(beamEnd, settings.impactTerrainRadius(), settings, terrainMode);
        }
    }

    private boolean shouldMutateTerrain(RoyalBeamSettings settings, double distanceFromOwner) {
        return settings.terrainEnabled()
                && settings.maxTerrainMutationsPerTick() > 0
                && this.beamTicks % settings.terrainIntervalTicks() == 0
                && distanceFromOwner <= settings.maxTerrainDistance()
                && this.owner.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    private boolean hurtEntitiesAround(
            Vec3 center,
            float radius,
            float damageAmount,
            float knockbackAmount,
            DamageSource damageSource,
            boolean requireLineOfSight,
            Set<Integer> damagedThisTick
    ) {
        if (radius <= 0.0F || damageAmount <= 0.0F) {
            return false;
        }

        AABB aabb = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        List<LivingEntity> entities = this.owner.level().getEntitiesOfClass(
                LivingEntity.class,
                aabb,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR
        );
        boolean hurtAny = false;
        for (LivingEntity living : entities) {
            if (living.is(this.owner)
                    || this.owner.isAlliedTo(living)
                    || living.getType() == this.owner.getType()
                    || living.distanceToSqr(center) > radius * radius
                    || damagedThisTick.contains(living.getId())
                    || requireLineOfSight && !canEntityBeHurtFrom(center, living)) {
                continue;
            }
            if (living.hurt(damageSource, damageAmount)) {
                damagedThisTick.add(living.getId());
                hurtAny = true;
                knockbackTarget(living, knockbackAmount);
            }
        }
        return hurtAny;
    }

    private boolean canEntityBeHurtFrom(Vec3 center, LivingEntity living) {
        Vec3 targetCenter = living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
        return this.owner.level().clip(new ClipContext(
                center,
                targetCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.owner
        )).getType() == HitResult.Type.MISS;
    }

    private void knockbackTarget(Entity target, double strength) {
        if (strength <= 0.0D) {
            return;
        }

        double resistance = target instanceof LivingEntity living
                ? living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)
                : 0.0D;
        double appliedStrength = strength * Math.max(0.0D, 1.0D - resistance);
        if (appliedStrength <= 0.0D) {
            return;
        }

        Vec3 push = target.position().subtract(this.owner.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(appliedStrength);
        Vec3 movement = target.getDeltaMovement();
        target.setDeltaMovement(
                movement.x * 0.5D + push.x,
                Math.min(0.4D, movement.y * 0.5D + appliedStrength),
                movement.z * 0.5D + push.z
        );
        target.hasImpulse = true;
    }

    private void mutateTerrainAround(
            Vec3 center,
            float radius,
            RoyalBeamSettings settings,
            RoyalBeamTerrainMode terrainMode
    ) {
        if (terrainMode == RoyalBeamTerrainMode.NONE || radius <= 0.0F) {
            return;
        }

        Level level = this.owner.level();
        int minX = Mth.floor(center.x - radius);
        int minY = Mth.floor(center.y - radius);
        int minZ = Mth.floor(center.z - radius);
        int maxX = Mth.floor(center.x + radius);
        int maxY = Mth.floor(center.y + radius);
        int maxZ = Mth.floor(center.z + radius);
        float radiusSqr = radius * radius;

        int cap = terrainMode == RoyalBeamTerrainMode.BUILD_ICE
                ? Math.min(ICE_MUTATIONS_PER_TICK, settings.maxTerrainMutationsPerTick())
                : settings.maxTerrainMutationsPerTick();
        for (BlockPos cursor : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (this.terrainMutationsThisTick >= cap) {
                return;
            }
            BlockPos pos = cursor.immutable();
            if (pos.distToCenterSqr(center.x, center.y, center.z) > radiusSqr
                    || this.owner.getRandom().nextFloat() > settings.terrainMutationChance()
                    || pos.getY() < level.getMinBuildHeight()
                    || pos.getY() >= level.getMaxBuildHeight()
                    || !level.getWorldBorder().isWithinBounds(pos)) {
                continue;
            }

            BlockState existing = level.getBlockState(pos);
            if (terrainMode == RoyalBeamTerrainMode.DESTROY) {
                destroyBeamBlock(level, pos, existing, settings);
            } else if (terrainMode == RoyalBeamTerrainMode.BUILD_ICE) {
                buildIceBlock(level, pos, existing, settings);
            }
        }
    }

    private void destroyBeamBlock(Level level, BlockPos pos, BlockState existing, RoyalBeamSettings settings) {
        if (existing.isAir()
                || level.getBlockEntity(pos) != null
                || existing.getDestroySpeed(level, pos) < 0.0F
                || existing.getBlock().getExplosionResistance() > settings.maxTerrainResistance()
                || existing.getCollisionShape(level, pos).isEmpty()) {
            return;
        }

        if (this.owner.getRandom().nextFloat() <= settings.terrainDropChance()) {
            level.destroyBlock(pos, true, this.owner);
        } else {
            level.levelEvent(2001, pos, Block.getId(existing));
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        this.terrainMutationsThisTick++;
    }

    private void buildIceBlock(Level level, BlockPos pos, BlockState existing, RoyalBeamSettings settings) {
        boolean replaceable = existing.isAir()
                || existing.canBeReplaced()
                || !existing.getFluidState().isEmpty();
        if (!replaceable || level.getBlockEntity(pos) != null || existing.is(BlockTags.ICE)) {
            return;
        }
        if (!touchesFreezableSurface(level, pos)) {
            return;
        }

        level.levelEvent(2001, pos, Block.getId(existing));
        level.setBlock(pos, randomIceBlock().defaultBlockState(), Block.UPDATE_ALL);
        this.terrainMutationsThisTick++;
    }

    private static boolean touchesFreezableSurface(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (state.is(BlockTags.ICE) || state.is(Blocks.SNOW_BLOCK)) {
                continue;
            }
            if (state.isFaceSturdy(level, neighbor, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private Block randomIceBlock() {
        return switch (this.owner.getRandom().nextInt(5)) {
            case 0 -> Blocks.BLUE_ICE;
            case 1 -> Blocks.PACKED_ICE;
            case 2 -> Blocks.FROSTED_ICE;
            case 3 -> Blocks.SNOW_BLOCK;
            default -> Blocks.ICE;
        };
    }
}
