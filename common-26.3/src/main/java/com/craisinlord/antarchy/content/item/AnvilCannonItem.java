package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.entity.LaunchedAnvilEntity;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class AnvilCannonItem extends ProjectileWeaponItem implements GeoItem {
    private static final String FIRE_CONTROLLER = "fire_controller";
    private static final String FIRE_ANIMATION = "shoot";
    private static final Predicate<ItemStack> AMMO = stack -> stack.is(AntarchyTags.Items.ANVIL_CANNON_AMMO);
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "anvil_cannon");
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "textures/models/item/anvil_cannon.png");
    private static final Identifier ANIMATION_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "anvil_cannon");
    private static final float[] MULTISHOT_YAW_OFFSETS = {0.0F, -10.0F, 10.0F};
    private static final int SHAKE_TICKS = 12;

    public static int clientShakeTicks = 0;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public AnvilCannonItem(Properties properties) {
        super(properties.enchantable(1));
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack cannon = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(cannon)) {
            return InteractionResult.FAIL;
        }

        boolean infinite = player.getAbilities().instabuild;
        if (!infinite && findAmmo(player).isEmpty()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 0.6F);
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            clientShakeTicks = SHAKE_TICKS;
        } else {
            int shots = hasMultishot(cannon) ? MULTISHOT_YAW_OFFSETS.length : 1;
            Vec3 recoilDirection = null;
            for (int shot = 0; shot < shots; shot++) {
                ItemStack ammo = infinite ? ItemStack.EMPTY : findAmmo(player);
                if (!infinite && ammo.isEmpty()) {
                    break;
                }
                BlockState anvilState = anvilStateFor(infinite ? new ItemStack(Blocks.ANVIL) : ammo, player.getDirection());
                if (!infinite) {
                    ammo.shrink(1);
                }
                Vec3 direction = fireFromPlayer(level, player, MULTISHOT_YAW_OFFSETS[shot], anvilState);
                if (recoilDirection == null) {
                    recoilDirection = direction;
                }
            }
            if (recoilDirection != null) {
                applyRecoil(player, recoilDirection);
            }
            spawnFireParticles((ServerLevel) level, player);
            cannon.hurtAndBreak(1, player, usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            triggerFireAnimation((ServerLevel) level, player, cannon);
        }

        playFireSound(level, player.getX(), player.getY(), player.getZ());
        player.awardStat(Stats.ITEM_USED.get(this));
        double cooldownSeconds = AntarchySettings.anvilCannonCooldownSeconds();
        if (cooldownSeconds > 0) {
            player.getCooldowns().addCooldown(cannon, (int) (cooldownSeconds * 20));
        }
        return InteractionResult.CONSUME;
    }

    public static void clientTick(Player player) {
        if (!player.level().isClientSide() || clientShakeTicks <= 0) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof AnvilCannonItem || player.getOffhandItem().getItem() instanceof AnvilCannonItem) {
            clientShakeTicks--;
        }
    }

    public static ItemStack findAmmo(Player player) {
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (AMMO.test(offhand)) {
            return offhand;
        }
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (AMMO.test(mainHand)) {
            return mainHand;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (AMMO.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isAmmo(ItemStack stack) {
        return AMMO.test(stack);
    }

    public static BlockState anvilStateFor(ItemStack ammo, Direction facing) {
        BlockState state = ammo.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AnvilBlock
                ? blockItem.getBlock().defaultBlockState()
                : Blocks.ANVIL.defaultBlockState();
        Direction horizontal = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        return state.setValue(AnvilBlock.FACING, horizontal.getClockWise());
    }

    public static void launchAnvil(Level level, Vec3 spawn, Vec3 velocity, @Nullable Entity owner, BlockState anvilState) {
        LaunchedAnvilEntity anvil = AntarchyObjects.LAUNCHED_ANVIL.get().create(level, owner == null ? EntitySpawnReason.DISPENSER : EntitySpawnReason.TRIGGERED);
        if (anvil == null) {
            return;
        }
        anvil.setAnvilState(anvilState);
        anvil.setOwner(owner);
        anvil.snapTo(spawn.x, spawn.y, spawn.z, owner == null ? 0.0F : owner.getYRot(), 0.0F);
        anvil.setDeltaMovement(velocity);
        level.addFreshEntity(anvil);
    }

    public static void playFireSound(Level level, double x, double y, double z) {
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.55F + level.getRandom().nextFloat() * 0.1F);
        level.playSound(null, x, y, z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.6F);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return AMMO;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return AMMO;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0;
    }

    @Override
    protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int i, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), angle, velocity, inaccuracy);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AnvilCannonItem>(FIRE_CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim(FIRE_ANIMATION, RawAnimation.begin().thenPlay(FIRE_ANIMATION)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<AnvilCannonItem> renderer;

            @Override
            public com.geckolib.renderer.GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }

    private void triggerFireAnimation(ServerLevel level, LivingEntity livingEntity, ItemStack stack) {
        long animatableId = GeoItem.getOrAssignId(stack, level);
        triggerAnim(livingEntity, animatableId, FIRE_CONTROLLER, FIRE_ANIMATION);
    }

    private static Vec3 fireFromPlayer(Level level, Player player, float yawOffset, BlockState anvilState) {
        float yaw = player.getYRot() + yawOffset;
        Vec3 localDirection = Vec3.directionFromRotation(new Vec2(player.getXRot(), yaw));
        GravityDirection gravity = GravityApi.getGravityDirection(player);
        Vec3 direction = GravityRotationUtil.vecPlayerToWorld(localDirection, gravity).normalize();
        Vec3 spawn = player.getEyePosition().add(direction.scale(1.1D)).subtract(0.0D, 0.5D, 0.0D);
        Vec3 velocity = direction.scale(AntarchySettings.anvilCannonLaunchVelocity()).add(GravityApi.deltaMovement(player));
        launchAnvil(level, spawn, velocity, player, anvilState);
        return direction;
    }

    private static void applyRecoil(Player player, Vec3 direction) {
        double recoil = AntarchySettings.anvilCannonRecoilStrength();
        if (recoil <= 0.0D) {
            return;
        }
        player.push(-direction.x * recoil, -direction.y * recoil * 0.5D, -direction.z * recoil);
        player.syncVelocity = true;
    }

    private static boolean hasMultishot(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                .keySet()
                .stream()
                .anyMatch(holder -> holder.is(Enchantments.MULTISHOT));
    }

    private static void spawnFireParticles(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        double originX = player.getX() + look.x * 1.2D;
        double originY = player.getEyeY() - 0.2D + look.y * 1.2D;
        double originZ = player.getZ() + look.z * 1.2D;

        level.sendParticles(ParticleTypes.EXPLOSION, originX, originY, originZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, originX, originY, originZ, 16, 0.25D, 0.2D, 0.25D, 0.03D);
        level.sendParticles(ParticleTypes.CLOUD, originX, originY - 0.05D, originZ, 10, 0.2D, 0.15D, 0.2D, 0.02D);
    }
}
