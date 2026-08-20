package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunBlackHoleEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PortalGunItem extends Item implements GeoItem {
    private static final String LED_CONTROLLER = "led_controller";
    private static final String FIRE_CONTROLLER = "fire_controller";
    private static final String LAST_SIDE_TAG = "antarchy.portal_gun_last_side";
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/portal_gun.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/portal_gun.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/portal_gun.animation.json");
    private final Supplier<? extends EntityType<? extends PortalGunPortalEntity>> portalType;
    private final Supplier<? extends EntityType<? extends PortalGunBlackHoleEntity>> blackHoleType;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PortalGunItem(Item.Properties properties, Supplier<? extends EntityType<? extends PortalGunPortalEntity>> portalType, Supplier<? extends EntityType<? extends PortalGunBlackHoleEntity>> blackHoleType) {
        super(properties);
        this.portalType = portalType;
        this.blackHoleType = blackHoleType;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (player.isShiftKeyDown()) {
            PortalGunSavedData.clearAllPortals(serverLevel.getServer(), player.getUUID());
            this.setLastSide(stack, null);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound("portal_fizzle", SoundEvents.ENDER_CHEST_CLOSE), SoundSource.PLAYERS, 0.6F, 1.0F);
            return InteractionResultHolder.consume(stack);
        }
        boolean fired = this.firePortal(serverLevel, player, stack, PortalGunPortalEntity.PortalSide.ORANGE);
        return fired ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    public void firePrimary(ServerLevel level, ServerPlayer player, ItemStack stack) {
        this.firePortal(level, player, stack, PortalGunPortalEntity.PortalSide.BLUE);
    }

    private boolean firePortal(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        BlockHitResult hitResult = this.raycast(level, player);
        if (hitResult.getType() == HitResult.Type.MISS) {
            if (this.tryShootMoon(level, player, stack, side)) {
                return true;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound("portal_gun_invalid_surface", SoundEvents.DISPENSER_FAIL), SoundSource.PLAYERS, 0.55F, 1.0F);
            return false;
        }
        this.spawnShotTrail(level, player, hitResult.getLocation(), side);
        Placement placement = this.findPlacement(level, player, hitResult);
        if (placement == null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound("portal_gun_invalid_surface", SoundEvents.DISPENSER_FAIL), SoundSource.PLAYERS, 0.55F, 1.0F);
            return false;
        }

        PortalGunPortalEntity portal = new PortalGunPortalEntity(this.portalType.get(), level);
        portal.configure(player.getUUID(), side, placement.facing(), placement.upAxis(), placement.supportOrigin());
        portal.moveTo(placement.center().x, placement.center().y, placement.center().z, placement.yaw(), 0.0F);
        level.addFreshEntity(portal);
        PortalGunSavedData.setPortal(level.getServer(), player.getUUID(), side, portal.getUUID());
        PortalGunPortalEntity other = this.findCounterpart(level, player.getUUID(), side);
        if (other != null) {
            portal.linkTo(other);
            other.linkTo(portal);
        }
        this.setLastSide(stack, side);
        this.triggerFireAnimation(level, player, stack);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 6);
        String firePath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_gun_fire_blue" : "portal_gun_fire_orange";
        String openPath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_open_blue" : "portal_open_orange";
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound(firePath, SoundEvents.BEACON_ACTIVATE), SoundSource.PLAYERS, 0.65F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.15F : 0.88F);
        level.playSound(null, portal.getX(), portal.getY(), portal.getZ(), PortalGunPortalEntity.sound(openPath, SoundEvents.END_PORTAL_SPAWN), SoundSource.PLAYERS, 0.45F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.05F : 0.92F);
        return true;
    }

    private boolean tryShootMoon(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        if (!level.dimensionType().hasSkyLight() || !level.isNight() || player.getXRot() > -40.0F) {
            return false;
        }
        PortalGunPortalEntity counterpart = this.findCounterpart(level, player.getUUID(), side);
        Vec3 spawnPos;
        if (counterpart != null) {
            spawnPos = counterpart.position();
            counterpart.discard();
        } else {
            spawnPos = player.getEyePosition().add(player.getLookAngle().scale(20.0D));
        }
        PortalGunBlackHoleEntity blackHole = new PortalGunBlackHoleEntity(this.blackHoleType.get(), level);
        blackHole.configure(player.getUUID());
        blackHole.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 0.0F, 0.0F);
        level.addFreshEntity(blackHole);
        this.setLastSide(stack, side);
        this.triggerFireAnimation(level, player, stack);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 6);
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, PortalGunPortalEntity.sound("portal_gun_black_hole", SoundEvents.WARDEN_SONIC_BOOM), SoundSource.PLAYERS, 0.6F, 0.6F);
        this.spawnShotTrail(level, player, spawnPos, side);
        return true;
    }

    private void spawnShotTrail(ServerLevel level, Player player, Vec3 endPos, PortalGunPortalEntity.PortalSide side) {
        Vec3 start = player.getEyePosition();
        Vec3 delta = endPos.subtract(start);
        double length = delta.length();
        int steps = (int) Math.max(4.0D, Math.min(48.0D, length * 2.0D));
        net.minecraft.core.particles.ParticleOptions particle = side == PortalGunPortalEntity.PortalSide.BLUE ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 point = start.add(delta.scale(t));
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private PortalGunPortalEntity findCounterpart(ServerLevel level, UUID owner, PortalGunPortalEntity.PortalSide side) {
        PortalGunPortalEntity.PortalSide otherSide = side == PortalGunPortalEntity.PortalSide.BLUE ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        Optional<UUID> otherId = PortalGunSavedData.getPortalId(level.getServer(), owner, otherSide);
        if (otherId.isEmpty()) {
            return null;
        }
        Entity entity = level.getEntity(otherId.get());
        return entity instanceof PortalGunPortalEntity portal ? portal : null;
    }

    private BlockHitResult raycast(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(64.0D));
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    private Placement findPlacement(Level level, Player player, BlockHitResult hitResult) {
        Direction facing = hitResult.getDirection();
        Direction heightAxis;
        float yaw;
        if (facing.getAxis() == Direction.Axis.Y) {
            heightAxis = Direction.fromYRot(player.getYRot());
            yaw = heightAxis.toYRot();
        } else {
            heightAxis = Direction.UP;
            yaw = facing.toYRot();
        }
        BlockPos hitPos = hitResult.getBlockPos();
        for (int offset = -1; offset <= 0; offset++) {
            BlockPos supportOrigin = hitPos.relative(heightAxis, offset);
            if (this.canPlaceAt(level, supportOrigin, facing, heightAxis)) {
                Vec3 firstCellCenter = supportOrigin.relative(facing).getCenter();
                Vec3 halfStep = Vec3.atLowerCornerOf(heightAxis.getNormal()).scale(0.5D);
                Vec3 center = firstCellCenter.add(halfStep);
                return new Placement(center, facing, heightAxis, yaw, supportOrigin);
            }
        }
        return null;
    }

    private boolean canPlaceAt(Level level, BlockPos supportOrigin, Direction facing, Direction heightAxis) {
        for (int h = 0; h < 2; h++) {
            BlockPos supportPos = supportOrigin.relative(heightAxis, h);
            BlockPos airPos = supportPos.relative(facing);
            BlockState supportState = level.getBlockState(supportPos);
            BlockState airState = level.getBlockState(airPos);
            FluidState fluidState = level.getFluidState(airPos);
            if (!supportState.isFaceSturdy(level, supportPos, facing)) {
                return false;
            }
            if (!airState.isAir() && !airState.canBeReplaced()) {
                return false;
            }
            if (!fluidState.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void setLastSide(ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        if (side == null) {
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(LAST_SIDE_TAG));
            return;
        }
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(LAST_SIDE_TAG, side.name()));
    }

    private PortalGunPortalEntity.PortalSide getLastSide(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        String sideName = customData.copyTag().getString(LAST_SIDE_TAG);
        if (PortalGunPortalEntity.PortalSide.BLUE.name().equals(sideName)) {
            return PortalGunPortalEntity.PortalSide.BLUE;
        }
        if (PortalGunPortalEntity.PortalSide.ORANGE.name().equals(sideName)) {
            return PortalGunPortalEntity.PortalSide.ORANGE;
        }
        return null;
    }

    private void triggerFireAnimation(ServerLevel level, LivingEntity livingEntity, ItemStack stack) {
        long animatableId = GeoItem.getOrAssignId(stack, level);
        triggerAnim(livingEntity, animatableId, FIRE_CONTROLLER, "fire");
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.portal_gun.primary").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.portal_gun.secondary").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.portal_gun.reset").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, LED_CONTROLLER, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            PortalGunPortalEntity.PortalSide side = stack == null ? null : this.getLastSide(stack);
            String animation = side == null ? "off_led" : side == PortalGunPortalEntity.PortalSide.BLUE ? "blue_led" : "orange_led";
            return state.setAndContinue(RawAnimation.begin().thenLoop(animation));
        }));
        controllers.add(new AnimationController<>(this, FIRE_CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim("fire", RawAnimation.begin().then("fire", Animation.LoopType.PLAY_ONCE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<PortalGunItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }

    private record Placement(Vec3 center, Direction facing, Direction upAxis, float yaw, BlockPos supportOrigin) {
    }
}
