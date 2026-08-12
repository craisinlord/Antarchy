package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import com.craisinlord.antarchy.content.client.AntarchyGeoItemRenderer;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RpoLauncherItem extends ProjectileWeaponItem implements GeoItem, com.craisinlord.antarchy.content.client.AntarchyGeoItem {
    private static final String FIRE_CONTROLLER = "fire_controller";
    private static final String FIRE_ANIMATION = "rpo_launcher_recoil";
    private static final Predicate<ItemStack> NO_PROJECTILES = stack -> false;
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation("antarchy", "geo/rpo_launcher.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("antarchy", "textures/models/item/rpo_launcher.png");
    private static final ResourceLocation ANIMATION_LOCATION = new ResourceLocation("antarchy", "animations/rpo_launcher.animation.json");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RpoLauncherItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!level.isClientSide()) {
            fireOctopusBomb(level, player, 0.0F);
            if (hasMultishot(itemStack)) {
                fireOctopusBomb(level, player, -10.0F);
                fireOctopusBomb(level, player, 10.0F);
            }
            spawnFireParticles((ServerLevel) level, player);
            itemStack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            triggerFireAnimation((ServerLevel) level, player, itemStack);
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                AntarchySoundEvents.SQUIDZOOKA_FIRE.get(),
                SoundSource.PLAYERS,
                1.15F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F
        );
        player.awardStat(Stats.ITEM_USED.get(this));
        double cooldownSeconds = AntarchySettings.rpoLauncherCooldownSeconds();
        if (cooldownSeconds > 0) {
            player.getCooldowns().addCooldown(this, (int) (cooldownSeconds * 20));
        }
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return NO_PROJECTILES;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return NO_PROJECTILES;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, FIRE_CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim(FIRE_ANIMATION, RawAnimation.begin().thenPlay(FIRE_ANIMATION)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new AntarchyGeoItemRenderer() {
            private AnimatedHeldItemRenderer<RpoLauncherItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }

    private final java.util.function.Supplier<Object> renderProvider = com.google.common.base.Suppliers.memoize(() -> {
        Object[] holder = new Object[1];
        createRenderer(o -> holder[0] = o);
        return holder[0];
    });

    @Override
    public java.util.function.Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    private void triggerFireAnimation(ServerLevel level, LivingEntity livingEntity, ItemStack stack) {
        long animatableId = GeoItem.getOrAssignId(stack, level);
        triggerAnim(livingEntity, animatableId, FIRE_CONTROLLER, FIRE_ANIMATION);
    }

    private static void fireOctopusBomb(Level level, Player player, float yawOffset) {
        OctopusBombEntity projectileBomb = AntarchyObjects.OCTOPUS_BOMB.get().create(level);
        if (projectileBomb == null) {
            return;
        }

        float yaw = player.getYRot() + yawOffset;
        Vec3 direction = Vec3.directionFromRotation(new Vec2(player.getXRot(), yaw));
        projectileBomb.moveTo(
                player.getX(),
                player.getEyeY() + 0.15D,
                player.getZ(),
                yaw,
                player.getXRot()
        );
        projectileBomb.launchAsExplosiveProjectile(player, direction.scale(AntarchySettings.rpoLauncherLaunchVelocity()).add(player.getDeltaMovement()));
        level.addFreshEntity(projectileBomb);
    }

    private static boolean hasMultishot(ItemStack stack) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0;
    }

    private static void spawnFireParticles(ServerLevel level, Player player) {
        double lookX = player.getLookAngle().x;
        double lookY = player.getLookAngle().y;
        double lookZ = player.getLookAngle().z;
        double originX = player.getX() + lookX * 0.65D;
        double originY = player.getEyeY() - 0.15D + lookY * 0.15D;
        double originZ = player.getZ() + lookZ * 0.65D;

        level.sendParticles(ParticleTypes.SMOKE, originX, originY, originZ, 12, 0.18D, 0.12D, 0.18D, 0.02D);
        level.sendParticles(ParticleTypes.CLOUD, originX, originY - 0.05D, originZ, 8, 0.16D, 0.1D, 0.16D, 0.01D);
    }
}
