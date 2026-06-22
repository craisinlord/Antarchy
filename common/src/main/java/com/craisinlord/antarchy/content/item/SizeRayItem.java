package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class SizeRayItem extends ProjectileWeaponItem implements GeoItem {
    private static final String FIRE_CONTROLLER = "fire_controller";
    private static final int CHARGE_TICKS_PER_LEVEL = 5;
    private static final int CHARGE_SOUND_INTERVAL_TICKS = 12;
    private static final int MAX_USE_DURATION = 72000;
    private static final Predicate<ItemStack> NO_PROJECTILES = stack -> false;
    private final Supplier<? extends EntityType<? extends SizeRayProjectileEntity>> projectileType;
    private final SizeRayProjectileEntity.SizeRayType rayType;
    private final ResourceLocation geoModelLocation;
    private final ResourceLocation geoTextureLocation;
    private final ResourceLocation geoAnimationLocation;
    private final String fireAnimationName;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public SizeRayItem(
            Item.Properties properties,
            Supplier<? extends EntityType<? extends SizeRayProjectileEntity>> projectileType,
            SizeRayProjectileEntity.SizeRayType rayType,
            ResourceLocation geoModelLocation,
            ResourceLocation geoTextureLocation,
            ResourceLocation geoAnimationLocation,
            String fireAnimationName
    ) {
        super(properties);
        this.projectileType = projectileType;
        this.rayType = rayType;
        this.geoModelLocation = geoModelLocation;
        this.geoTextureLocation = geoTextureLocation;
        this.geoAnimationLocation = geoAnimationLocation;
        this.fireAnimationName = fireAnimationName;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!AntarchySettings.sizeChangingRaysEnabled()) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemStack);
        }

        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        if (!AntarchySettings.sizeChangingRaysEnabled() || player.getCooldowns().isOnCooldown(this)) {
            return;
        }

        int useTicks = this.getUseDuration(stack, livingEntity) - timeLeft;
        int chargeLevel = getChargeLevel(useTicks);

        if (!level.isClientSide()) {
            SizeRayProjectileEntity projectile = this.createProjectile(level, player, stack, chargeLevel);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
            level.addFreshEntity(projectile);
            stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND
                    : EquipmentSlot.OFFHAND);
            triggerFireAnimation((ServerLevel) level, player, stack);
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    this.getFireSound(),
                    SoundSource.PLAYERS,
                    1.0F,
                    0.95F + 0.12F * (chargeLevel - 1) + 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F)
            );
            player.awardStat(Stats.ITEM_USED.get(this));
            double cooldownSeconds = AntarchySettings.sizeRayCooldownSeconds();
            if (cooldownSeconds > 0) {
                player.getCooldowns().addCooldown(this, (int) (cooldownSeconds * 20));
            }
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (level.isClientSide() || !AntarchySettings.sizeChangingRaysEnabled()) {
            return;
        }

        int useTicks = this.getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (useTicks == 1 || useTicks % CHARGE_SOUND_INTERVAL_TICKS == 0) {
            level.playSound(
                    null,
                    livingEntity.getX(),
                    livingEntity.getY(),
                    livingEntity.getZ(),
                    AntarchySoundEvents.SIZE_RAY_CHARGE.get(),
                    SoundSource.PLAYERS,
                    0.15F,
                    0.95F + level.getRandom().nextFloat() * 0.08F
            );
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return MAX_USE_DURATION;
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

    public SizeRayProjectileEntity createProjectile(Level level, LivingEntity livingEntity, ItemStack weaponStack) {
        return this.createProjectile(level, livingEntity, weaponStack, 1);
    }

    public SizeRayProjectileEntity createProjectile(Level level, LivingEntity livingEntity, ItemStack weaponStack, int chargeLevel) {
        SizeRayProjectileEntity projectile = new SizeRayProjectileEntity(this.projectileType.get(), livingEntity, level, weaponStack.copy(), this.rayType);
        projectile.setChargeLevel(chargeLevel);
        return projectile;
    }

    public SizeRayProjectileEntity createProjectile(Level level, ItemStack weaponStack) {
        SizeRayProjectileEntity projectile = new SizeRayProjectileEntity(this.projectileType.get(), level, this.rayType);
        projectile.setChargeLevel(1);
        return projectile;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String tooltipKey = this.rayType == SizeRayProjectileEntity.SizeRayType.SHRINK
                ? "tooltip.antarchy.shrink_ray"
                : "tooltip.antarchy.growth_ray";
        tooltipComponents.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, FIRE_CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim(this.fireAnimationName, RawAnimation.begin().then(this.fireAnimationName, Animation.LoopType.PLAY_ONCE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<SizeRayItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(geoModelLocation, geoTextureLocation, geoAnimationLocation));
                }

                return this.renderer;
            }
        });
    }

    private void triggerFireAnimation(ServerLevel level, LivingEntity livingEntity, ItemStack stack) {
        long animatableId = GeoItem.getOrAssignId(stack, level);
        triggerAnim(livingEntity, animatableId, FIRE_CONTROLLER, this.fireAnimationName);
    }

    private SoundEvent getFireSound() {
        return this.rayType == SizeRayProjectileEntity.SizeRayType.SHRINK
                ? AntarchySoundEvents.SHRINK_RAY_SOUND.get()
                : AntarchySoundEvents.GROWTH_RAY_SOUND.get();
    }

    private static int getChargeLevel(int useTicks) {
        return Math.max(1, 1 + useTicks / CHARGE_TICKS_PER_LEVEL);
    }
}
