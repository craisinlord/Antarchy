package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.state.PotentNyxiteState;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class PotentNyxiteBlock extends BaseEntityBlock {
    private static final ResourceLocation ANTIWATER_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antiwater");
    private static final ResourceLocation FLOWING_ANTIWATER_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flowing_antiwater");
    private static final ResourceLocation ICHOR_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ichor");
    private static final ResourceLocation FLOWING_ICHOR_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flowing_ichor");
    private static final ResourceLocation HYPNOTIC_GAS_SOUND_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "potent_nyxite_hypnotic_gas");
    private static final ResourceLocation HYPNOTIC_GAS_CLOUD_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hypnotic_gas_cloud");
    private static final ResourceLocation HYPNOTIC_GAS_CLOUD_DOWN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hypnotic_gas_cloud_down");

    public static final EnumProperty<PotentNyxiteState> STATE = EnumProperty.create("state", PotentNyxiteState.class);

    private final Supplier<? extends BlockEntityType<PotentNyxiteBlockEntity>> blockEntityTypeSupplier;
    private final MapCodec<PotentNyxiteBlock> codec;

    public PotentNyxiteBlock(
            Supplier<? extends BlockEntityType<PotentNyxiteBlockEntity>> blockEntityTypeSupplier,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.codec = Block.simpleCodec(ignored -> this);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, PotentNyxiteState.DRY));
    }

    @Override
    public MapCodec<PotentNyxiteBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotentNyxiteBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(STATE, resolveState(context.getLevel(), context.getClickedPos(), this.defaultBlockState()));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return state.setValue(STATE, resolveState(level, pos, state));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), PotentNyxiteBlockEntity::clientTick)
                : createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (tickLevel instanceof ServerLevel serverLevel) {
                        PotentNyxiteBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
                    }
                });
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        PotentNyxiteBlockEntity.handleStateActivation(level, pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PotentNyxiteBlockEntity potentNyxiteBlockEntity) {
            potentNyxiteBlockEntity.markEruptionStart(level.getGameTime());
        }
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction fluidDirection = getFluidColumnDirection(level, pos);
        if (state.getValue(STATE) == PotentNyxiteState.DRY || fluidDirection == null) {
            return;
        }

        Direction eruptionDirection = fluidDirection.getOpposite();
        BlockPos particlePos = pos.relative(eruptionDirection);
        ResourceLocation particleId = eruptionDirection == Direction.DOWN ? HYPNOTIC_GAS_CLOUD_DOWN_ID : HYPNOTIC_GAS_CLOUD_ID;
        spawnHypnoticGasParticlesAt(level, random, particlePos, particleId, eruptionDirection);
        if (random.nextBoolean()) {
            spawnHypnoticGasParticlesAt(level, random, particlePos, particleId, eruptionDirection);
        }

        if (random.nextInt(10) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    BuiltInRegistries.SOUND_EVENT.get(HYPNOTIC_GAS_SOUND_ID),
                    SoundSource.AMBIENT,
                    1.75F,
                    1.0F,
                    false
            );
        }
    }

    public static PotentNyxiteState resolveState(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction fluidDirection = getFluidColumnDirection(level, pos);
        if (fluidDirection == null) {
            return hasHypnoticGasFluid(level, pos) ? PotentNyxiteState.WET : PotentNyxiteState.DRY;
        }

        if (hasContinuousTrigger(level, pos, fluidDirection)) {
            return PotentNyxiteState.CONTINUOUS;
        }

        if (hasPeriodicTrigger(level, pos, fluidDirection)) {
            return PotentNyxiteState.DORMANT;
        }

        return PotentNyxiteState.WET;
    }

    public static boolean hasPeriodicTrigger(LevelAccessor level, BlockPos pos) {
        Direction fluidDirection = getFluidColumnDirection(level, pos);
        return fluidDirection != null && hasPeriodicTrigger(level, pos, fluidDirection);
    }

    public static boolean hasHypnoticGasFluid(LevelAccessor level, BlockPos pos) {
        return hasGasFluid(level, pos, Direction.UP) || hasGasFluid(level, pos, Direction.DOWN);
    }

    public static boolean hasContinuousTrigger(LevelAccessor level, BlockPos pos) {
        Direction fluidDirection = getFluidColumnDirection(level, pos);
        return fluidDirection != null && hasContinuousTrigger(level, pos, fluidDirection);
    }

    public static boolean isAntiwater(FluidState fluidState) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return ANTIWATER_ID.equals(fluidId) || FLOWING_ANTIWATER_ID.equals(fluidId);
    }

    public static boolean isIchor(FluidState fluidState) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return ICHOR_ID.equals(fluidId) || FLOWING_ICHOR_ID.equals(fluidId);
    }

    public static Direction getEruptionDirection(LevelAccessor level, BlockPos pos) {
        Direction fluidDirection = getFluidColumnDirection(level, pos);
        if (fluidDirection != null) {
            return fluidDirection.getOpposite();
        }

        return Direction.UP;
    }

    public static Direction getFluidColumnDirection(LevelAccessor level, BlockPos pos) {
        boolean hasUpFluid = hasValidFluidColumn(level, pos, Direction.UP);
        boolean hasDownFluid = hasValidFluidColumn(level, pos, Direction.DOWN);
        if (!hasUpFluid && !hasDownFluid) {
            return null;
        }

        if (hasUpFluid && !hasDownFluid) {
            return Direction.UP;
        }

        if (hasDownFluid && !hasUpFluid) {
            return Direction.DOWN;
        }

        boolean hasUpHeat = hasContinuousTrigger(level, pos, Direction.UP);
        boolean hasDownHeat = hasContinuousTrigger(level, pos, Direction.DOWN);
        if (hasUpHeat && !hasDownHeat) {
            return Direction.DOWN;
        }

        if (hasDownHeat && !hasUpHeat) {
            return Direction.UP;
        }

        boolean hasUpTag = hasPeriodicTrigger(level, pos, Direction.UP);
        boolean hasDownTag = hasPeriodicTrigger(level, pos, Direction.DOWN);
        if (hasUpTag && !hasDownTag) {
            return Direction.DOWN;
        }

        if (hasDownTag && !hasUpTag) {
            return Direction.UP;
        }

        return Direction.UP;
    }

    public static boolean hasAntiwaterOnSide(LevelAccessor level, BlockPos pos, Direction direction) {
        FluidState fluidState = level.getFluidState(pos.relative(direction));
        return fluidState.isSource() && isAntiwater(fluidState);
    }

    public static boolean hasGasFluid(LevelAccessor level, BlockPos pos, Direction direction) {
        FluidState fluidState = level.getFluidState(pos.relative(direction));
        return fluidState.isSource() && isAntiwater(fluidState);
    }

    public static boolean hasPeriodicTrigger(LevelAccessor level, BlockPos pos, Direction direction) {
        int fluidBlocks = getFluidColumnDepth(level, pos, direction);
        return fluidBlocks > 0
                && level.getBlockState(pos.relative(direction, fluidBlocks + 1)).is(AntarchyTags.Blocks.POTENT_NYXITE_ACTIVATION_BLOCKS);
    }

    public static boolean hasContinuousTrigger(LevelAccessor level, BlockPos pos, Direction direction) {
        int fluidBlocks = getFluidColumnDepth(level, pos, direction);
        if (fluidBlocks <= 0) {
            return false;
        }

        FluidState fluidState = level.getFluidState(pos.relative(direction, fluidBlocks + 1));
        return fluidState.isSourceOfType(Fluids.LAVA);
    }

    public static int getFluidColumnDepth(LevelAccessor level, BlockPos pos, Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return 0;
        }

        int depth = 0;
        BlockPos.MutableBlockPos cursor = pos.relative(direction).mutable();
        while (true) {
            FluidState fluidState = level.getFluidState(cursor);
            if (!fluidState.isSource() || !isAntiwater(fluidState)) {
                break;
            }

            depth++;
            cursor.move(direction);
        }

        if (depth <= 0) {
            return 0;
        }

        BlockState terminatorState = level.getBlockState(cursor);
        FluidState terminatorFluidState = level.getFluidState(cursor);
        if (terminatorFluidState.isSourceOfType(Fluids.LAVA) || terminatorState.is(AntarchyTags.Blocks.POTENT_NYXITE_ACTIVATION_BLOCKS)) {
            return depth;
        }

        return 0;
    }

    private static boolean hasValidFluidColumn(LevelAccessor level, BlockPos pos, Direction direction) {
        return getFluidColumnDepth(level, pos, direction) > 0;
    }

    private static void spawnHypnoticGasParticlesAt(
            Level level,
            RandomSource random,
            BlockPos particlePos,
            ResourceLocation particleId,
            Direction eruptionDirection
    ) {
        level.addAlwaysVisibleParticle(
                simpleParticle(particleId),
                particlePos.getX() + 0.2D + random.nextDouble() * 0.6D,
                particlePos.getY() + 0.1D + random.nextDouble() * 0.35D,
                particlePos.getZ() + 0.2D + random.nextDouble() * 0.6D,
                0.0D,
                eruptionDirection == Direction.UP ? 0.01D : -0.01D,
                0.0D
        );
    }

    private static SimpleParticleType simpleParticle(ResourceLocation id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(id);
    }
}
