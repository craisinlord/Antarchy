package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
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
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SquidzookaItem extends ProjectileWeaponItem implements GeoItem {
    private static final String FIRE_CONTROLLER = "fire_controller";
    private static final String FIRE_ANIMATION = "squidzooka_recoil";
    private static final Predicate<ItemStack> NO_PROJECTILES = stack -> false;
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/squidzooka.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/models/item/squidzooka.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/squidzooka.animation.json");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public SquidzookaItem(Properties properties) {
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
            MissileSquidEntity projectileSquid = AntarchyObjects.MISSILE_SQUID.get().create(level);
            if (projectileSquid != null) {
                projectileSquid.moveTo(
                        player.getX(),
                        player.getEyeY() - 0.2D,
                        player.getZ(),
                        player.getYRot(),
                        player.getXRot()
                );
                projectileSquid.launchFromSquidzooka(player, (float) AntarchySettings.squidzookaLaunchVelocity());
                level.addFreshEntity(projectileSquid);
            }
            spawnFireParticles((ServerLevel) level, player);
            itemStack.hurtAndBreak(1, player, usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
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
        double cooldownSeconds = AntarchySettings.squidzookaCooldownSeconds();
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
    protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int i, float velocity, float inaccuracy, float angle, LivingEntity target) {
        projectile.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), angle, velocity, inaccuracy);
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

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<SquidzookaItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
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

    private static void spawnFireParticles(ServerLevel level, Player player) {
        double lookX = player.getLookAngle().x;
        double lookY = player.getLookAngle().y;
        double lookZ = player.getLookAngle().z;
        double originX = player.getX() + lookX * 0.65D;
        double originY = player.getEyeY() - 0.15D + lookY * 0.15D;
        double originZ = player.getZ() + lookZ * 0.65D;

        level.sendParticles(ParticleTypes.SPLASH, originX, originY, originZ, 12, 0.18D, 0.12D, 0.18D, 0.02D);
        level.sendParticles(ParticleTypes.BUBBLE, originX, originY - 0.05D, originZ, 8, 0.16D, 0.1D, 0.16D, 0.01D);
    }
}
