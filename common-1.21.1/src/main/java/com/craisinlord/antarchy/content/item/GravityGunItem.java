package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GravityGunItem extends Item implements GeoItem {
    public static final String GRAVITY_GUN_CARRIED_BLOCK_TAG = "antarchy.gravity_gun_carried_block";
    private static final String LOOP_CONTROLLER = "loop_controller";
    private static final String TRANSITION_CONTROLLER = "transition_controller";
    private static final int TRANSITION_TICKS = 8;
    private static final String OFF_ANIMATION = "off";
    private static final String ON_ANIMATION = "on";
    private static final String TOGGLE_ON_ANIMATION = "toggle_on";
    private static final String TOGGLE_OFF_ANIMATION = "toggle_off";
    private static final String HELD_TARGET_UUID_TAG = "antarchy.gravity_gun_held_target_uuid";
    private static final String HELD_DISTANCE_TAG = "antarchy.gravity_gun_held_distance";
    private static final double DEFAULT_HOLD_DISTANCE = 3.0D;
    private static final double MIN_HOLD_DISTANCE = 1.5D;
    private static final int HOLD_LOOP_INTERVAL_TICKS = 26;
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/gravity_gun.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/models/item/gravity_gun.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/gravity_gun.animation.json");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Long2BooleanMap animationActiveStates = new Long2BooleanOpenHashMap();
    private final Long2BooleanMap animationTransitionTargets = new Long2BooleanOpenHashMap();
    private final Long2IntMap animationTransitionTicks = new Long2IntOpenHashMap();
    private final Long2IntMap animationObservedLoopStates = new Long2IntOpenHashMap();
    private int holdLoopCooldown = 0;

    public GravityGunItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
        this.animationObservedLoopStates.defaultReturnValue(Integer.MIN_VALUE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!AntarchySettings.gravityGunEnabled()) {
            return InteractionResultHolder.fail(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        if (hasHeldTarget(stack)) {
            if (releaseHeldTarget(serverLevel, player, stack, false)) {
                finishAction(serverLevel, player, stack, usedHand);
                return InteractionResultHolder.consume(stack);
            }
        }

        if (!tryCaptureEntity(serverLevel, player, stack)) {
            return InteractionResultHolder.pass(stack);
        }

        finishAction(serverLevel, player, stack, usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!AntarchySettings.gravityGunEnabled()) {
            return InteractionResult.FAIL;
        }

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            if (!tryCaptureBlock((ServerLevel) level, player, stack, context.getClickedPos())) {
                return InteractionResult.PASS;
            }

            finishAction((ServerLevel) level, player, stack, context.getHand());
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }

        long animatableId = level instanceof ServerLevel serverLevelForId
                ? GeoItem.getOrAssignId(stack, serverLevelForId)
                : GeoItem.getId(stack);
        this.tickAnimationState(level, player, stack, animatableId);

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        this.clearLegacyAnimationData(stack);

        UUID heldTargetId = getHeldTargetId(stack);
        if (heldTargetId == null) {
            return;
        }

        if (!isStackInPlayerHands(player, stack)) {
            releaseHeldTarget(serverLevel, player, stack, false);
            return;
        }

        Entity heldTarget = serverLevel.getEntity(heldTargetId);
        if (heldTarget == null || !heldTarget.isAlive()) {
            clearHeldTarget(stack);
            this.holdLoopCooldown = 0;
            return;
        }

        maintainHeldTarget(serverLevel, player, stack, heldTarget);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<net.minecraft.network.chat.Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.antarchy.gravity_gun.primary").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.antarchy.gravity_gun.secondary").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.antarchy.gravity_gun.scroll").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.antarchy.gravity_gun.blacklist").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, LOOP_CONTROLLER, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            long animatableId = stack == null ? Long.MAX_VALUE : GeoItem.getId(stack);
            if (animatableId != Long.MAX_VALUE && this.isAnimationTransitioning(animatableId)) {
                this.observeLoopControllerState(animatableId, "transition_stop", stack);
                return PlayState.STOP;
            }

            boolean active = animatableId != Long.MAX_VALUE && this.isAnimationActive(animatableId);
            this.observeLoopControllerState(animatableId, active ? ON_ANIMATION : OFF_ANIMATION, stack);
            return state.setAndContinue(RawAnimation.begin().thenLoop(active ? ON_ANIMATION : OFF_ANIMATION));
        }));
        controllers.add(new AnimationController<>(this, TRANSITION_CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim(TOGGLE_ON_ANIMATION, RawAnimation.begin().thenPlay(TOGGLE_ON_ANIMATION))
                .triggerableAnim(TOGGLE_OFF_ANIMATION, RawAnimation.begin().thenPlay(TOGGLE_OFF_ANIMATION)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<GravityGunItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }

    private boolean tryCaptureEntity(ServerLevel level, Player player, ItemStack stack) {
        if (!AntarchySettings.gravityGunEntitiesEnabled() || hasHeldTarget(stack)) {
            return false;
        }

        EntityHitResult hitResult = findEntityTarget(level, player);
        if (hitResult == null) {
            return false;
        }

        Entity target = hitResult.getEntity();
        if (!canCaptureEntity(target)) {
            return false;
        }

        setHeldTarget(stack, target.getUUID());
        setHeldDistance(stack, DEFAULT_HOLD_DISTANCE);
        target.setDeltaMovement(Vec3.ZERO);
        target.setNoGravity(true);
        target.hurtMarked = true;
        this.beginAnimationTransition(level, player, stack, GeoItem.getOrAssignId(stack, level), true);
        playGravityGunSound(level, player, AntarchySoundEvents.GRAVITY_GUN_PICKUP.get(), 0.20F, 1.0F);
        return true;
    }

    private boolean tryCaptureBlock(ServerLevel level, Player player, ItemStack stack, BlockPos clickedPos) {
        if (!AntarchySettings.gravityGunBlocksEnabled() || hasHeldTarget(stack)) {
            return false;
        }

        if (!level.isLoaded(clickedPos)) {
            return false;
        }

        CarriedBlockCapture capture = this.resolveCarriedBlockCapture(level, clickedPos);
        if (capture == null || !canCaptureBlock(level, capture.basePos(), capture.baseState())) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(capture.basePos());
        CompoundTag blockEntityData = null;
        if (blockEntity != null) {
            blockEntityData = blockEntity.saveWithId(level.registryAccess());
            level.removeBlockEntity(capture.basePos());
        }

        if (capture.upperPos() != null) {
            level.removeBlock(capture.upperPos(), false);
        }

        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, capture.basePos(), capture.baseState());
        if (fallingBlock == null) {
            return false;
        }

        if (blockEntityData != null) {
            fallingBlock.blockData = blockEntityData;
        }
        fallingBlock.addTag(GRAVITY_GUN_CARRIED_BLOCK_TAG);

        setHeldTarget(stack, fallingBlock.getUUID());
        setHeldDistance(stack, DEFAULT_HOLD_DISTANCE);
        fallingBlock.setDeltaMovement(Vec3.ZERO);
        fallingBlock.setNoGravity(true);
        fallingBlock.setHurtsEntities(1.0F, 40);
        fallingBlock.hurtMarked = true;
        this.beginAnimationTransition(level, player, stack, GeoItem.getOrAssignId(stack, level), true);
        playGravityGunSound(level, player, AntarchySoundEvents.GRAVITY_GUN_PICKUP.get(), 0.20F, 1.0F);
        return true;
    }

    private boolean throwHeldTarget(ServerLevel level, Player player, ItemStack stack) {
        UUID heldTargetId = getHeldTargetId(stack);
        if (heldTargetId == null) {
            return false;
        }

        Entity heldTarget = level.getEntity(heldTargetId);
        if (heldTarget == null || !heldTarget.isAlive()) {
            clearHeldTarget(stack);
            return false;
        }

        Vec3 launch = getLaunchVector(player).scale(AntarchySettings.gravityGunThrowStrength());
        heldTarget.setNoGravity(false);
        heldTarget.setDeltaMovement(launch);
        heldTarget.hurtMarked = true;
        clearHeldTarget(stack);
        this.beginAnimationTransition(level, player, stack, GeoItem.getOrAssignId(stack, level), false);
        this.holdLoopCooldown = 0;
        playGravityGunSound(level, player, AntarchySoundEvents.GRAVITY_GUN_LAUNCH.get(), 0.14F, 0.9F);
        return true;
    }

    public boolean firePrimary(ServerLevel level, Player player, ItemStack stack) {
        if (!AntarchySettings.gravityGunEnabled() || player.getCooldowns().isOnCooldown(this) || player.getMainHandItem() != stack) {
            return false;
        }

        if (!throwHeldTarget(level, player, stack)) {
            blastEntities(level, player, stack);
            playGravityGunSound(level, player, AntarchySoundEvents.GRAVITY_GUN_DRYFIRE.get(), 0.16F, 1.0F);
        }

        finishAction(level, player, stack, InteractionHand.MAIN_HAND);
        return true;
    }

    private void blastEntities(ServerLevel level, Player player, ItemStack stack) {
        Vec3 look = getLaunchVector(player);
        double range = AntarchySettings.gravityGunRange();
        AABB searchBox = player.getBoundingBox().inflate(range).expandTowards(look.scale(range));
        for (Entity target : level.getEntities(player, searchBox, entity -> canBlastEntity(player, stack, entity))) {
            Vec3 away = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() < 1.0E-4D) {
                away = look;
            }

            double alignment = away.normalize().dot(look);
            if (alignment < 0.15D) {
                continue;
            }

            double distance = Math.max(0.75D, target.distanceTo(player));
            Vec3 push = away.normalize().scale(AntarchySettings.gravityGunBlastStrength() / distance)
                    .add(0.0D, 0.22D, 0.0D);
            target.setDeltaMovement(target.getDeltaMovement().add(push));
            target.hurtMarked = true;
        }
    }

    private void maintainHeldTarget(ServerLevel level, Player player, ItemStack stack, Entity heldTarget) {
        Vec3 currentPosition = heldTarget.position();
        Vec3 holdPosition = player.getEyePosition().add(getHoldVector(player).scale(getHeldDistance(stack)));
        Vec3 movement = holdPosition.subtract(currentPosition);
        Vec2 holdRotation = getHoldRotation(player);
        heldTarget.setNoGravity(true);

        // Snap back only when the held entity has drifted badly; otherwise drive it with
        // velocity so the client can interpolate the motion smoothly between server updates.
        if (movement.lengthSqr() > 16.0D) {
            heldTarget.moveTo(holdPosition.x, holdPosition.y, holdPosition.z, holdRotation.x, holdRotation.y);
            heldTarget.setDeltaMovement(Vec3.ZERO);
        } else {
            Vec3 holdVelocity = movement.scale(0.45D);
            double maxSpeed = 1.35D;
            if (holdVelocity.lengthSqr() > maxSpeed * maxSpeed) {
                holdVelocity = holdVelocity.normalize().scale(maxSpeed);
            }
            heldTarget.setDeltaMovement(holdVelocity);
        }
        heldTarget.setYRot(holdRotation.x);
        heldTarget.setXRot(holdRotation.y);
        heldTarget.hurtMarked = true;
        tickHoldLoopSound(level, player);
    }

    private boolean releaseHeldTarget(ServerLevel level, Player player, ItemStack stack, boolean throwIt) {
        UUID heldTargetId = getHeldTargetId(stack);
        if (heldTargetId == null) {
            return false;
        }

        Entity heldTarget = level.getEntity(heldTargetId);
        if (heldTarget != null) {
            heldTarget.setNoGravity(false);
            if (throwIt) {
                heldTarget.setDeltaMovement(getLaunchVector(player).scale(AntarchySettings.gravityGunThrowStrength()));
                heldTarget.hurtMarked = true;
            }
        }

        clearHeldTarget(stack);
        this.beginAnimationTransition(level, player, stack, GeoItem.getOrAssignId(stack, level), false);
        this.holdLoopCooldown = 0;
        if (!throwIt) {
            playGravityGunSound(level, player, AntarchySoundEvents.GRAVITY_GUN_DROP.get(), 0.18F, 1.0F);
        }
        return true;
    }

    public static boolean adjustHeldDistance(ItemStack stack, double delta) {
        if (!hasHeldTarget(stack)) {
            return false;
        }

        double currentDistance = getHeldDistance(stack);
        double maxDistance = AntarchySettings.gravityGunMaxHoldDistance();
        double newDistance = Mth.clamp(currentDistance + delta, MIN_HOLD_DISTANCE, maxDistance);
        if (Math.abs(newDistance - currentDistance) < 1.0E-4D) {
            return false;
        }

        setHeldDistance(stack, newDistance);
        return true;
    }

    private boolean canCaptureEntity(Entity entity) {
        if (entity instanceof Player) {
            return false;
        }

        return !entity.getType().is(AntarchyTags.Entities.GRAVITY_GUN_BLACKLIST);
    }

    private boolean canCaptureBlock(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(AntarchyTags.Blocks.GRAVITY_GUN_BLACKLIST)) {
            return false;
        }

        if (state.getBlock() instanceof DoublePlantBlock) {
            if (!state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)
                    || state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER) {
                return false;
            }

            BlockPos upperPos = pos.above();
            BlockState upperState = level.getBlockState(upperPos);
            if (!upperState.is(state.getBlock())
                    || !upperState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)
                    || upperState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) {
                return false;
            }
        }

        return state.getDestroySpeed(level, pos) >= 0.0F;
    }

    private CarriedBlockCapture resolveCarriedBlockCapture(Level level, BlockPos clickedPos) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!(clickedState.getBlock() instanceof DoublePlantBlock)
                || !clickedState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return new CarriedBlockCapture(clickedPos, clickedState, null);
        }

        DoubleBlockHalf half = clickedState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF);
        BlockPos basePos = half == DoubleBlockHalf.UPPER ? clickedPos.below() : clickedPos;
        BlockPos upperPos = basePos.above();
        BlockState baseState = level.getBlockState(basePos);
        BlockState upperState = level.getBlockState(upperPos);
        if (!(baseState.getBlock() instanceof DoublePlantBlock)
                || !baseState.is(clickedState.getBlock())
                || !baseState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)
                || baseState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER
                || !upperState.is(clickedState.getBlock())
                || !upperState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)
                || upperState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) {
            return null;
        }

        return new CarriedBlockCapture(basePos, baseState, upperPos);
    }

    private boolean canBlastEntity(Player player, ItemStack stack, Entity entity) {
        return entity.isAlive()
                && entity != player
                && entity != player.getVehicle()
                && !entity.getUUID().equals(getHeldTargetId(stack))
                && !entity.getType().is(AntarchyTags.Entities.GRAVITY_GUN_BLACKLIST);
    }

    private EntityHitResult findEntityTarget(ServerLevel level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        double reach = AntarchySettings.gravityGunRange();
        Vec3 end = eyePosition.add(viewVector.scale(reach));
        AABB searchBox = player.getBoundingBox().expandTowards(viewVector.scale(reach)).inflate(1.0D);
        return ProjectileUtil.getEntityHitResult(
                level,
                player,
                eyePosition,
                end,
                searchBox,
                entity -> entity.isPickable() && canCaptureEntity(entity)
        );
    }

    private static boolean isStackInPlayerHands(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }

    private static Vec3 getLaunchVector(Player player) {
        Vec3 look = player.getViewVector(1.0F);
        if (look.lengthSqr() < 1.0E-4D) {
            look = getRawLookVector(player);
        }
        return look.lengthSqr() < 1.0E-4D ? getRawLookVector(player) : look.normalize();
    }

    private static Vec3 getHoldVector(Player player) {
        Vec3 look = getRawLookVector(player);
        if (!AntarchyGravityApi.isGravityInverted(player)) {
            return look;
        }

        return AntarchyGravityRotationUtil.vecWorldToPlayer(look, AntarchyGravityApi.getGravityDirection(player));
    }

    private static Vec3 getRawLookVector(Player player) {
        Vec3 look = player.getLookAngle();
        return look.lengthSqr() < 1.0E-4D ? Vec3.directionFromRotation(player.getXRot(), player.getYRot()) : look.normalize();
    }

    private static Vec2 getHoldRotation(Player player) {
        if (!AntarchyGravityApi.isGravityInverted(player)) {
            return new Vec2(player.getYRot(), player.getXRot());
        }

        return AntarchyGravityRotationUtil.rotWorldToPlayer(
                player.getYRot(),
                player.getXRot(),
                AntarchyGravityApi.getGravityDirection(player)
        );
    }

    private static UUID getHeldTargetId(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        return tag.hasUUID(HELD_TARGET_UUID_TAG) ? tag.getUUID(HELD_TARGET_UUID_TAG) : null;
    }

    public static boolean hasHeldTarget(ItemStack stack) {
        return getHeldTargetId(stack) != null;
    }

    public static double getHeldDistance(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        double maxDistance = Math.max(MIN_HOLD_DISTANCE, AntarchySettings.gravityGunMaxHoldDistance());
        if (!tag.contains(HELD_DISTANCE_TAG)) {
            return Mth.clamp(DEFAULT_HOLD_DISTANCE, MIN_HOLD_DISTANCE, maxDistance);
        }

        return Mth.clamp(tag.getDouble(HELD_DISTANCE_TAG), MIN_HOLD_DISTANCE, maxDistance);
    }

    private static void setHeldTarget(ItemStack stack, UUID targetId) {
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(HELD_TARGET_UUID_TAG, targetId));
    }

    private static void setHeldDistance(ItemStack stack, double distance) {
        double maxDistance = Math.max(MIN_HOLD_DISTANCE, AntarchySettings.gravityGunMaxHoldDistance());
        double clampedDistance = Mth.clamp(distance, MIN_HOLD_DISTANCE, maxDistance);
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble(HELD_DISTANCE_TAG, clampedDistance));
    }

    private static void clearHeldTarget(ItemStack stack) {
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(HELD_TARGET_UUID_TAG);
            tag.remove(HELD_DISTANCE_TAG);
        });
    }

    private boolean isAnimationActive(long animatableId) {
        return this.animationActiveStates.get(animatableId);
    }

    private void setAnimationActive(long animatableId, boolean active) {
        if (active) {
            this.animationActiveStates.put(animatableId, true);
        } else {
            this.animationActiveStates.remove(animatableId);
        }
    }

    private boolean isAnimationTransitioning(long animatableId) {
        return this.getAnimationTransitionTicks(animatableId) > 0;
    }

    private boolean getAnimationTransitionTarget(long animatableId) {
        return this.animationTransitionTargets.get(animatableId);
    }

    private void setAnimationTransitionTarget(long animatableId, boolean targetActive) {
        this.animationTransitionTargets.put(animatableId, targetActive);
    }

    private void clearAnimationTransitionTarget(long animatableId) {
        this.animationTransitionTargets.remove(animatableId);
    }

    private int getAnimationTransitionTicks(long animatableId) {
        return Math.max(0, this.animationTransitionTicks.get(animatableId));
    }

    private void setAnimationTransitionTicks(long animatableId, int ticks) {
        if (ticks > 0) {
            this.animationTransitionTicks.put(animatableId, ticks);
        } else {
            this.animationTransitionTicks.remove(animatableId);
        }
    }

    private void finishAction(ServerLevel level, Player player, ItemStack stack, InteractionHand hand) {
        stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.awardStat(Stats.ITEM_USED.get(this));
        double cooldownSeconds = AntarchySettings.gravityGunCooldownSeconds();
        if (cooldownSeconds > 0.0D) {
            player.getCooldowns().addCooldown(this, (int) (cooldownSeconds * 20.0D));
        }
    }

    private void tickAnimationState(Level level, Player player, ItemStack stack, long animatableId) {
        if (animatableId == Long.MAX_VALUE) {
            return;
        }

        int transitionTicks = this.getAnimationTransitionTicks(animatableId);
        if (transitionTicks > 0) {
            transitionTicks--;
            this.setAnimationTransitionTicks(animatableId, transitionTicks);
            if (transitionTicks == 0) {
                this.setAnimationActive(animatableId, this.getAnimationTransitionTarget(animatableId));
                this.clearAnimationTransitionTarget(animatableId);
            }
            return;
        }

        boolean hasHeldTarget = hasHeldTarget(stack);
        boolean active = this.isAnimationActive(animatableId);
        if (hasHeldTarget && !active) {
            this.beginAnimationTransition(level, player, stack, animatableId, true);
        } else if (!hasHeldTarget && active) {
            this.beginAnimationTransition(level, player, stack, animatableId, false);
        } else if (!hasHeldTarget) {
            this.clearAnimationRuntimeState(animatableId);
        }
    }

    private void beginAnimationTransition(Level level, Player player, ItemStack stack, long animatableId, boolean targetActive) {
        if (animatableId == Long.MAX_VALUE || this.getAnimationTransitionTicks(animatableId) > 0 || this.isAnimationActive(animatableId) == targetActive) {
            return;
        }

        this.setAnimationTransitionTarget(animatableId, targetActive);
        this.setAnimationTransitionTicks(animatableId, TRANSITION_TICKS);
        if (level instanceof ServerLevel) {
            triggerAnim(player, animatableId, TRANSITION_CONTROLLER, targetActive ? TOGGLE_ON_ANIMATION : TOGGLE_OFF_ANIMATION);
        }
    }

    private void clearAnimationRuntimeState(long animatableId) {
        this.animationActiveStates.remove(animatableId);
        this.animationTransitionTargets.remove(animatableId);
        this.animationTransitionTicks.remove(animatableId);
        this.animationObservedLoopStates.remove(animatableId);
    }

    private void clearLegacyAnimationData(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (!tag.contains("antarchy.gravity_gun_anim_active")
                && !tag.contains("antarchy.gravity_gun_anim_target_active")
                && !tag.contains("antarchy.gravity_gun_anim_transition_ticks")) {
            return;
        }

        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, customData -> {
            customData.remove("antarchy.gravity_gun_anim_active");
            customData.remove("antarchy.gravity_gun_anim_target_active");
            customData.remove("antarchy.gravity_gun_anim_transition_ticks");
        });
    }

    private void tickHoldLoopSound(ServerLevel level, Player player) {
        if (this.holdLoopCooldown > 0) {
            this.holdLoopCooldown--;
            return;
        }

        playGravityGunSound(level, player, AntarchySoundEvents.GRAVITY_GUN_HOLD_LOOP.get(), 0.06F, 0.95F + level.getRandom().nextFloat() * 0.1F);
        this.holdLoopCooldown = HOLD_LOOP_INTERVAL_TICKS;
    }

    private static void playGravityGunSound(Level level, Player player, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private void observeLoopControllerState(long animatableId, String stateName, ItemStack stack) {
        if (animatableId == Long.MAX_VALUE) {
            return;
        }

        int encodedState = this.encodeObservedLoopState(stateName);
        if (this.animationObservedLoopStates.get(animatableId) == encodedState) {
            return;
        }

        this.animationObservedLoopStates.put(animatableId, encodedState);
    }

    private int encodeObservedLoopState(String stateName) {
        return switch (stateName) {
            case OFF_ANIMATION -> 0;
            case ON_ANIMATION -> 1;
            case "transition_stop" -> 2;
            default -> 3;
        };
    }

    private record CarriedBlockCapture(BlockPos basePos, BlockState baseState, BlockPos upperPos) {
    }
}
