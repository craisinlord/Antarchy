package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.effect.SaucedMeals;
import com.craisinlord.antarchy.mixins.LivingEntityJumpingAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JelliedCranberryBlock extends Block {
    public static final int MAX_SERVINGS = 4;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 1, MAX_SERVINGS);

    private static final VoxelShape[] OUTLINE_SHAPES = new VoxelShape[MAX_SERVINGS + 1];
    private static final VoxelShape[] COLLISION_SHAPES = new VoxelShape[MAX_SERVINGS + 1];

    private static final int SLICE_NUTRITION = 3;
    private static final float SLICE_SATURATION_MODIFIER = 0.5F;
    private static final double EDGE_EPSILON = 1.0E-7;
    private static final double WALL_INSET = 0.4375;
    private static final double INPUT_THRESHOLD = 0.05;
    private static final double CLING_HORIZONTAL_DRAG = 0.6;
    private static final double SLIDE_SPEED = -0.05;
    private static final double SLIDE_TRIGGER_SPEED = -0.08;
    private static final double SLOW_FALL_GRAVITY = 0.01;
    private static final double WALL_JUMP_HORIZONTAL = 0.5;
    private static final double WALL_JUMP_VERTICAL = 0.6;
    private static final int WALL_JUMP_COOLDOWN_TICKS = 8;

    private static int lastWallJumpTick = Integer.MIN_VALUE;

    static {
        for (int servings = 1; servings <= MAX_SERVINGS; servings++) {
            int height = servings * 16 / MAX_SERVINGS;
            OUTLINE_SHAPES[servings] = Block.box(0.0, 0.0, 0.0, 16.0, height, 16.0);
            COLLISION_SHAPES[servings] = Block.box(1.0, 0.0, 1.0, 15.0, height - 1.0, 15.0);
        }
    }

    public JelliedCranberryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SERVINGS, MAX_SERVINGS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SERVINGS);
    }

    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    public boolean canStickTo(BlockState state, BlockState other) {
        return !other.is(Blocks.SLIME_BLOCK)
                && !other.is(Blocks.HONEY_BLOCK)
                && !(other.getBlock() instanceof FlytrapGooBlock);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPES[state.getValue(SERVINGS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[state.getValue(SERVINGS)];
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
        entity.causeFallDamage(fallDistance, 0.2F, level.damageSources().fall());
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!isTouchingSide(state, pos, entity)) {
            return;
        }
        if (entity instanceof Player player) {
            if (canCling(player)) {
                entity.resetFallDistance();
                if (player.isLocalPlayer()) {
                    tickLocalPlayerCling(pos, player);
                }
            }
            return;
        }
        Vec3 delta = entity.getDeltaMovement();
        if (delta.y < SLIDE_TRIGGER_SPEED) {
            double scale = delta.y < -0.13 ? SLIDE_SPEED / delta.y : 1.0;
            entity.setDeltaMovement(delta.x * scale, SLIDE_SPEED, delta.z * scale);
            entity.resetFallDistance();
        }
    }

    private static boolean isTouchingSide(BlockState state, BlockPos pos, Entity entity) {
        if (entity.onGround()) {
            return false;
        }
        double top = pos.getY() + state.getValue(SERVINGS) * 1.0 / MAX_SERVINGS - 0.0625;
        if (entity.getY() > top - EDGE_EPSILON) {
            return false;
        }
        double reach = WALL_INSET + entity.getBbWidth() / 2.0;
        return Math.abs(pos.getX() + 0.5 - entity.getX()) + EDGE_EPSILON > reach
                || Math.abs(pos.getZ() + 0.5 - entity.getZ()) + EDGE_EPSILON > reach;
    }

    private static boolean canCling(Player player) {
        return !player.getAbilities().flying && !player.isFallFlying() && !player.isPassenger() && !player.isSpectator();
    }

    private static void tickLocalPlayerCling(BlockPos pos, Player player) {
        double reach = WALL_INSET + player.getBbWidth() / 2.0;
        double offsetX = player.getX() - (pos.getX() + 0.5);
        double offsetZ = player.getZ() - (pos.getZ() + 0.5);

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        double inputX = player.xxa * cos - player.zza * sin;
        double inputZ = player.zza * cos + player.xxa * sin;

        boolean pressingX = Math.abs(offsetX) + EDGE_EPSILON > reach && inputX * offsetX < 0.0 && Math.abs(inputX) > INPUT_THRESHOLD;
        boolean pressingZ = Math.abs(offsetZ) + EDGE_EPSILON > reach && inputZ * offsetZ < 0.0 && Math.abs(inputZ) > INPUT_THRESHOLD;
        if (!pressingX && !pressingZ) {
            return;
        }

        if (((LivingEntityJumpingAccessor) player).antarchy$isJumping() && wallJumpReady(player)) {
            lastWallJumpTick = player.tickCount;
            double awayX = pressingX ? Math.signum(offsetX) : 0.0;
            double awayZ = pressingZ ? Math.signum(offsetZ) : 0.0;
            player.setDeltaMovement(awayX * WALL_JUMP_HORIZONTAL, WALL_JUMP_VERTICAL, awayZ * WALL_JUMP_HORIZONTAL);
            player.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.3F);
            return;
        }

        double gravity = player.getAttributeValue(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
        if (player.hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = Math.min(gravity, SLOW_FALL_GRAVITY);
        }
        Vec3 delta = player.getDeltaMovement();
        player.setDeltaMovement(delta.x * CLING_HORIZONTAL_DRAG, gravity, delta.z * CLING_HORIZONTAL_DRAG);
    }

    private static boolean wallJumpReady(Player player) {
        return player.tickCount < lastWallJumpTick || player.tickCount - (long) lastWallJumpTick >= WALL_JUMP_COOLDOWN_TICKS;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide) {
                ItemStack sauce = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("antarchy", "cranberry_sauce")));
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, sauce));
                player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                removeServing(state, level, pos, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!stack.isEmpty() || !player.canEat(true)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.getFoodData().eat(SLICE_NUTRITION, SLICE_SATURATION_MODIFIER);
            SaucedMeals.applySauced(player);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.EAT, pos);
            removeServing(state, level, pos, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    private static void removeServing(BlockState state, Level level, BlockPos pos, Player player) {
        int servings = state.getValue(SERVINGS);
        if (servings > 1) {
            level.setBlock(pos, state.setValue(SERVINGS, servings - 1), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        } else {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
    }
}
