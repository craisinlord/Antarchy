package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.block.state.PotentNyxiteState;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PotentNyxiteBlockEntity extends BlockEntity {
    public static final long GEYSER_SALT = -904011478L;

    private static final ResourceLocation HYPNOTIC_GAS_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hypnotic_gas");
    private static final ResourceLocation HYPNOTIC_GAS_DOWN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hypnotic_gas_down");
    private static final ResourceLocation HYPNOTIC_GAS_CLOUD_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hypnotic_gas_cloud");
    private static final ResourceLocation HYPNOTIC_GAS_CLOUD_DOWN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hypnotic_gas_cloud_down");
    private static final ResourceLocation INVERTED_GEYSER_ERUPTION_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "inverted_geyser_eruption");
    private static final ResourceLocation GEYSER_ERUPTION_START_SOUND_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "potent_nyxite_geyser_eruption_start");
    private static final ResourceLocation GEYSER_ERUPTION_ACTIVE_SOUND_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "potent_nyxite_geyser_eruption_active");
    private static final ResourceLocation GEYSER_CONTINUOUS_START_SOUND_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "potent_nyxite_geyser_continuous_start");
    private static final ResourceLocation GEYSER_CONTINUOUS_ACTIVE_SOUND_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "potent_nyxite_geyser_continuous_active");

    private static final int MAX_FLUID_COLUMN_STEPS = 5;
    private static final Predicate<Entity> EFFECT_PREDICATE = EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE);

    private int waitingCountdown = -1;
    private long eruptionTick = -1L;

    public PotentNyxiteBlockEntity(
            BlockPos pos,
            BlockState blockState,
            Supplier<? extends BlockEntityType<PotentNyxiteBlockEntity>> blockEntityTypeSupplier
    ) {
        super(blockEntityTypeSupplier.get(), pos, blockState);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, PotentNyxiteBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PotentNyxiteBlockEntity blockEntity) {
        if (level instanceof ClientLevel clientLevel) {
            blockEntity.clientTick(clientLevel, pos, state);
        }
    }

    public static void handleStateActivation(Level level, BlockPos pos, BlockState state) {
        PotentNyxiteState stateValue = state.getValue(PotentNyxiteBlock.STATE);
        if (stateValue != PotentNyxiteState.ERUPTING && stateValue != PotentNyxiteState.CONTINUOUS) {
            return;
        }

        level.blockEvent(pos, state.getBlock(), 0, 0);
        level.playSound(
                null,
                pos,
                BuiltInRegistries.SOUND_EVENT.get(
                        stateValue == PotentNyxiteState.CONTINUOUS ? GEYSER_CONTINUOUS_START_SOUND_ID : GEYSER_ERUPTION_START_SOUND_ID
                ),
                net.minecraft.sounds.SoundSource.BLOCKS,
                2.5F,
                1.0F
        );
    }

    public void markEruptionStart(long gameTime) {
        this.eruptionTick = gameTime;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (this.eruptionTick == -1L) {
            this.eruptionTick = level.getGameTime();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("countdown", this.waitingCountdown);
        tag.putLong("eruption_tick", this.eruptionTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.waitingCountdown = tag.getInt("countdown");
        this.eruptionTick = tag.getLong("eruption_tick");
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof PotentNyxiteBlock)) {
            return;
        }

        Direction fluidDirection = PotentNyxiteBlock.getFluidColumnDirection(level, pos);
        BlockPos source = fluidDirection == null ? null : findHypnoticGasSourceBlock(level, pos, fluidDirection);
        BlockPos adjustedSource = source == null || fluidDirection == null ? null : getAdjustedSource(source, fluidDirection);
        if (level.getGameTime() % 30L == 0L && adjustedSource != null) {
            applyHypnoticGasEffects(level, adjustedSource, fluidDirection);
        }

        PotentNyxiteState environmentState = PotentNyxiteBlock.resolveState(level, pos, state);
        PotentNyxiteState currentState = state.getValue(PotentNyxiteBlock.STATE);

        if (environmentState == PotentNyxiteState.CONTINUOUS) {
            if (currentState != PotentNyxiteState.CONTINUOUS) {
                BlockState newState = state.setValue(PotentNyxiteBlock.STATE, PotentNyxiteState.CONTINUOUS);
                level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
                handleStateActivation(level, pos, newState);
                this.setChanged();
            }
            this.waitingCountdown = -1;
            launchEntities(level, pos);
            return;
        }

        if (environmentState == PotentNyxiteState.DORMANT) {
            if (currentState != PotentNyxiteState.DORMANT && currentState != PotentNyxiteState.ERUPTING) {
                level.setBlock(pos, state.setValue(PotentNyxiteBlock.STATE, PotentNyxiteState.DORMANT), Block.UPDATE_CLIENTS);
                currentState = PotentNyxiteState.DORMANT;
            }

            if (source != null && fluidDirection != null && level.getGameTime() % 20L == 0L) {
                int fluidBlocks = getFluidBlocks(level, pos, source, fluidDirection);
                RandomSource geyserRandom = geyserPositional(level, pos);
                if (this.waitingCountdown <= 0) {
                    if (currentState == PotentNyxiteState.ERUPTING) {
                        geyserRandom.nextInt();
                        this.waitingCountdown = Math.max(1, fluidBlocks - 1) + geyserRandom.nextIntBetweenInclusive(1, 2);
                    } else {
                        this.waitingCountdown = 10 * Math.max(0, fluidBlocks - 1) + geyserRandom.nextIntBetweenInclusive(15, 30);
                    }
                }

                if (this.waitingCountdown > 0) {
                    this.waitingCountdown--;
                }

                if (this.waitingCountdown == 0) {
                    PotentNyxiteState nextState = currentState == PotentNyxiteState.ERUPTING
                            ? PotentNyxiteState.DORMANT
                            : PotentNyxiteState.ERUPTING;
                    BlockState newState = state.setValue(PotentNyxiteBlock.STATE, nextState);
                    level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
                    if (nextState == PotentNyxiteState.ERUPTING) {
                        handleStateActivation(level, pos, newState);
                        level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(newState));
                    } else {
                        level.gameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(newState));
                    }
                    currentState = nextState;
                    this.setChanged();
                }
            }

            if (currentState == PotentNyxiteState.ERUPTING) {
                launchEntities(level, pos);
            }
            return;
        }

        PotentNyxiteState fallbackState = environmentState == PotentNyxiteState.WET ? PotentNyxiteState.WET : PotentNyxiteState.DRY;
        if (currentState != fallbackState) {
            level.setBlock(pos, state.setValue(PotentNyxiteBlock.STATE, fallbackState), Block.UPDATE_CLIENTS);
        }
        this.waitingCountdown = -1;
    }

    private void clientTick(ClientLevel level, BlockPos pos, BlockState state) {
        Direction fluidDirection = PotentNyxiteBlock.getFluidColumnDirection(level, pos);
        if (fluidDirection == null) {
            return;
        }

        BlockPos source = findHypnoticGasSourceBlock(level, pos, fluidDirection);
        Direction eruptionDirection = PotentNyxiteBlock.getEruptionDirection(level, pos);
        if (source != null && level.getGameTime() % 4L == 0L) {
            boolean flipped = eruptionDirection == Direction.DOWN;
            RandomSource random = level.getRandom();
            int count = flipped ? 3 : 4;
            Vec3 center = Vec3.atCenterOf(source);
            for (int i = 0; i < count; i++) {
                double angle = random.nextDouble() * Mth.TWO_PI;
                double radius = 0.18D + random.nextDouble() * 0.32D;
                double outwardX = Mth.cos((float) angle) * radius;
                double outwardZ = Mth.sin((float) angle) * radius;
                double x = center.x + outwardX;
                double z = center.z + outwardZ;
                double y = source.getY() + (flipped ? 0.72D : 0.18D) + random.nextDouble() * 0.20D;
                double driftX = outwardX * 0.015D;
                double driftY = flipped ? 0.02D : 0.015D;
                double driftZ = outwardZ * 0.015D;
                level.addAlwaysVisibleParticle(
                        simpleParticle(flipped ? HYPNOTIC_GAS_DOWN_ID : HYPNOTIC_GAS_ID),
                        x,
                        y,
                        z,
                        driftX,
                        driftY,
                        driftZ
                );
            }
        }
        if (source != null && level.getGameTime() % 20L == 0L) {
            ResourceLocation cloudParticleId = eruptionDirection == Direction.DOWN
                    ? HYPNOTIC_GAS_CLOUD_DOWN_ID
                    : HYPNOTIC_GAS_CLOUD_ID;
            level.addParticle(simpleParticle(cloudParticleId), source.getX() + 0.5D, source.getY() + 0.5D, source.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
        }

        PotentNyxiteState stateValue = state.getValue(PotentNyxiteBlock.STATE);
        if ((stateValue == PotentNyxiteState.ERUPTING || stateValue == PotentNyxiteState.CONTINUOUS) && source != null) {
            int fluidBlocks = getFluidBlocks(level, pos, source, fluidDirection);
            long eruptionTime = level.getGameTime() - this.eruptionTick;

            if (eruptionDirection == Direction.DOWN && eruptionTime % 20L == 0L) {
                spawnDownwardHypnoticGasTrail(level, source, fluidBlocks);
            }

            if (eruptionTime % 20L == 0L) {
                level.addParticle(
                        new InvertedGeyserParticleOptions(geyserParticleType(INVERTED_GEYSER_ERUPTION_ID), fluidBlocks, eruptionDirection),
                        source.getX() + 0.5D,
                        source.getY(),
                        source.getZ() + 0.5D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }

            if (eruptionTime % 40L == 0L) {
                level.playLocalSound(
                        source.getX() + 0.5D,
                        source.getY() + 0.5D,
                        source.getZ() + 0.5D,
                        BuiltInRegistries.SOUND_EVENT.get(
                                stateValue == PotentNyxiteState.CONTINUOUS ? GEYSER_CONTINUOUS_ACTIVE_SOUND_ID : GEYSER_ERUPTION_ACTIVE_SOUND_ID
                        ),
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        1.0F,
                        1.0F,
                        false
                );
            }
        }
    }

    private static void applyHypnoticGasEffects(ServerLevel level, BlockPos source, Direction fluidDirection) {
        for (LivingEntity entity : getNearbyLivingEntities(level, source)) {
            if (canBeReachedByHypnoticGas(level, source, entity.getEyePosition(), fluidDirection)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 0, true, true));
            }
        }
    }

    private void launchEntities(ServerLevel level, BlockPos pos) {
        Direction fluidDirection = PotentNyxiteBlock.getFluidColumnDirection(level, pos);
        if (fluidDirection == null) {
            return;
        }

        BlockPos source = findHypnoticGasSourceBlock(level, pos, fluidDirection);
        if (source == null) {
            return;
        }

        Direction eruptionDirection = PotentNyxiteBlock.getEruptionDirection(level, pos);
        int fluidBlocks = getFluidBlocks(level, pos, source, fluidDirection);
        int unobstructedBlocks = getUnobstructedBlockCount(level, source, eruptionDirection, fluidBlocks);
        if (unobstructedBlocks <= 0) {
            return;
        }

        BlockPos start = source;
        BlockPos end = source.relative(eruptionDirection, unobstructedBlocks - 1);
        AABB area = new AABB(
                Math.min(start.getX(), end.getX()),
                Math.min(start.getY(), end.getY()),
                Math.min(start.getZ(), end.getZ()),
                Math.max(start.getX(), end.getX()) + 1.0D,
                Math.max(start.getY(), end.getY()) + 1.0D,
                Math.max(start.getZ(), end.getZ()) + 1.0D
        );

        Vec3 worldPush = new Vec3(0.0D, eruptionDirection.getStepY() * 0.2D, 0.0D);
        double velocityThreshold = 0.3D + fluidBlocks * 0.1D;
        int invertedDurationTicks = Mth.floor(AntarchySettings.potentNyxiteInvertedDurationSeconds() * 20.0D);
        Holder<MobEffect> invertedEffect = AntarchyObjects.INVERTED_EFFECT.get();

        for (Entity entity : level.getEntitiesOfClass(Entity.class, area, EFFECT_PREDICATE)) {
            if (entity instanceof Player player && player.getAbilities().flying) {
                continue;
            }

            AntarchyGravityDirection gravityDirection = AntarchyGravityApi.getGravityDirection(entity);
            Vec3 currentWorldVelocity = gravityDirection.isInverted()
                    ? AntarchyGravityRotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), gravityDirection)
                    : entity.getDeltaMovement();

            double axisVelocity = currentWorldVelocity.y * eruptionDirection.getStepY();
            if (axisVelocity <= velocityThreshold) {
                Vec3 nextWorldVelocity = currentWorldVelocity.add(worldPush);
                entity.setDeltaMovement(
                        gravityDirection.isInverted()
                                ? AntarchyGravityRotationUtil.vecWorldToPlayer(nextWorldVelocity, gravityDirection)
                                : nextWorldVelocity
                );
                entity.hurtMarked = true;
                entity.hasImpulse = true;
                entity.fallDistance = 0.0F;
            }

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(invertedEffect, invertedDurationTicks, 0, false, true, true));
            }
        }
    }

    private static List<LivingEntity> getNearbyLivingEntities(Level level, BlockPos source) {
        AABB aabb = new AABB(source).inflate(2.5D, 0.0D, 2.5D);
        return level.getEntitiesOfClass(LivingEntity.class, aabb, entity -> EFFECT_PREDICATE.test(entity));
    }

    public static RandomSource geyserPositional(ServerLevel level, BlockPos pos) {
        return new net.minecraft.world.level.levelgen.XoroshiroRandomSource(level.getSeed() ^ GEYSER_SALT).forkPositional().at(pos);
    }

    public static BlockPos findHypnoticGasSourceBlock(LevelAccessor level, BlockPos origin, Direction direction) {
        if (direction == Direction.DOWN) {
            return origin.above();
        }

        return findLegacyHypnoticGasSourceBlock(level, origin, direction);
    }

    private static BlockPos findLegacyHypnoticGasSourceBlock(LevelAccessor level, BlockPos origin, Direction direction) {
        BlockPos.MutableBlockPos cursor = origin.relative(direction).mutable();

        for (int i = 0; i <= MAX_FLUID_COLUMN_STEPS; i++) {
            FluidState fluidState = level.getFluidState(cursor);
            BlockState blockState = level.getBlockState(cursor);

            if (fluidState.isSource() && isHypnoticGasFluid(fluidState) && isGeyserPassableBlock(level, cursor)) {
                cursor.move(direction);
                continue;
            }

            if (isGeyserPassableBlock(level, cursor)) {
                return cursor.immutable();
            }

            return null;
        }

        return null;
    }

    public static Vec3 pickRandomHypnoticGasSpawnPoint(Level level, BlockPos source) {
        RandomSource random = level.getRandom();
        Vec3 offset = new Vec3(random.nextFloat() - 0.5F, 0.0D, random.nextFloat() - 0.5F).normalize();
        float distance = random.nextFloat() * 3.0F;
        return Vec3.atCenterOf(source).add(offset.scale(distance)).subtract(0.0D, 0.25D, 0.0D);
    }

    public static boolean canBeReachedByHypnoticGas(Level level, BlockPos source, Vec3 pos) {
        Direction fluidDirection = isHypnoticGasFluid(level.getFluidState(source.below())) ? Direction.DOWN : Direction.UP;
        return canBeReachedByHypnoticGas(level, source, pos, fluidDirection);
    }

    public static boolean canBeReachedByHypnoticGas(Level level, BlockPos source, Vec3 pos, Direction fluidDirection) {
        BlockPos blockPos = BlockPos.containing(pos);
        if (!isGeyserPassableBlock(level, blockPos)) {
            return false;
        }

        if (pos.distanceToSqr(Vec3.atCenterOf(source)) > 9.0D) {
            return false;
        }

        if (fluidDirection == Direction.DOWN) {
            return haveLineOfSight(level, Vec3.atCenterOf(source), pos);
        }

        Direction adjacentFluidDirection = findAdjacentFluidDirection(level, source);
        if (adjacentFluidDirection == null) {
            return false;
        }

        Vec3 sourceSupport = Vec3.atCenterOf(source.relative(adjacentFluidDirection));
        Vec3 targetSupport = pos.add(
                adjacentFluidDirection.getStepX(),
                adjacentFluidDirection.getStepY(),
                adjacentFluidDirection.getStepZ()
        );
        return isHypnoticGasFluid(level.getFluidState(BlockPos.containing(targetSupport)))
                && haveLineOfSight(level, sourceSupport, targetSupport);
    }

    public static void spawnHypnoticGasParticle(Level level, Vec3 pos, boolean flipped) {
        level.addAlwaysVisibleParticle(simpleParticle(flipped ? HYPNOTIC_GAS_DOWN_ID : HYPNOTIC_GAS_ID), pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnHypnoticGasParticle(Level level, Vec3 pos) {
        spawnHypnoticGasParticle(level, pos, false);
    }

    private static void spawnDownwardHypnoticGasTrail(ClientLevel level, BlockPos source, int fluidBlocks) {
        RandomSource random = level.getRandom();
        int particleCount = Math.max(3, fluidBlocks + 1);
        for (int i = 0; i < particleCount; i++) {
            double progress = particleCount == 1 ? 0.0D : (double) i / (double) (particleCount - 1);
            double y = source.getY() + 0.35D - progress * Math.max(1, fluidBlocks);
            double x = source.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            double z = source.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            level.addParticle(simpleParticle(HYPNOTIC_GAS_DOWN_ID), x, y, z, 0.0D, -0.01D, 0.0D);
        }
    }

    private static BlockPos getAdjustedSource(BlockPos source, Direction direction) {
        return direction == Direction.DOWN ? source.above() : source;
    }

    private static Direction findAdjacentFluidDirection(LevelAccessor level, BlockPos source) {
        if (isHypnoticGasFluid(level.getFluidState(source.above()))) {
            return Direction.UP;
        }
        if (isHypnoticGasFluid(level.getFluidState(source.below()))) {
            return Direction.DOWN;
        }
        return null;
    }

    private static int getFluidBlocks(LevelAccessor level, BlockPos origin, BlockPos source, Direction fluidDirection) {
        if (fluidDirection == Direction.DOWN) {
            return PotentNyxiteBlock.getFluidColumnDepth(level, origin, fluidDirection);
        }

        return fluidBlocksBetween(origin, source, fluidDirection);
    }

    private static int fluidBlocksBetween(BlockPos origin, BlockPos source, Direction fluidDirection) {
        return Math.max(1, Math.abs(source.getY() - origin.getY()) - 1);
    }

    private static int getUnobstructedBlockCount(LevelAccessor level, BlockPos start, Direction direction, int fluidBlocks) {
        int geyserForceHeight = 6 * Math.max(1, fluidBlocks);
        for (int i = 0; i < geyserForceHeight; i++) {
            BlockPos currentPos = start.relative(direction, i);
            if (!isGeyserPassableBlock(level, currentPos)) {
                return i;
            }
        }
        return geyserForceHeight;
    }

    private static boolean isGeyserPassableBlock(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        return state.isAir()
                || (fluidState.isSource() && isHypnoticGasFluid(fluidState))
                || state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isHypnoticGasFluid(FluidState fluidState) {
        return PotentNyxiteBlock.isAntiwater(fluidState);
    }

    private static boolean haveLineOfSight(Level level, Vec3 from, Vec3 to) {
        HitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return hitResult.getType() != HitResult.Type.BLOCK;
    }

    private static SimpleParticleType simpleParticle(ResourceLocation id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(id);
    }

    @SuppressWarnings("unchecked")
    private static ParticleType<InvertedGeyserParticleOptions> geyserParticleType(ResourceLocation id) {
        return (ParticleType<InvertedGeyserParticleOptions>) BuiltInRegistries.PARTICLE_TYPE.get(id);
    }
}
