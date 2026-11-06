package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class GiantLilyPadBlock extends Block implements BonemealableBlock {
    public static final EnumProperty<TilePosition> TILE_POSITION = EnumProperty.create("tile_position", TilePosition.class);
    public static final EnumProperty<PadRotation> ROTATION = EnumProperty.create("rotation", PadRotation.class);
    public static final BooleanProperty HAS_LOTUS = BooleanProperty.create("has_lotus");

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.5D, 16.0D);
    private static final ThreadLocal<Boolean> REMOVING_STRUCTURE = ThreadLocal.withInitial(() -> false);
    private static final Map<TilePosition, Vec2> OFFSETS = createOffsets();
    private static final double LAUNCH_VERTICAL_SPEED = 0.82D;
    private static final double LAUNCH_SPEED_PER_BLOCK = 0.1044D;
    private static final double NON_LIVING_HORIZONTAL_SCALE = 0.45D;
    private static final double NON_LIVING_VERTICAL_SCALE = 0.7D;
    private static final double PAD_SURFACE_HEIGHT = 0.25D;
    private static final float LAUNCH_SOUND_PITCH = 1.4F;
    private static final int LAUNCH_PARTICLE_COUNT = 12;

    public GiantLilyPadBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TILE_POSITION, TilePosition.CENTER)
                .setValue(ROTATION, PadRotation.DEG_0)
                .setValue(HAS_LOTUS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TILE_POSITION, ROTATION, HAS_LOTUS);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(TILE_POSITION, TilePosition.CENTER)
                .setValue(ROTATION, rotationForPlacement(context.getClickedPos()));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return !structureHasLotus(level, centerFrom(pos, state));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos center = centerFrom(pos, state);
        if (structureHasLotus(level, center)) {
            return;
        }

        level.setBlock(pos, state.setValue(HAS_LOTUS, true), Block.UPDATE_CLIENTS);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!REMOVING_STRUCTURE.get()) {
            removeStructure(level, pos, state);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!stack.is(AntarchyObjects.LOTUS.get().asItem()) || structureHasLotus(level, centerFrom(pos, state))) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        level.setBlock(pos, state.setValue(HAS_LOTUS, true), Block.UPDATE_CLIENTS);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.LILY_PAD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(HAS_LOTUS) || random.nextInt(10) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
        double y = pos.getY() + 0.55D;
        level.addParticle(AntarchyObjects.LOTUS_POLLEN.get(), x, y, z, 0.0D, 0.015D, 0.0D);
    }

    public static void launchFromJump(Player player) {
        BlockPos pos = player.getOnPos();
        BlockState state = player.level().getBlockState(pos);
        if (!state.is(AntarchyObjects.GIANT_LILY_PAD.get()) || player.isSuppressingBounce()) {
            return;
        }

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        launch(player, forwardX, forwardZ);

        if (player.level() instanceof ServerLevel level) {
            launchOccupants(level, player, centerFrom(pos, state), forwardX, forwardZ);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.LILY_PAD_PLACE, SoundSource.BLOCKS, 1.0F, LAUNCH_SOUND_PITCH);
            level.sendParticles(ParticleTypes.SPLASH, player.getX(), pos.getY() + 0.1D, player.getZ(), LAUNCH_PARTICLE_COUNT, 0.5D, 0.05D, 0.5D, 0.0D);
        }
    }

    private static void launchOccupants(ServerLevel level, Player jumper, BlockPos center, double forwardX, double forwardZ) {
        AABB surface = new AABB(
                center.getX() - 1.0D,
                center.getY(),
                center.getZ() - 1.0D,
                center.getX() + 2.0D,
                center.getY() + PAD_SURFACE_HEIGHT,
                center.getZ() + 2.0D);
        for (Entity entity : level.getEntities(jumper, surface, GiantLilyPadBlock::canBeLaunched)) {
            launch(entity, forwardX, forwardZ);
            entity.needsSync = true;
        }
    }

    private static boolean canBeLaunched(Entity entity) {
        return entity.isAlive() && entity.onGround() && !entity.isSpectator() && !entity.isPassenger();
    }

    private static void launch(Entity entity, double forwardX, double forwardZ) {
        boolean living = entity instanceof LivingEntity;
        double horizontal = AntarchySettings.giantLilyPadLaunchDistance() * LAUNCH_SPEED_PER_BLOCK * (living ? 1.0D : NON_LIVING_HORIZONTAL_SCALE);
        double vertical = LAUNCH_VERTICAL_SPEED * (living ? 1.0D : NON_LIVING_VERTICAL_SCALE);
        entity.setDeltaMovement(forwardX * horizontal, Math.max(entity.getDeltaMovement().y, vertical), forwardZ * horizontal);
    }

    public static boolean canPlaceStructure(LevelAccessor level, BlockPos centerPos) {
        for (TilePosition tilePosition : TilePosition.values()) {
            BlockPos partPos = positionFor(centerPos, tilePosition);
            if (!level.isEmptyBlock(partPos) || !isSupported(level, partPos)) {
                return false;
            }
        }
        return true;
    }

    public static void placeStructure(LevelAccessor level, BlockPos centerPos, PadRotation rotation) {
        for (TilePosition tilePosition : TilePosition.values()) {
            BlockPos partPos = positionFor(centerPos, tilePosition);
            BlockState partState = AntarchyObjects.GIANT_LILY_PAD.get().defaultBlockState()
                    .setValue(TILE_POSITION, tilePosition)
                    .setValue(ROTATION, rotation)
                    .setValue(HAS_LOTUS, false);
            level.setBlock(partPos, partState, Block.UPDATE_CLIENTS);
        }
    }

    public static PadRotation rotationForPlacement(BlockPos pos) {
        long seed = pos.asLong() ^ Long.rotateLeft(pos.asLong(), 17);
        RandomSource random = RandomSource.create(seed);
        return PadRotation.values()[random.nextInt(4)];
    }

    private static boolean isSupported(LevelReader level, BlockPos pos) {
        return level.getFluidState(pos.below()).is(AntarchyTags.Fluids.GIANT_LILY_PAD_SUPPORTING_FLUIDS)
                && level.getFluidState(pos).isEmpty();
    }

    private static void removeStructure(Level level, BlockPos pos, BlockState state) {
        BlockPos center = centerFrom(pos, state);
        boolean shouldDropLotus = structureHasLotus(level, center) && !state.getValue(HAS_LOTUS);

        REMOVING_STRUCTURE.set(true);
        try {
            for (TilePosition tilePosition : TilePosition.values()) {
                BlockPos partPos = positionFor(center, tilePosition);
                if (partPos.equals(pos)) {
                    continue;
                }
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(AntarchyObjects.GIANT_LILY_PAD.get())) {
                    level.removeBlock(partPos, false);
                }
            }
        } finally {
            REMOVING_STRUCTURE.set(false);
        }

        if (shouldDropLotus && !level.isClientSide()) {
            popResource(level, pos, new ItemStack(AntarchyObjects.LOTUS.get()));
        }
    }

    private static boolean structureHasLotus(LevelReader level, BlockPos center) {
        for (TilePosition tilePosition : TilePosition.values()) {
            BlockState state = level.getBlockState(positionFor(center, tilePosition));
            if (state.is(AntarchyObjects.GIANT_LILY_PAD.get()) && state.getValue(HAS_LOTUS)) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos centerFrom(BlockPos pos, BlockState state) {
        Vec2 offset = OFFSETS.get(state.getValue(TILE_POSITION));
        return pos.offset((int) -offset.x, 0, (int) -offset.y);
    }

    private static BlockPos positionFor(BlockPos center, TilePosition tilePosition) {
        Vec2 offset = OFFSETS.get(tilePosition);
        return center.offset((int) offset.x, 0, (int) offset.y);
    }

    private static Map<TilePosition, Vec2> createOffsets() {
        Map<TilePosition, Vec2> offsets = new EnumMap<>(TilePosition.class);
        offsets.put(TilePosition.TOP_LEFT, new Vec2(-1, -1));
        offsets.put(TilePosition.TOP_MIDDLE, new Vec2(0, -1));
        offsets.put(TilePosition.TOP_RIGHT, new Vec2(1, -1));
        offsets.put(TilePosition.MIDDLE_LEFT, new Vec2(-1, 0));
        offsets.put(TilePosition.CENTER, new Vec2(0, 0));
        offsets.put(TilePosition.MIDDLE_RIGHT, new Vec2(1, 0));
        offsets.put(TilePosition.BOTTOM_LEFT, new Vec2(-1, 1));
        offsets.put(TilePosition.BOTTOM_MIDDLE, new Vec2(0, 1));
        offsets.put(TilePosition.BOTTOM_RIGHT, new Vec2(1, 1));
        return offsets;
    }

    public enum TilePosition implements StringRepresentable {
        TOP_LEFT("top_left"),
        TOP_MIDDLE("top_middle"),
        TOP_RIGHT("top_right"),
        MIDDLE_LEFT("middle_left"),
        CENTER("center"),
        MIDDLE_RIGHT("middle_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_MIDDLE("bottom_middle"),
        BOTTOM_RIGHT("bottom_right");

        private final String name;

        TilePosition(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum PadRotation implements StringRepresentable {
        DEG_0("0"),
        DEG_90("90"),
        DEG_180("180"),
        DEG_270("270");

        private final String name;

        PadRotation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
