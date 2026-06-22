package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyAdvancements;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DuctTapeBlock extends Block {
    public static final MapCodec<DuctTapeBlock> CODEC = Block.simpleCodec(DuctTapeBlock::new);
    public static final int MAX_USES = 4;
    private static final ResourceLocation DUCT_TAPE_ADVANCEMENT_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "duct_tape");
    public static final IntegerProperty USES = IntegerProperty.create("uses", 1, MAX_USES);
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final int TAPE_HEIGHT = 2;
    private static final long STICK_SOUND_COOLDOWN_TICKS = 10L;
    private static final long TAPE_DECAY_INTERVAL_TICKS = 30L;
    private static final double FLOOR_TRIGGER_HEIGHT = 0.30D;
    private static final double WALL_TRIGGER_DEPTH = 0.25D;
    private static final double TRIGGER_PAD = 0.12D;
    private static final double WALL_Y_PAD = 0.12D;
    private static final double WALL_SIDE_PAD = 0.08D;
    private static final double WALL_INNER_INSET = 0.03D;
    private static final double TRIGGER_INFLATE_DEFAULT = 0.15D;

    private static final Map<UUID, Long> STICK_SOUND_TIMES = new HashMap<>();
    private static final Map<UUID, String> STICK_CONTACT_KEYS = new HashMap<>();
    private static final Map<String, Long> TAPE_DECAY_TIMES = new HashMap<>();
    private static final Map<UUID, StuckContact> STUCK_ENTITIES = new HashMap<>();

    public DuctTapeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(USES, MAX_USES)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<DuctTapeBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
            case WALL -> {
                BlockPos supportPos = pos.relative(state.getValue(FACING).getOpposite());
                yield level.getBlockState(supportPos).isFaceSturdy(level, supportPos, state.getValue(FACING));
            }
            default -> false;
        };
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
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState state = this.defaultBlockState();
        if (clickedFace.getAxis().isHorizontal()) {
            state = state.setValue(FACE, AttachFace.WALL).setValue(FACING, clickedFace);
        } else {
            state = state.setValue(FACE, AttachFace.FLOOR).setValue(FACING, context.getHorizontalDirection());
        }

        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            releaseEntitiesForTape(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (stack.is(AntarchyObjects.DUCT_TAPE.get().asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (isReleaseTool(stack) && (isStuckToTape(player) || isTouchingTape(level, player))) {
            if (!level.isClientSide) {
                consumeTapeUse(level, pos, state);
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.2F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!stack.isDamageableItem() || !stack.isDamaged() || stack.is(AntarchyTags.Items.DUCT_TAPE_BLACKLIST)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int repairPerUse = Math.max(1, (int) Math.ceil(stack.getMaxDamage() * AntarchySettings.ductTapeRepairPercentPerUse()));
        int currentDamage = stack.getDamageValue();
        int repairedDamage = Math.min(currentDamage, repairPerUse);

        if (repairedDamage <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            stack.setDamageValue(currentDamage - repairedDamage);
            if (!player.getAbilities().instabuild) {
                consumeTapeUse(level, pos, state);
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                AntarchyAdvancements.award(serverPlayer, DUCT_TAPE_ADVANCEMENT_ID);
            }
            level.playSound(null, pos, AntarchySoundEvents.DUCT_TAPE_USE.get(), SoundSource.BLOCKS, 0.9F, 0.95F + level.random.nextFloat() * 0.1F);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isSpectator() || !touchesTape(level, entity, pos, state)) {
            return;
        }

        rememberStuckContact(level, pos, entity);
        if (!(entity instanceof ItemEntity)) {
            maybeConsumeTapeDecay(level, pos, state);
        }
        long gameTime = level.getGameTime();
        String tapeKey = tapeKey(level, pos);
        if (shouldPlayStickSound(entity.getUUID(), tapeKey, gameTime)) {
            level.playSound(null, pos, AntarchySoundEvents.DUCT_TAPE_USE.get(), SoundSource.BLOCKS, 0.85F, 0.9F + level.random.nextFloat() * 0.15F);
        }
        applyStick(state, pos, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(USES, FACE, FACING);
    }

    private static VoxelShape getShapeFor(BlockState state) {
        int depth = switch (state.getValue(USES)) {
            case 1 -> 4;
            case 2 -> 8;
            case 3 -> 12;
            default -> 16;
        };

        return switch (state.getValue(FACE)) {
            case FLOOR -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(0.0D, 0.0D, 16.0D - depth, 16.0D, TAPE_HEIGHT, 16.0D);
                case SOUTH -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, depth);
                case EAST -> Block.box(0.0D, 0.0D, 0.0D, depth, TAPE_HEIGHT, 16.0D);
                case WEST -> Block.box(16.0D - depth, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, 16.0D);
                default -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, 16.0D);
            };
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(0.0D, 16.0D - depth, 16.0D - TAPE_HEIGHT, 16.0D, 16.0D, 16.0D);
                case SOUTH -> Block.box(0.0D, 16.0D - depth, 0.0D, 16.0D, 16.0D, TAPE_HEIGHT);
                case EAST -> Block.box(0.0D, 16.0D - depth, 0.0D, TAPE_HEIGHT, 16.0D, 16.0D);
                case WEST -> Block.box(16.0D - TAPE_HEIGHT, 16.0D - depth, 0.0D, 16.0D, 16.0D, 16.0D);
                default -> Block.box(0.0D, 16.0D - depth, 0.0D, TAPE_HEIGHT, 16.0D, 16.0D);
            };
            default -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, 16.0D);
        };
    }

    private static boolean isReleaseTool(ItemStack stack) {
        return stack.getItem() instanceof DiggerItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof ShearsItem;
    }

    private static void consumeTapeUse(Level level, BlockPos pos, BlockState state) {
        int remainingUses = state.getValue(USES) - 1;
        if (remainingUses > 0) {
            level.setBlock(pos, state.setValue(USES, remainingUses), Block.UPDATE_ALL);
            return;
        }

        releaseEntitiesForTape(level, pos);
        TAPE_DECAY_TIMES.remove(tapeDecayKey(level, pos));
        level.removeBlock(pos, false);
    }

    private static void removeTape(Level level, BlockPos pos, BlockState state) {
        releaseEntitiesForTape(level, pos);
        TAPE_DECAY_TIMES.remove(tapeDecayKey(level, pos));
        level.removeBlock(pos, false);
    }

    private static void maybeConsumeTapeDecay(Level level, BlockPos pos, BlockState state) {
        long gameTime = level.getGameTime();
        String key = tapeDecayKey(level, pos);
        Long lastConsumed = TAPE_DECAY_TIMES.get(key);
        if (lastConsumed == null) {
            TAPE_DECAY_TIMES.put(key, gameTime);
            return;
        }

        if (gameTime - lastConsumed < TAPE_DECAY_INTERVAL_TICKS) {
            return;
        }

        TAPE_DECAY_TIMES.put(key, gameTime);
        consumeTapeUse(level, pos, state);
    }

    public static void tickStuckEntity(Entity entity) {
        if (entity == null || entity.level().isClientSide() || isStickyBlacklist(entity)) {
            return;
        }

        if (!entity.isAlive() || entity.isRemoved()) {
            releaseEntity(entity);
            return;
        }

        StuckContact contact = STUCK_ENTITIES.get(entity.getUUID());
        if (contact != null && isContactValid(entity.level(), contact)) {
            BlockPos pos = BlockPos.of(contact.blockPos());
            BlockState state = entity.level().getBlockState(pos);
            applyStick(state, pos, entity);
            if (!(entity instanceof ItemEntity)) {
                maybeConsumeTapeDecay(entity.level(), pos, state);
            }
            return;
        }

        if (contact != null) {
            releaseEntity(entity);
        }

        TapeHit hit = findTouchedTape(entity.level(), entity);
        if (hit == null) {
            if (entity.isNoGravity()) {
                entity.setNoGravity(false);
            }
            return;
        }

        rememberStuckContact(entity.level(), hit.pos(), entity);
        applyStick(hit.state(), hit.pos(), entity);
        if (!(entity instanceof ItemEntity)) {
            maybeConsumeTapeDecay(entity.level(), hit.pos(), hit.state());
        }
    }

    public static boolean isStuckToTape(Entity entity) {
        if (entity == null || entity.level().isClientSide() || isStickyBlacklist(entity)) {
            return false;
        }

        StuckContact contact = STUCK_ENTITIES.get(entity.getUUID());
        return contact != null && isContactValid(entity.level(), contact);
    }

    public static boolean isTouchingTape(Entity entity) {
        return entity != null && isTouchingTape(entity.level(), entity);
    }

    public static boolean isTouchingTape(Level level, Entity entity) {
        if (isStickyBlacklist(entity)) {
            return false;
        }

        return findTouchedTape(level, entity) != null;
    }

    private static boolean isStickyBlacklist(Entity entity) {
        return entity != null && entity.getType().is(AntarchyTags.Entities.DUCT_TAPE_STICK_BLACKLIST);
    }

    private static void applyStick(BlockState state, BlockPos pos, Entity entity) {
        entity.setDeltaMovement(Vec3.ZERO);
        entity.resetFallDistance();
        entity.hasImpulse = true;
        entity.setNoGravity(true);

        Vec3 anchor = getTapeAnchor(state, pos, entity);
        entity.setPos(anchor.x, anchor.y, anchor.z);

        if (state.getValue(FACE) == AttachFace.FLOOR) {
            entity.setOnGround(true);
        }
    }

    private static void rememberStuckContact(Level level, BlockPos pos, Entity entity) {
        UUID entityId = entity.getUUID();
        String key = tapeKey(level, pos);
        StuckContact existing = STUCK_ENTITIES.get(entityId);
        if (existing != null && existing.tapeKey().equals(key)) {
            return;
        }

        double offsetX = entity.getX() - pos.getX();
        double offsetY = entity.getY() - pos.getY();
        double offsetZ = entity.getZ() - pos.getZ();
        STUCK_ENTITIES.put(entityId, new StuckContact(
                key,
                level.dimension().location().toString(),
                pos.asLong(),
                clamp(offsetX, 0.05D, 0.95D),
                clamp(offsetY, 0.0D, 0.98D),
                clamp(offsetZ, 0.05D, 0.95D)
        ));
    }

    private static Vec3 getTapeAnchor(BlockState state, BlockPos pos, Entity entity) {
        StuckContact contact = STUCK_ENTITIES.get(entity.getUUID());
        double offsetX = contact != null ? contact.offsetX() : 0.5D;
        double offsetY = contact != null ? contact.offsetY() : 0.0D;
        double offsetZ = contact != null ? contact.offsetZ() : 0.5D;
        double wallOffset = entity instanceof Player ? 0.06D : -0.28D;
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        return switch (state.getValue(FACE)) {
            case FLOOR -> new Vec3(
                    pos.getX() + offsetX,
                    pos.getY() + 0.01D,
                    pos.getZ() + offsetZ
            );
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> new Vec3(centerX, pos.getY() + offsetY, pos.getZ() - wallOffset);
                case SOUTH -> new Vec3(centerX, pos.getY() + offsetY, pos.getZ() + 1.0D + wallOffset);
                case EAST -> new Vec3(pos.getX() + 1.0D + wallOffset, pos.getY() + offsetY, centerZ);
                case WEST -> new Vec3(pos.getX() - wallOffset, pos.getY() + offsetY, centerZ);
                default -> new Vec3(centerX, pos.getY() + offsetY, centerZ);
            };
            default -> new Vec3(pos.getX() + offsetX, pos.getY() + 0.01D, pos.getZ() + offsetZ);
        };
    }

    private static boolean isContactValid(Level level, StuckContact contact) {
        if (!level.dimension().location().toString().equals(contact.dimensionId())) {
            return false;
        }

        BlockPos pos = BlockPos.of(contact.blockPos());
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof DuctTapeBlock
                && tapeKey(level, pos).equals(contact.tapeKey());
    }

    private static TapeHit findTouchedTape(Level level, Entity entity) {
        AABB box = entity.getBoundingBox().inflate(0.30D);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (state.getBlock() instanceof DuctTapeBlock && touchesTape(level, entity, cursor, state)) {
                        return new TapeHit(cursor.immutable(), state);
                    }
                }
            }
        }
        return null;
    }

    private static boolean touchesTape(Level level, Entity entity, BlockPos pos, BlockState state) {
        return entity.getBoundingBox().intersects(getStickyTriggerBounds(state, pos));
    }

    private static AABB getStickyTriggerBounds(BlockState state, BlockPos pos) {
        AABB bounds = getShapeFor(state).bounds().move(pos);
        return switch (state.getValue(FACE)) {
            case FLOOR -> new AABB(
                    bounds.minX - TRIGGER_PAD,
                    pos.getY() - 0.02D,
                    bounds.minZ - TRIGGER_PAD,
                    bounds.maxX + TRIGGER_PAD,
                    pos.getY() + FLOOR_TRIGGER_HEIGHT,
                    bounds.maxZ + TRIGGER_PAD
            );
            case WALL -> {
                yield switch (state.getValue(FACING)) {
                    case NORTH -> new AABB(bounds.minX - WALL_SIDE_PAD, bounds.minY - WALL_Y_PAD, bounds.minZ - WALL_TRIGGER_DEPTH, bounds.maxX + WALL_SIDE_PAD, bounds.maxY + WALL_Y_PAD, bounds.maxZ + WALL_INNER_INSET);
                    case SOUTH -> new AABB(bounds.minX - WALL_SIDE_PAD, bounds.minY - WALL_Y_PAD, bounds.minZ - WALL_INNER_INSET, bounds.maxX + WALL_SIDE_PAD, bounds.maxY + WALL_Y_PAD, bounds.maxZ + WALL_TRIGGER_DEPTH);
                    case EAST -> new AABB(bounds.minX - WALL_TRIGGER_DEPTH, bounds.minY - WALL_Y_PAD, bounds.minZ - WALL_SIDE_PAD, bounds.maxX + WALL_INNER_INSET, bounds.maxY + WALL_Y_PAD, bounds.maxZ + WALL_SIDE_PAD);
                    case WEST -> new AABB(bounds.minX - WALL_INNER_INSET, bounds.minY - WALL_Y_PAD, bounds.minZ - WALL_SIDE_PAD, bounds.maxX + WALL_TRIGGER_DEPTH, bounds.maxY + WALL_Y_PAD, bounds.maxZ + WALL_SIDE_PAD);
                    default -> bounds.inflate(TRIGGER_INFLATE_DEFAULT);
                };
            }
            default -> bounds.inflate(TRIGGER_INFLATE_DEFAULT);
        };
    }

    private static void releaseEntitiesForTape(Level level, BlockPos pos) {
        String key = tapeKey(level, pos);
        AABB searchBox = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos)).inflate(2.0D);
        STUCK_ENTITIES.entrySet().removeIf(entry -> {
            StuckContact contact = entry.getValue();
            if (!key.equals(contact.tapeKey())) {
                return false;
            }

            level.getEntitiesOfClass(Entity.class, searchBox, candidate -> candidate.getUUID().equals(entry.getKey()))
                    .forEach(candidate -> candidate.setNoGravity(false));

            STICK_SOUND_TIMES.remove(entry.getKey());
            STICK_CONTACT_KEYS.remove(entry.getKey());
            return true;
        });
    }

    private static void releaseEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        STUCK_ENTITIES.remove(entity.getUUID());
        STICK_SOUND_TIMES.remove(entity.getUUID());
        STICK_CONTACT_KEYS.remove(entity.getUUID());
        entity.setNoGravity(false);
    }

    private static String tapeDecayKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.asLong();
    }

    private static String tapeKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.asLong();
    }

    private static boolean shouldPlayStickSound(UUID entityId, String tapeKey, long gameTime) {
        String lastKey = STICK_CONTACT_KEYS.get(entityId);
        Long lastContact = STICK_SOUND_TIMES.get(entityId);
        if (lastKey == null || !tapeKey.equals(lastKey)) {
            STICK_CONTACT_KEYS.put(entityId, tapeKey);
            STICK_SOUND_TIMES.put(entityId, gameTime);
            return true;
        }

        if (lastContact == null || gameTime - lastContact > STICK_SOUND_COOLDOWN_TICKS) {
            STICK_SOUND_TIMES.put(entityId, gameTime);
            return true;
        }

        return false;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record StuckContact(
            String tapeKey,
            String dimensionId,
            long blockPos,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
    }

    private record TapeHit(BlockPos pos, BlockState state) {
    }
}
