package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EasterBunnyEntity extends Rabbit implements GeoEntity {
    private static final int MIN_LAY_COOLDOWN = 20 * 45;
    private static final int MAX_LAY_COOLDOWN = 20 * 120;
    private static final int LAY_ANIMATION_TICKS = 40;
    private static final String MAIN_CONTROLLER = "main_controller";
    private static final String LAY_CONTROLLER = "lay_controller";
    private static final String LAY_TRIGGER = "lay";
    private static final String LAY_COOLDOWN_KEY = "LayCooldown";
    private static final String LAY_ANIM_TICKS_KEY = "LayAnimTicks";
    private static final EntityDataAccessor<Integer> LAY_ANIM_TICKS =
            SynchedEntityData.defineId(EasterBunnyEntity.class, EntityDataSerializers.INT);
    private static final List<Item> SPAWN_EGG_ITEMS = BuiltInRegistries.ITEM.stream()
            .filter(SpawnEggItem.class::isInstance)
            .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()))
            .toList();
    private int layCooldown = this.createLayCooldown();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HOP_ANIM = RawAnimation.begin().thenLoop("hop");
    public EasterBunnyEntity(EntityType<? extends Rabbit> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAY_ANIM_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide || !this.isAlive()) {
            return;
        }

        if (!AntarchySettings.easterBunnyEnabled() || this.isBaby()) {
            return;
        }

        if (this.getLayAnimationTicks() > 0) {
            this.setLayAnimationTicks(this.getLayAnimationTicks() - 1);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            if (this.getLayAnimationTicks() == 0) {
                this.layRandomSpawnEgg();
                this.layCooldown = this.createLayCooldown();
            }
        } else if (--this.layCooldown <= 0 && this.onGround()) {
            this.startLayingEgg();
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CARROT) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.DANDELION);
    }

    @Override
    public Rabbit getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return AntarchyObjects.EASTER_BUNNY.get().create(level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(LAY_COOLDOWN_KEY, this.layCooldown);
        tag.putInt(LAY_ANIM_TICKS_KEY, this.getLayAnimationTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.layCooldown = tag.contains(LAY_COOLDOWN_KEY) ? tag.getInt(LAY_COOLDOWN_KEY) : this.createLayCooldown();
        this.setLayAnimationTicks(Math.max(0, tag.getInt(LAY_ANIM_TICKS_KEY)));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, MAIN_CONTROLLER, 0, this::mainAnimController));
        controllers.add(new AnimationController<>(this, LAY_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(LAY_TRIGGER, RawAnimation.begin().thenPlay("lay")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private PlayState mainAnimController(AnimationState<EasterBunnyEntity> state) {
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            state.getController().setAnimationSpeed(1.35D);
            return state.setAndContinue(HOP_ANIM);
        }

        state.getController().setAnimationSpeed(1.0D);
        return state.setAndContinue(IDLE_ANIM);
    }

    private int getLayAnimationTicks() {
        return this.entityData.get(LAY_ANIM_TICKS);
    }

    private void setLayAnimationTicks(int ticks) {
        this.entityData.set(LAY_ANIM_TICKS, Math.max(0, ticks));
    }

    private void startLayingEgg() {
        this.setLayAnimationTicks(LAY_ANIMATION_TICKS);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.triggerAnim(LAY_CONTROLLER, LAY_TRIGGER);
    }

    private void layRandomSpawnEgg() {
        Item item = this.getRandomSpawnEgg();
        if (item == Items.AIR) {
            return;
        }

        this.spawnAtLocation(new ItemStack(item), 0.2F);
        this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    private Item getRandomSpawnEgg() {
        HolderGetter<Item> items = this.level().registryAccess().lookupOrThrow(Registries.ITEM);
        List<Item> candidates = SPAWN_EGG_ITEMS.stream()
                .filter(item -> !this.isBlacklistedSpawnEgg(items, item))
                .toList();

        return candidates.isEmpty() ? Items.AIR : candidates.get(this.random.nextInt(candidates.size()));
    }

    private boolean isBlacklistedSpawnEgg(HolderGetter<Item> items, Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item)
                .map(items::getOrThrow)
                .map(holder -> holder.is(AntarchyTags.Items.EASTER_BUNNY_SPAWN_EGG_BLACKLIST))
                .orElse(false);
    }

    private int createLayCooldown() {
        return MIN_LAY_COOLDOWN + this.random.nextInt(MAX_LAY_COOLDOWN - MIN_LAY_COOLDOWN + 1);
    }
}
