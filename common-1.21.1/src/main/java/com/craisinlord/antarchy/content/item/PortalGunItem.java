package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalFaceRecord;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalCellAccess;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonMath;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.craisinlord.antarchy.content.portalgun.PortalGunVariant;
import com.craisinlord.antarchy.content.portalgun.PortalGunResetManager;
import com.craisinlord.antarchy.config.AntarchySettings;
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
import net.minecraft.util.Mth;
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
    public static final String OWNER_ID_TAG = "antarchy.portal_gun_owner";
    public static final String PORTAL_WIDTH_TAG = "width";
    public static final String PORTAL_HEIGHT_TAG = "height";
    private static final int MAX_PORTAL_WIDTH = 16;
    private static final int MAX_PORTAL_HEIGHT = 16;
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/portal_gun.geo.json");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/portal_gun.animation.json");
    private final Supplier<? extends EntityType<? extends PortalGunPortalEntity>> portalType;
    private final Supplier<? extends EntityType<? extends PortalGunProjectileEntity>> projectileType;
    private final Supplier<? extends Block> portalMasterBlock;
    private final Supplier<? extends Block> portalBaseBlock;
    private final PortalGunVariant variant;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PortalGunItem(
            Item.Properties properties,
            Supplier<? extends EntityType<? extends PortalGunPortalEntity>> portalType,
            Supplier<? extends EntityType<? extends PortalGunProjectileEntity>> projectileType,
            Supplier<? extends Block> portalMasterBlock,
            Supplier<? extends Block> portalBaseBlock,
            PortalGunVariant variant
    ) {
        super(properties);
        this.variant = variant;
        this.portalType = portalType;
        this.projectileType = projectileType;
        this.portalMasterBlock = portalMasterBlock;
        this.portalBaseBlock = portalBaseBlock;
        GeoItem.registerSyncedAnimatable(this);
    }

    public PortalGunVariant getVariant() {
        return this.variant;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                this.resetPortals(serverPlayer, stack);
            }
            return InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer serverPlayer && PortalGunResetManager.handleOrangeReset(serverPlayer, stack)) {
            return InteractionResultHolder.consume(stack);
        }
        boolean fired = this.firePortal(serverLevel, player, stack, PortalGunPortalEntity.PortalSide.ORANGE, usedHand == InteractionHand.OFF_HAND);
        return fired ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    public void firePrimary(ServerLevel level, ServerPlayer player, ItemStack stack, boolean offhand) {
        this.firePortal(level, player, stack, PortalGunPortalEntity.PortalSide.BLUE, offhand);
    }

    public void resetPortals(ServerPlayer player, ItemStack stack) {
        UUID gunId = this.ensureGunId(stack, player.getUUID());
        PortalGunSavedData.clearAllPortals(player.serverLevel(), gunId);
        this.setLastSide(stack, null);
        this.setMoonSide(stack, null);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound("portal_fizzle"), SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    public void resetPortalSide(ServerPlayer player, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        UUID gunId = this.ensureGunId(stack, player.getUUID());
        PortalGunSavedData.clearPortalSide(player.serverLevel(), gunId, side);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound("portal_fizzle"), SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    private boolean damageGun(ServerLevel level, Player player, ItemStack stack, boolean offhand) {
        UUID gunId = getGunId(stack);
        EquipmentSlot slot = offhand ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        boolean[] broke = {false};
        stack.hurtAndBreak(1, level, player instanceof ServerPlayer serverPlayer ? serverPlayer : null, item -> {
            broke[0] = true;
            player.onEquippedItemBroken(item, slot);
            PortalGunSavedData.clearAllPortals(level, gunId);
        });
        return broke[0];
    }

    public static void clearPortalsOf(ServerLevel level, ItemStack stack) {
        if (stack.getItem() instanceof PortalGunItem) {
            PortalGunSavedData.clearAllPortals(level, getGunId(stack));
        }
    }

    public void fireMobPortal(ServerLevel level, LivingEntity shooter, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        this.ensureGunId(stack, shooter.getUUID());
        String firePath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_gun_fire_blue" : "portal_gun_fire_orange";
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), PortalGunPortalEntity.sound(firePath), shooter.getSoundSource(), 0.65F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.15F : 0.88F);
        this.spawnProjectile(level, shooter, stack, side);
    }

    private boolean firePortal(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side, boolean offhand) {
        if (this.raycast(level, player).getType() == HitResult.Type.MISS) {
            if (this.tryArmMoonShot(level, player, stack, side, offhand)) {
                return true;
            }
        }
        this.triggerFireAnimation(level, player, stack);
        this.ensureGunId(stack, player.getUUID());
        if (this.damageGun(level, player, stack, offhand)) {
            return true;
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 6);
        String firePath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_gun_fire_blue" : "portal_gun_fire_orange";
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound(firePath), SoundSource.PLAYERS, 0.65F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.15F : 0.88F);
        this.spawnProjectile(level, player, stack, side);
        return true;
    }

    public void handlePortalImpact(ServerLevel level, LivingEntity shooter, ItemStack stack, UUID gunId, PortalGunPortalEntity.PortalSide side, BlockHitResult hitResult, Vec3 impactPos, int portalWidth, int portalHeight) {
        UUID portalOwnerId = getPortalOwnerId(stack, shooter.getUUID());
        PortalGunPortalEntity existing = this.findPlacedPortal(level, gunId, side);
        PortalGunPlacement placement = this.findPlacement(level, shooter, gunId, side, hitResult, existing, portalWidth, portalHeight);
        if (placement == null) {
            Antarchy.LOGGER.debug("Portal gun placement failed owner={} side={} hitBlock={} hitFace={} hitLocation={}", shooter.getUUID(), side, hitResult.getBlockPos(), hitResult.getDirection(), hitResult.getLocation());
            level.playSound(null, impactPos.x, impactPos.y, impactPos.z, PortalGunPortalEntity.sound("portal_gun_invalid_surface"), SoundSource.PLAYERS, 0.55F, 1.0F);
            return;
        }
        if (existing != null && this.isSamePortalPlacement(existing, placement)) {
            existing.restartOpeningAnimation();
            if (stack.getItem() == this) {
                this.setLastSide(stack, side);
            }
            level.playSound(null, existing.getX(), existing.getY(), existing.getZ(), PortalGunPortalEntity.sound("portal_fizzle"), SoundSource.PLAYERS, 0.6F, 1.0F);
            return;
        }
        if (existing != null) {
            Antarchy.LOGGER.debug("Portal gun replacing existing portal owner={} side={} oldPortal={}", shooter.getUUID(), side, existing.getUUID());
            existing.discard();
        }
        if (this.isMoonSideArmed(stack, side)) {
            this.setMoonSide(stack, null);
        }
        boolean moon = shooter instanceof Player && this.isMoonSideArmed(stack, this.otherSide(side));
        if (moon) {
            this.setMoonSide(stack, null);
        }
        PortalGunPortalEntity portal = new PortalGunPortalEntity(this.portalType.get(), level);
        portal.configure(portalOwnerId, gunId, this.variant, side, placement);
        portal.moveTo(placement.center().x, placement.center().y, placement.center().z, placement.yaw(), 0.0F);
        if (!this.placePortalBlocks(level, portalOwnerId, gunId, side, portal, placement)) {
            Antarchy.LOGGER.debug("Portal gun block placement failed owner={} side={} portal={} center={} facing={} up={} master={} base={}", shooter.getUUID(), side, portal.getUUID(), placement.center(), placement.facing(), placement.upAxis(), placement.masterPos(), placement.basePos());
            level.playSound(null, impactPos.x, impactPos.y, impactPos.z, PortalGunPortalEntity.sound("portal_gun_invalid_surface"), SoundSource.PLAYERS, 0.55F, 1.0F);
            return;
        }
        if (moon) {
            portal.openToMoon();
        }
        level.addFreshEntity(portal);
        PortalGunSavedData.setPortal(level, gunId, portalOwnerId, side, portal.getUUID());
        PortalGunPortalEntity other = this.findCounterpart(level, gunId, side);
        if (other != null && (moon || other.isMoonPortal())) {
            other.discard();
            other = null;
        }
        if (other != null) {
            portal.linkTo(other);
            other.linkTo(portal);
            this.syncPortalBlocks(level, portal);
            this.syncPortalBlocks(level, other);
        }
        if (stack.getItem() == this) {
            this.setLastSide(stack, side);
        }
        String openPath = side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_open_blue" : "portal_open_orange";
        level.playSound(null, portal.getX(), portal.getY(), portal.getZ(), PortalGunPortalEntity.sound(openPath), SoundSource.PLAYERS, 0.45F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.05F : 0.92F);
    }

    private boolean isSamePortalPlacement(PortalGunPortalEntity portal, PortalGunPlacement placement) {
        if (portal.getPortalWidth() != placement.width()
                || portal.getPortalHeight() != placement.height()
                || portal.getFacingDirection() != placement.facing()
                || portal.getUpAxis() != placement.upAxis()
                || !portal.getMasterPos().equals(placement.masterPos())) {
            return false;
        }
        return new HashSet<>(java.util.Arrays.asList(portal.getPortalSpots()))
                .equals(new HashSet<>(java.util.Arrays.asList(placement.portalSpots())));
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

    private boolean tryArmMoonShot(ServerLevel level, Player player, ItemStack stack, PortalGunPortalEntity.PortalSide side, boolean offhand) {
        if (!this.isAimingAtMoon(level, player)) {
            return false;
        }
        UUID gunId = this.ensureGunId(stack, player.getUUID());
        PortalGunPortalEntity existing = this.findPlacedPortal(level, gunId, side);
        if (existing != null) {
            existing.discard();
        }
        PortalGunPortalEntity counterpart = this.findPlacedPortal(level, gunId, this.otherSide(side));
        if (counterpart != null && counterpart.isAlive() && !counterpart.isMoonPortal()) {
            counterpart.openToMoon();
            this.setMoonSide(stack, null);
        } else {
            this.setMoonSide(stack, side);
        }
        this.setLastSide(stack, side);
        this.triggerFireAnimation(level, player, stack);
        if (this.damageGun(level, player, stack, offhand)) {
            return true;
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 6);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PortalGunPortalEntity.sound(side == PortalGunPortalEntity.PortalSide.BLUE ? "portal_gun_fire_blue" : "portal_gun_fire_orange"), SoundSource.PLAYERS, 0.65F, side == PortalGunPortalEntity.PortalSide.BLUE ? 1.15F : 0.88F);
        this.spawnShotTrail(level, player, player.getEyePosition().add(this.worldLook(player).scale(64.0D)), side);
        return true;
    }

    private boolean isAimingAtMoon(ServerLevel level, Player player) {
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling()) {
            return false;
        }
        return PortalGunMoonMath.isAimedAtMoon(this.worldLook(player), level.getTimeOfDay(1.0F));
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

    private void spawnProjectile(ServerLevel level, LivingEntity shooter, ItemStack stack, PortalGunPortalEntity.PortalSide side) {
        UUID gunId = this.ensureGunId(stack, shooter.getUUID());
        PortalGunProjectileEntity projectile = new PortalGunProjectileEntity(this.projectileType.get(), shooter, level);
        projectile.configure(side, gunId, stack);
        Vec3 look = this.worldLook(shooter);
        Vec3 start = shooter.getEyePosition();
        double launchSpeed = PortalGunProjectileEntity.MIN_LAUNCH_SPEED + level.getRandom().nextDouble() * PortalGunProjectileEntity.LAUNCH_SPEED_VARIANCE;
        projectile.setProjectileSpeed(launchSpeed);
        projectile.setPos(start.x, start.y, start.z);
        projectile.setDeltaMovement(look.scale(projectile.getProjectileSpeed()));
        projectile.syncLaunchState();
        level.addFreshEntity(projectile);
    }

    public static int getPortalWidth(ItemStack stack) {
        net.minecraft.world.item.component.CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return Mth.clamp(data == null ? 1 : data.copyTag().getInt(PORTAL_WIDTH_TAG), 1, MAX_PORTAL_WIDTH);
    }

    public static int getPortalHeight(ItemStack stack) {
        net.minecraft.world.item.component.CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return Mth.clamp(data == null || !data.copyTag().contains(PORTAL_HEIGHT_TAG) ? 2 : data.copyTag().getInt(PORTAL_HEIGHT_TAG), 2, MAX_PORTAL_HEIGHT);
    }

    private PortalGunPortalEntity findCounterpart(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        PortalGunPortalEntity.PortalSide otherSide = side == PortalGunPortalEntity.PortalSide.BLUE ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        Optional<UUID> otherId = PortalGunSavedData.getPortalId(level.getServer(), gunId, otherSide, level.dimension().location());
        if (otherId.isEmpty()) {
            return null;
        }
        Entity entity = level.getEntity(otherId.get());
        return entity instanceof PortalGunPortalEntity portal ? portal : null;
    }

    private PortalGunPortalEntity findPlacedPortal(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        Optional<UUID> portalId = PortalGunSavedData.getPortalId(level.getServer(), gunId, side, level.dimension().location());
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
        Vec3 end = start.add(this.worldLook(player).scale(64.0D));
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    private Vec3 worldLook(LivingEntity entity) {
        Vec3 look = entity.getLookAngle();
        return AntarchyGravityApi.isGravityInverted(entity)
                ? com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil.vecPlayerToWorld(
                        look, AntarchyGravityApi.getGravityDirection(entity))
                : look;
    }

    private PortalGunPlacement findPlacement(Level level, LivingEntity shooter, UUID gunId, PortalGunPortalEntity.PortalSide side, BlockHitResult hitResult, PortalGunPortalEntity replacingPortal, int requestedWidth, int requestedHeight) {
        int maxWidth = Mth.clamp(requestedWidth, 1, 16);
        int maxHeight = Mth.clamp(requestedHeight, 2, 16);
        boolean canResize = AntarchySettings.portalGunCanPortalsResizeWhenCreated();
        for (int height = maxHeight; height >= 2; height--) {
            for (int width = maxWidth; width >= 1; width--) {
                if (!canResize && (width != maxWidth || height != maxHeight)) {
                    continue;
                }
                PortalGunPlacement placement = this.findPlacementForSize(level, shooter, gunId, side, hitResult, replacingPortal, width, height);
                if (placement != null) {
                    return placement;
                }
            }
        }
        return null;
    }

    private PortalGunPlacement findPlacementForSize(Level level, LivingEntity shooter, UUID gunId, PortalGunPortalEntity.PortalSide side, BlockHitResult hitResult, PortalGunPortalEntity replacingPortal, int width, int height) {
        Direction facing = hitResult.getDirection();
        Direction heightAxis = this.resolveUpAxis(shooter, facing);
        Direction widthAxis = PortalGunPlacement.widthAxis(facing, heightAxis);
        float yaw = PortalGunPlacement.yawFor(facing, heightAxis);
        BlockPos hitPos = hitResult.getBlockPos();
        int maxRadius = Math.max(width, height);
        List<Integer> radiusOrder = new java.util.ArrayList<>(maxRadius);
        for (int radius = 0; radius < maxRadius; radius++) {
            radiusOrder.add(radius);
        }
        int preferredRadius = maxRadius / 2;
        radiusOrder.remove(Integer.valueOf(preferredRadius));
        radiusOrder.add(0, preferredRadius);
        for (int radius : radiusOrder) {
            for (int horizontalOffset = radius; horizontalOffset >= 0; horizontalOffset--) {
                for (int verticalOffset = radius; verticalOffset >= 0; verticalOffset--) {
                    List<BlockPos> supports = new java.util.ArrayList<>(width * height);
                    for (int widthCell = 1 - width; widthCell <= 0; widthCell++) {
                        for (int heightCell = 1 - height; heightCell <= 0; heightCell++) {
                            supports.add(this.referenceSupportPosition(hitPos, facing, heightAxis, horizontalOffset, verticalOffset, widthCell, heightCell));
                        }
                    }
                    if (!supports.contains(hitPos)) {
                        continue;
                    }
                    BlockPos[] portalSpots = new BlockPos[supports.size()];
                    boolean valid = true;
                    int spotIndex = 0;
                    for (BlockPos supportPos : supports) {
                        BlockPos portalPos = supportPos.relative(facing);
                        BlockState supportState = level.getBlockState(supportPos);
                        BlockState portalState = level.getBlockState(portalPos);
                        if (!supportState.isFaceSturdy(level, supportPos, facing)
                                || !this.isPortalSpaceAvailable(level, portalPos, portalState, gunId, side, facing)) {
                            valid = false;
                            break;
                        }
                        portalSpots[spotIndex++] = portalPos;
                    }
                    if (!valid) {
                        continue;
                    }
                    BlockPos masterPos = null;
                    for (BlockPos portalSpot : portalSpots) {
                        BlockState state = level.getBlockState(portalSpot);
                        boolean belongsToReplacingPortal = replacingPortal != null && replacingPortal.containsPortalSpot(portalSpot);
                        boolean existingPortalCell = level.getBlockEntity(portalSpot) instanceof PortalGunPortalCellAccess;
                        if ((state.canBeReplaced() || belongsToReplacingPortal || existingPortalCell) && level.getFluidState(portalSpot).isEmpty()) {
                            masterPos = portalSpot;
                            break;
                        }
                    }
                    if (masterPos == null) {
                        return null;
                    }
                    BlockPos[] orderedPortalSpots = new BlockPos[portalSpots.length];
                    orderedPortalSpots[0] = masterPos;
                    int orderedSpotIndex = 1;
                    for (BlockPos portalSpot : portalSpots) {
                        if (!portalSpot.equals(masterPos)) {
                            orderedPortalSpots[orderedSpotIndex++] = portalSpot;
                        }
                    }
                    portalSpots = orderedPortalSpots;
                    Vec3 center = Vec3.ZERO;
                    Set<BlockPos> compensatedSpots = new HashSet<>();
                    for (BlockPos portalSpot : portalSpots) {
                        center = center.add(portalSpot.getCenter());
                        if (horizontalOffset != 0 || verticalOffset != 0) {
                            compensatedSpots.add(portalSpot);
                            compensatedSpots.add(portalSpot.relative(facing.getOpposite()));
                        }
                    }
                    center = center.scale(1.0D / portalSpots.length)
                            .subtract(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5D));
                    BlockPos supportOrigin = masterPos.relative(facing.getOpposite());
                    BlockPos basePos = portalSpots.length > 1 ? portalSpots[1] : portalSpots[0];
                    return new PortalGunPlacement(center, facing, heightAxis, widthAxis, yaw, supportOrigin, masterPos, basePos, portalSpots, compensatedSpots, width, height);
                }
            }
        }
        return null;
    }

    private BlockPos referenceSupportPosition(BlockPos origin, Direction facing, Direction upAxis, int horizontalOffset, int verticalOffset, int widthCell, int heightCell) {
        int x;
        int y;
        int z;
        if (facing.getAxis() != Direction.Axis.Y) {
            if (facing.getAxis() == Direction.Axis.Z) {
                x = horizontalOffset + widthCell;
                z = 0;
            } else {
                x = 0;
                z = horizontalOffset + widthCell;
            }
            y = -verticalOffset - heightCell;
        } else if (upAxis == Direction.NORTH) {
            x = horizontalOffset + widthCell;
            y = 0;
            z = verticalOffset + heightCell;
        } else if (upAxis == Direction.SOUTH) {
            x = horizontalOffset + widthCell;
            y = 0;
            z = -verticalOffset - heightCell;
        } else if (upAxis == Direction.WEST) {
            x = -verticalOffset - heightCell;
            y = 0;
            z = horizontalOffset + widthCell;
        } else {
            x = verticalOffset + heightCell;
            y = 0;
            z = horizontalOffset + widthCell;
        }
        return origin.offset(x, y, z);
    }

    private Direction resolveUpAxis(LivingEntity shooter, Direction facing) {
        if (facing.getAxis() != Direction.Axis.Y) {
            return Direction.UP;
        }
        Direction horizontal = shooter.getDirection();
        if (horizontal.getAxis() == Direction.Axis.Y) {
            return Direction.NORTH;
        }
        return horizontal;
    }

    private boolean isPortalSpaceAvailable(Level level, BlockPos pos, BlockState state, UUID gunId, PortalGunPortalEntity.PortalSide side, Direction face) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PortalGunPortalCellAccess portalCell) {
            PortalGunPortalFaceRecord record = portalCell.portalGun$getFaceRecord(face);
            if (record == null) {
                if (blockEntity instanceof PortalGunPortalMasterBlockEntity master && master.getPortalId() != null && master.getFacing() == face) {
                    return gunId != null && gunId.equals(master.getGunId()) && master.getSide() == side;
                }
                if (blockEntity instanceof PortalGunPortalBaseBlockEntity base && base.getPortalId() != null) {
                    BlockEntity masterEntity = level.getBlockEntity(base.getMasterPos());
                    if (!(masterEntity instanceof PortalGunPortalMasterBlockEntity master)) {
                        return false;
                    }
                    return master.getFacing() != face
                            || (gunId != null
                            && gunId.equals(master.getGunId())
                            && base.getSide() == side
                            && master.getSide() == side);
                }
                return true;
            }
            return gunId != null
                    && gunId.equals(record.gunId())
                    && record.side() == side;
        }
        return state.getCollisionShape(level, pos).isEmpty();
    }

    private boolean placePortalBlocks(ServerLevel level, UUID ownerId, UUID gunId, PortalGunPortalEntity.PortalSide side, PortalGunPortalEntity portal, PortalGunPlacement placement) {
        int pairTime = level.getServer().getTickCount();
        for (int i = 0; i < placement.portalSpots().length; i++) {
            BlockPos spot = placement.portalSpots()[i];
            BlockEntity existing = level.getBlockEntity(spot);
            boolean masterRole = i == 0;
            if (!(existing instanceof PortalGunPortalCellAccess)) {
                BlockState block = masterRole ? this.portalMasterBlock.get().defaultBlockState() : this.portalBaseBlock.get().defaultBlockState();
                if (!level.setBlock(spot, block, 3)) {
                    for (BlockPos placed : placement.portalSpots()) {
                        BlockEntity cell = level.getBlockEntity(placed);
                        if (cell instanceof PortalGunPortalCellAccess portalCell) {
                            portalCell.portalGun$removeFaceRecord(placement.facing(), portal.getUUID());
                        }
                    }
                    return false;
                }
                existing = level.getBlockEntity(spot);
            }
            if (!(existing instanceof PortalGunPortalCellAccess cell)
                    || !cell.portalGun$putFaceRecord(new PortalGunPortalFaceRecord(
                    ownerId,
                    gunId,
                    this.variant,
                    portal.getUUID(),
                    portal.getLinkedPortalId(),
                    side,
                    placement.facing(),
                    placement.upAxis(),
                    placement.masterPos(),
                    placement.basePos(),
                    java.util.Arrays.asList(placement.portalSpots()),
                    placement.compensatedSpots(),
                    pairTime,
                    placement.width(),
                    placement.height(),
                    masterRole
            ))) {
                return false;
            }
            if (masterRole && existing instanceof PortalGunPortalMasterBlockEntity master
                    && (master.getPortalId() == null || master.getPortalId().equals(portal.getUUID()))) {
                master.configure(ownerId, gunId, this.variant, portal.getUUID(), side, placement.facing(), placement.upAxis(), placement.basePos(), placement.portalSpots(), placement.compensatedSpots(), pairTime, placement.width(), placement.height());
            } else if (!masterRole && existing instanceof PortalGunPortalBaseBlockEntity base
                    && (base.getPortalId() == null || base.getPortalId().equals(portal.getUUID()))) {
                base.configure(ownerId, portal.getUUID(), side, placement.masterPos());
            }
        }
        return true;
    }

    private void syncPortalBlocks(ServerLevel level, PortalGunPortalEntity portal) {
        for (BlockPos portalSpot : portal.getPortalSpots()) {
            if (level.getBlockEntity(portalSpot) instanceof PortalGunPortalCellAccess cell) {
                cell.portalGun$updateFacePair(portal.getUUID(), portal.getLinkedPortalId(), portal.getPairTime());
            }
            if (portalSpot.equals(portal.getMasterPos()) && level.getBlockEntity(portalSpot) instanceof PortalGunPortalMasterBlockEntity master && portal.getUUID().equals(master.getPortalId())) {
                master.updatePair(portal.getLinkedPortalId(), portal.getPairTime());
            } else if (!portalSpot.equals(portal.getMasterPos()) && level.getBlockEntity(portalSpot) instanceof PortalGunPortalBaseBlockEntity base && portal.getUUID().equals(base.getPortalId())) {
                base.updatePair(portal.getLinkedPortalId(), portal.getPairTime());
            }
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
        return side == getMoonSide(stack);
    }

    public static PortalGunPortalEntity.PortalSide getMoonSide(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        String sideName = customData.copyTag().getString(MOON_SIDE_TAG);
        for (PortalGunPortalEntity.PortalSide side : PortalGunPortalEntity.PortalSide.values()) {
            if (side.name().equals(sideName)) {
                return side;
            }
        }
        return null;
    }

    public static UUID getGunId(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.copyTag().hasUUID(GUN_ID_TAG)) {
            return null;
        }
        return customData.copyTag().getUUID(GUN_ID_TAG);
    }

    private UUID ensureGunId(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        UUID gunId;
        if (customData != null && customData.copyTag().hasUUID(GUN_ID_TAG)) {
            gunId = customData.copyTag().getUUID(GUN_ID_TAG);
        } else {
            gunId = UUID.randomUUID();
        }
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putUUID(GUN_ID_TAG, gunId);
            if (!tag.contains(LAST_SIDE_TAG)) {
                tag.putString(LAST_SIDE_TAG, PortalGunPortalEntity.PortalSide.BLUE.name());
            }
        });
        return gunId;
    }

    private UUID ensureGunId(ItemStack stack, UUID ownerId) {
        UUID gunId = this.ensureGunId(stack);
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (!tag.contains(OWNER_ID_TAG)) {
                tag.putString(OWNER_ID_TAG, ownerId.toString());
            }
        });
        return gunId;
    }

    public static UUID ensureGunIdentity(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PortalGunItem item)) {
            return null;
        }
        return item.ensureGunId(stack);
    }

    public static UUID ensureGunIdentity(ItemStack stack, Player owner) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PortalGunItem item)) {
            return null;
        }
        return item.ensureGunId(stack, owner.getUUID());
    }

    public static UUID getPortalOwnerId(ItemStack stack, UUID fallback) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return fallback;
        }
        String owner = customData.copyTag().getString(OWNER_ID_TAG);
        try {
            return owner.isEmpty() ? fallback : UUID.fromString(owner);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
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

    private Component portalColorName(PortalGunPortalEntity.PortalSide side) {
        return Component.translatable(this.variant.colorTranslationKey(side))
                .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(this.variant.color(side))));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.portal_gun.primary", this.portalColorName(PortalGunPortalEntity.PortalSide.BLUE)).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.portal_gun.secondary", this.portalColorName(PortalGunPortalEntity.PortalSide.ORANGE)).withStyle(ChatFormatting.GRAY));
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
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, PortalGunItem.this.variant.modelTexture(), ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }

}
