package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunBlackHoleEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    private static final String MOON_SIDE_TAG = "antarchy.portal_gun_moon_side";
    public static final String GUN_ID_TAG = "antarchy.portal_gun_id";
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/portal_gun.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/portal_gun.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/portal_gun.animation.json");
    private final Supplier<? extends EntityType<? extends PortalGunPortalEntity>> portalType;
    private final Supplier<? extends EntityType<? extends PortalGunBlackHoleEntity>> blackHoleType;
    private final Supplier<? extends EntityType<? extends PortalGunProjectileEntity>> projectileType;
    private final Supplier<? extends Block> portalMasterBlock;
    private final Supplier<? extends Block> portalBaseBlock;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PortalGunItem(
            Item.Properties properties,
            Supplier<? extends EntityType<? extends PortalGunPortalEntity>> portalType,
            Supplier<? extends EntityType<? extends PortalGunBlackHoleEntity>> blackHoleType,
            Supplier<? extends EntityType<? extends PortalGunProjectileEntity>> projectileType,
            Supplier<? extends Block> portalMasterBlock,
            Supplier<? extends Block> portalBaseBlock
    ) {
        super(properties);
        this.portalType = portalType;
        this.blackHoleType = blackHoleType;
        this.projectileType = projectileType;
        this.portalMasterBlock = portalMasterBlock;
        this.portalBaseBlock = portalBaseBlock;
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
            this.setMoonSide(stack, null);
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
        if (this.raycast(level, player).getType() == HitResult.Type.MISS) {
            if (this.tryArmMoonShot(level, player, stack, side)) {
                return true;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound("portal_gun_invalid_surface", SoundEvents.DISPENSER_FAIL), SoundSource.PLAYERS, 0.55F, 1.0F);
            return false;
        }
        this.triggerFireAnimation(level, player, stack);
        this.setLastSide(stack, side);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 6);
        String firePath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_gun_fire_blue" : "portal_gun_fire_orange";
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound(firePath, SoundEvents.BEACON_ACTIVATE), SoundSource.PLAYERS, 0.65F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.15F : 0.88F);
        this.spawnProjectile(level, player, stack, side);
        return true;
    }

    public void handlePortalImpact(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side, BlockHitResult hitResult, Vec3 impactPos) {
        PortalGunPortalEntity existing = this.findPlacedPortal(level, player.getUUID(), side);
        PortalGunPlacement placement = this.findPlacement(level, player, side, hitResult, existing);
        if (placement == null) {
            level.playSound(null, impactPos.x, impactPos.y, impactPos.z, PortalGunPortalEntity.sound("portal_gun_invalid_surface", SoundEvents.DISPENSER_FAIL), SoundSource.PLAYERS, 0.55F, 1.0F);
            return;
        }
        if (existing != null) {
            existing.discard();
        }
        if (this.isMoonSideArmed(stack, side)) {
            this.setMoonSide(stack, null);
        }
        if (this.isMoonSideArmed(stack, this.otherSide(side))) {
            this.spawnBlackHole(level, player, stack, side, placement.center());
            return;
        }
        PortalGunPortalEntity portal = new PortalGunPortalEntity(this.portalType.get(), level);
        portal.configure(player.getUUID(), side, placement);
        portal.moveTo(placement.center().x, placement.center().y, placement.center().z, placement.yaw(), 0.0F);
        if (!this.placePortalBlocks(level, player.getUUID(), side, portal, placement)) {
            level.playSound(null, impactPos.x, impactPos.y, impactPos.z, PortalGunPortalEntity.sound("portal_gun_invalid_surface", SoundEvents.DISPENSER_FAIL), SoundSource.PLAYERS, 0.55F, 1.0F);
            return;
        }
        level.addFreshEntity(portal);
        PortalGunSavedData.setPortal(level.getServer(), player.getUUID(), side, portal.getUUID());
        PortalGunPortalEntity other = this.findCounterpart(level, player.getUUID(), side);
        if (other != null) {
            portal.linkTo(other);
            other.linkTo(portal);
            this.syncPortalBlocks(level, portal);
            this.syncPortalBlocks(level, other);
        }
        String openPath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_open_blue" : "portal_open_orange";
        level.playSound(null, portal.getX(), portal.getY(), portal.getZ(), PortalGunPortalEntity.sound(openPath, SoundEvents.END_PORTAL_SPAWN), SoundSource.PLAYERS, 0.45F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.05F : 0.92F);
    }

    public ItemStack findMatchingGunStack(Player player, UUID gunId) {
        ItemStack mainHand = player.getMainHandItem();
        if (this.matchesGunId(mainHand, gunId)) {
            return mainHand;
        }
        ItemStack offhand = player.getOffhandItem();
        if (this.matchesGunId(offhand, gunId)) {
            return offhand;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (this.matchesGunId(stack, gunId)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean tryArmMoonShot(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        if (!level.dimensionType().hasSkyLight() || !level.isNight() || player.getXRot() > -40.0F) {
            return false;
        }
        PortalGunPortalEntity existing = this.findPlacedPortal(level, player.getUUID(), side);
        if (existing != null) {
            existing.discard();
        }
        this.setMoonSide(stack, side);
        this.setLastSide(stack, side);
        this.triggerFireAnimation(level, player, stack);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 6);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound(side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_gun_fire_blue" : "portal_gun_fire_orange", SoundEvents.BEACON_ACTIVATE), SoundSource.PLAYERS, 0.65F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.15F : 0.88F);
        this.spawnShotTrail(level, player, player.getEyePosition().add(player.getLookAngle().scale(64.0D)), side);
        return true;
    }

    private boolean spawnBlackHole(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side, Vec3 spawnPos) {
        this.setMoonSide(stack, null);
        PortalGunBlackHoleEntity blackHole = new PortalGunBlackHoleEntity(this.blackHoleType.get(), level);
        blackHole.configure(player.getUUID());
        blackHole.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 0.0F, 0.0F);
        level.addFreshEntity(blackHole);
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, PortalGunPortalEntity.sound("portal_gun_black_hole", SoundEvents.WARDEN_SONIC_BOOM), SoundSource.PLAYERS, 0.6F, 0.6F);
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

    private void spawnProjectile(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        UUID gunId = this.ensureGunId(stack);
        PortalGunProjectileEntity projectile = new PortalGunProjectileEntity(this.projectileType.get(), player, level);
        projectile.configure(side, gunId, stack);
        Vec3 look = player.getLookAngle();
        Vec3 start = player.getEyePosition().add(look.scale(0.4D));
        projectile.setPos(start.x, start.y, start.z);
        projectile.setDeltaMovement(look.scale(2.5D));
        level.addFreshEntity(projectile);
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

    private PortalGunPortalEntity findPlacedPortal(ServerLevel level, UUID owner, PortalGunPortalEntity.PortalSide side) {
        Optional<UUID> portalId = PortalGunSavedData.getPortalId(level.getServer(), owner, side);
        if (portalId.isEmpty()) {
            return null;
        }
        Entity entity = level.getEntity(portalId.get());
        return entity instanceof PortalGunPortalEntity portal ? portal : null;
    }

    private PortalGunPortalEntity.PortalSide otherSide(PortalGunPortalEntity.PortalSide side) {
        return side == PortalGunPortalEntity.PortalSide.BLUE ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
    }

    private BlockHitResult raycast(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(64.0D));
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    private PortalGunPlacement findPlacement(Level level, Player player, PortalGunPortalEntity.PortalSide side, BlockHitResult hitResult, PortalGunPortalEntity replacingPortal) {
        Direction facing = hitResult.getDirection();
        Direction heightAxis = this.resolveUpAxis(player, facing);
        Direction widthAxis = PortalGunPlacement.widthAxis(facing, heightAxis);
        float yaw = PortalGunPlacement.yawFor(facing, heightAxis);
        BlockPos hitPos = hitResult.getBlockPos();
        Vec3 hitLocation = hitResult.getLocation();
        UUID replacingPortalId = replacingPortal == null ? null : replacingPortal.getUUID();
        PortalGunPlacement bestPlacement = null;
        double bestScore = Double.MAX_VALUE;
        for (int widthOffset = -3; widthOffset <= 3; widthOffset++) {
            for (int heightOffset = -4; heightOffset <= 3; heightOffset++) {
                BlockPos supportOrigin = hitPos.relative(widthAxis, widthOffset).relative(heightAxis, heightOffset);
                if (!this.canPlaceAt(level, player.getUUID(), side, supportOrigin, facing, heightAxis, replacingPortalId)) {
                    continue;
                }
                BlockPos masterPos = supportOrigin.relative(facing);
                BlockPos basePos = masterPos.relative(heightAxis);
                BlockPos[] portalSpots = new BlockPos[] {masterPos, basePos};
                Vec3 firstCellCenter = masterPos.getCenter();
                Vec3 center = firstCellCenter
                        .add(Vec3.atLowerCornerOf(heightAxis.getNormal()).scale(0.5D))
                        .subtract(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5D));
                Vec3 local = hitLocation.subtract(center);
                double widthLocal = local.dot(Vec3.atLowerCornerOf(widthAxis.getNormal()));
                double heightLocal = local.dot(Vec3.atLowerCornerOf(heightAxis.getNormal()));
                double depthLocal = local.dot(Vec3.atLowerCornerOf(facing.getNormal()));
                double widthDistance = Math.max(0.0D, Math.abs(widthLocal) - 0.5D);
                double heightDistance = Math.max(0.0D, Math.abs(heightLocal) - 1.0D);
                double score = widthDistance * widthDistance
                        + heightDistance * heightDistance
                        + Math.abs(depthLocal) * 0.25D
                        + supportOrigin.distSqr(hitPos) * 0.12D
                        + Math.abs(widthOffset) * 0.04D
                        + Math.abs(heightOffset) * 0.025D
                        + (widthOffset != 0 || heightOffset != 0 ? 0.02D : 0.0D);
                if (score < bestScore) {
                    bestScore = score;
                    Set<BlockPos> compensatedSpots = new HashSet<>();
                    if (widthOffset != 0 || heightOffset != 0) {
                        compensatedSpots.add(hitPos.relative(facing));
                        compensatedSpots.add(masterPos);
                        compensatedSpots.add(basePos);
                        compensatedSpots.add(supportOrigin);
                        compensatedSpots.add(basePos.relative(facing.getOpposite()));
                    }
                    bestPlacement = new PortalGunPlacement(center, facing, heightAxis, widthAxis, yaw, supportOrigin, masterPos, basePos, portalSpots, compensatedSpots);
                }
            }
        }
        return bestPlacement;
    }

    private Direction resolveUpAxis(Player player, Direction facing) {
        if (facing.getAxis() != Direction.Axis.Y) {
            return Direction.UP;
        }
        Direction horizontal = player.getDirection();
        if (horizontal.getAxis() == Direction.Axis.Y) {
            return Direction.NORTH;
        }
        return horizontal.getOpposite();
    }

    private boolean canPlaceAt(Level level, UUID ownerId, PortalGunPortalEntity.PortalSide side, BlockPos supportOrigin, Direction facing, Direction heightAxis, UUID replacingPortalId) {
        for (int h = 0; h < 2; h++) {
            BlockPos supportPos = supportOrigin.relative(heightAxis, h);
            BlockPos airPos = supportPos.relative(facing);
            BlockState supportState = level.getBlockState(supportPos);
            BlockState airState = level.getBlockState(airPos);
            FluidState fluidState = level.getFluidState(airPos);
            if (!supportState.isFaceSturdy(level, supportPos, facing)) {
                return false;
            }
            if (!this.isPortalSpaceAvailable(level, airPos, airState, ownerId, side, replacingPortalId)) {
                return false;
            }
            if (!fluidState.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isPortalSpaceAvailable(Level level, BlockPos pos, BlockState state, UUID ownerId, PortalGunPortalEntity.PortalSide side, UUID replacingPortalId) {
        if (state.isAir() || state.canBeReplaced()) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PortalGunPortalMasterBlockEntity master) {
            return ownerId != null
                    && ownerId.equals(master.getOwnerId())
                    && master.getSide() == side
                    && (replacingPortalId == null || replacingPortalId.equals(master.getPortalId()));
        }
        if (blockEntity instanceof PortalGunPortalBaseBlockEntity base) {
            return ownerId != null
                    && ownerId.equals(base.getOwnerId())
                    && base.getSide() == side
                    && (replacingPortalId == null || replacingPortalId.equals(base.getPortalId()));
        }
        return false;
    }

    private boolean placePortalBlocks(ServerLevel level, UUID ownerId, PortalGunPortalEntity.PortalSide side, PortalGunPortalEntity portal, PortalGunPlacement placement) {
        if (!level.setBlock(placement.masterPos(), this.portalMasterBlock.get().defaultBlockState(), 3)) {
            return false;
        }
        if (!level.setBlock(placement.basePos(), this.portalBaseBlock.get().defaultBlockState(), 3)) {
            level.setBlock(placement.masterPos(), Blocks.AIR.defaultBlockState(), 3);
            return false;
        }
        if (!(level.getBlockEntity(placement.masterPos()) instanceof PortalGunPortalMasterBlockEntity master) || !(level.getBlockEntity(placement.basePos()) instanceof PortalGunPortalBaseBlockEntity base)) {
            level.setBlock(placement.masterPos(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(placement.basePos(), Blocks.AIR.defaultBlockState(), 3);
            return false;
        }
        master.configure(ownerId, portal.getUUID(), side, placement.facing(), placement.upAxis(), placement.basePos(), placement.portalSpots(), placement.compensatedSpots(), level.getServer().getTickCount());
        base.configure(ownerId, portal.getUUID(), side, placement.masterPos());
        return true;
    }

    private void syncPortalBlocks(ServerLevel level, PortalGunPortalEntity portal) {
        if (level.getBlockEntity(portal.getMasterPos()) instanceof PortalGunPortalMasterBlockEntity master) {
            master.updatePair(portal.getLinkedPortalId(), portal.getPairTime());
        }
        if (level.getBlockEntity(portal.getBasePos()) instanceof PortalGunPortalBaseBlockEntity base) {
            base.updatePair(portal.getLinkedPortalId(), portal.getPairTime());
        }
    }

    private void setLastSide(ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        if (side == null) {
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(LAST_SIDE_TAG));
            return;
        }
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(LAST_SIDE_TAG, side.name()));
    }

    private void setMoonSide(ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        if (side == null) {
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(MOON_SIDE_TAG));
            return;
        }
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(MOON_SIDE_TAG, side.name()));
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

    private boolean isMoonSideArmed(ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        String sideName = customData.copyTag().getString(MOON_SIDE_TAG);
        return side.name().equals(sideName);
    }

    private UUID ensureGunId(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().hasUUID(GUN_ID_TAG)) {
            return customData.copyTag().getUUID(GUN_ID_TAG);
        }
        UUID gunId = UUID.randomUUID();
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(GUN_ID_TAG, gunId));
        return gunId;
    }

    private boolean matchesGunId(ItemStack stack, UUID gunId) {
        if (stack.isEmpty() || stack.getItem() != this) {
            return false;
        }
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().hasUUID(GUN_ID_TAG) && gunId.equals(customData.copyTag().getUUID(GUN_ID_TAG));
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

}
