package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WaterCannonItem extends ProjectileWeaponItem implements GeoItem {

    private static final String FIRE_CONTROLLER = "fire_controller";
    private static final String FIRE_ANIMATION = "water_cannon_fire";
    private static final Predicate<ItemStack> NO_PROJECTILES = stack -> false;
    private static final int FIRE_INTERVAL_TICKS = 8;
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/water_cannon.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/models/item/water_cannon.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/water_cannon.animation.json");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public WaterCannonItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
        if (!(livingEntity instanceof Player player)) return;
        int usedTicks = getUseDuration(stack, livingEntity) - remainingUseTicks;
        if (usedTicks % FIRE_INTERVAL_TICKS != 0) return;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                AntarchySoundEvents.SQUIDZOOKA_FIRE.get(), SoundSource.PLAYERS,
                0.8F, 0.9F + level.getRandom().nextFloat() * 0.2F);

        if (!level.isClientSide()) {
            WaterBombEntity bomb = new WaterBombEntity(level, player);
            bomb.moveTo(player.getX(), player.getEyeY() - 0.1D, player.getZ(), player.getYRot(), player.getXRot());
            bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(bomb);
            stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            if (level instanceof ServerLevel serverLevel) {
                triggerFireAnimation(serverLevel, player, stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
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
            private AnimatedHeldItemRenderer<WaterCannonItem> renderer;

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
}
