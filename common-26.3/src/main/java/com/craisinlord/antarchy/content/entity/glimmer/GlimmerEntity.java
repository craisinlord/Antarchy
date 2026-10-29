package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancements;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancementTriggers;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import com.craisinlord.antarchy.content.entity.ConfiguredMobSpawnUtil;
import com.craisinlord.antarchy.Antarchy;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GlimmerEntity extends TamableAnimal implements GeoEntity {
    private static final ResourceKey<Biome> GLIMMERING_POOLS = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "glimmering_pools")
    );
    private static final ResourceKey<Biome> OURANWOOD_FOREST = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "ouranwood_forest")
    );
    private static final ResourceKey<Biome> SPARSE_OURANWOOD_FOREST = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "sparse_ouranwood_forest")
    );
    private static final ResourceKey<Biome> FUNGAL_OURANWOOD_FOREST = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "fungal_ouranwood_forest")
    );
    private static final ResourceKey<Biome> ELYTHIA_MEADOW = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "primordial_plains")
    );
    private static final ResourceKey<Biome> ELYTHIA_RIVER = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia_river")
    );
    private static final int LUMEN_SPAWN_RADIUS = 4;
    private static final ResourceKey<Biome> PEACH_FOREST = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "peach_forest")
    );
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHEAR_COOLDOWN =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FADE_TICKS =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FOLLOWING =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ABILITY_COOLDOWN =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> GROWTH_SCALE =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> ABILITY_ANIM_TICKS =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ELKA_IDLE_SITTING =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ELKA_IDLE_QUIRK_TICKS =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ELKA_IDLE_QUIRK_VARIANT =
            SynchedEntityData.defineId(GlimmerEntity.class, EntityDataSerializers.INT);

    public static final int FADE_DURATION_TICKS = 40;
    private static final int PASSIVE_RANGE = 16;
    private static final int PASSIVE_EFFECT_REFRESH_TICKS = 60;

    private static final String VARIANT_KEY = "GlimmerVariant";
    private static final String SHEAR_COOLDOWN_KEY = "ShearCooldown";
    private static final String VARIANT_LOCKED_KEY = "VariantLocked";
    private static final String FOLLOWING_KEY = "GlimmerFollowing";
    private static final String ABILITY_COOLDOWN_KEY = "AbilityCooldown";
    private static final String ELKA_IDLE_SITTING_KEY = "ElkaIdleSitting";
    private static final String ELKA_IDLE_SIT_TICKS_KEY = "ElkaIdleSitTicks";
    private static final String ELKA_IDLE_SIT_COOLDOWN_KEY = "ElkaIdleSitCooldown";
    private static final String ELKA_IDLE_QUIRK_TICKS_KEY = "ElkaIdleQuirkTicks";
    private static final String ELKA_IDLE_QUIRK_VARIANT_KEY = "ElkaIdleQuirkVariant";

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean variantLocked;
    private float lastOwnerExhaustion = -1.0F;
    private long lastOwnerShieldLostCount = -1L;
    private int elkaIdleSitTicksRemaining;
    private int elkaIdleSitCooldownTicks = 20 * 60 * 6;
    private int elkaIdleAnimationCycleTicks;

    public GlimmerEntity(EntityType<? extends GlimmerEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setPermanentlyInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.glimmerHealth())
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
        return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.is(DamageTypes.GENERIC);
    }

    @Override
    public int getMaxHeadYRot() {
        return this.getVariant() == GlimmerVariant.FROG ? 5 : super.getMaxHeadYRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, GlimmerVariant.APPLE_COW.ordinal());
        builder.define(SHEAR_COOLDOWN, 0);
        builder.define(FADE_TICKS, 0);
        builder.define(FOLLOWING, true);
        builder.define(ABILITY_COOLDOWN, 0);
        builder.define(GROWTH_SCALE, 1.0F);
        builder.define(ABILITY_ANIM_TICKS, 0);
        builder.define(ELKA_IDLE_SITTING, false);
        builder.define(ELKA_IDLE_QUIRK_TICKS, 0);
        builder.define(ELKA_IDLE_QUIRK_VARIANT, 0);
    }

    public void playAbilityAnimation(int ticks) {
        this.entityData.set(ABILITY_ANIM_TICKS, ticks);
    }

    public int getFadeTicks() {
        return this.entityData.get(FADE_TICKS);
    }

    public float getFadeAlpha(float partialTick) {
        int ticks = this.getFadeTicks();
        if (ticks <= 0) {
            return 1.0F;
        }
        return Mth.clamp((ticks - partialTick) / (float) FADE_DURATION_TICKS, 0.0F, 1.0F);
    }

    public boolean isFollowingOwner() {
        return this.entityData.get(FOLLOWING);
    }

    public boolean isAbilityOnCooldown() {
        return this.entityData.get(ABILITY_COOLDOWN) > 0;
    }

    public void startAbilityCooldown() {
        this.entityData.set(ABILITY_COOLDOWN, this.getVariant().getBehavior().abilityCooldownTicks());
    }

    public void setAbilityCooldown(int ticks) {
        this.entityData.set(ABILITY_COOLDOWN, Math.max(0, ticks));
    }

    public float getAbilityCooldownFraction(float partialTick) {
        int max = this.getVariant().getBehavior().abilityCooldownTicks();
        if (max <= 0) {
            return 0.0F;
        }
        int ticks = this.entityData.get(ABILITY_COOLDOWN);
        return Mth.clamp((ticks - partialTick) / (float) max, 0.0F, 1.0F);
    }

    public float getLastOwnerExhaustion() {
        return this.lastOwnerExhaustion;
    }

    public void setLastOwnerExhaustion(float value) {
        this.lastOwnerExhaustion = value;
    }

    public long getLastOwnerShieldLostCount() {
        return this.lastOwnerShieldLostCount;
    }

    public void setLastOwnerShieldLostCount(long value) {
        this.lastOwnerShieldLostCount = value;
    }

    public float getGrowthScale() {
        return this.entityData.get(GROWTH_SCALE);
    }

    public void setGrowthScale(float scale) {
        this.entityData.set(GROWTH_SCALE, scale);
        this.refreshDimensions();
    }

    public boolean isElkaIdleSitting() {
        return this.entityData.get(ELKA_IDLE_SITTING);
    }

    public void setElkaIdleSitting(boolean sitting) {
        this.entityData.set(ELKA_IDLE_SITTING, sitting);
    }

    public int getElkaIdleQuirkTicks() {
        return this.entityData.get(ELKA_IDLE_QUIRK_TICKS);
    }

    public void setElkaIdleQuirkTicks(int ticks) {
        this.entityData.set(ELKA_IDLE_QUIRK_TICKS, Math.max(0, ticks));
    }

    public int getElkaIdleQuirkVariant() {
        return this.entityData.get(ELKA_IDLE_QUIRK_VARIANT);
    }

    public void setElkaIdleQuirkVariant(int variant) {
        this.entityData.set(ELKA_IDLE_QUIRK_VARIANT, Math.max(0, variant));
    }

    public int getElkaIdleSitTicksRemaining() {
        return this.elkaIdleSitTicksRemaining;
    }

    public void setElkaIdleSitTicksRemaining(int ticks) {
        this.elkaIdleSitTicksRemaining = Math.max(0, ticks);
    }

    public int getElkaIdleSitCooldownTicks() {
        return this.elkaIdleSitCooldownTicks;
    }

    public void setElkaIdleSitCooldownTicks(int ticks) {
        this.elkaIdleSitCooldownTicks = Math.max(0, ticks);
    }

    public int getElkaIdleAnimationCycleTicks() {
        return this.elkaIdleAnimationCycleTicks;
    }

    public void setElkaIdleAnimationCycleTicks(int ticks) {
        this.elkaIdleAnimationCycleTicks = Math.max(0, ticks);
    }

    private void cycleMovementState(Player player) {
        if (this.isOrderedToSit()) {
            this.setOrderedToSit(false);
            this.entityData.set(FOLLOWING, true);
            player.sendOverlayMessage(Component.translatable("entity.antarchy.glimmer.set_follow"));
        } else if (this.isFollowingOwner()) {
            this.entityData.set(FOLLOWING, false);
            player.sendOverlayMessage(Component.translatable("entity.antarchy.glimmer.set_wander"));
        } else {
            this.setOrderedToSit(true);
            this.getNavigation().stop();
            player.sendOverlayMessage(Component.translatable("entity.antarchy.glimmer.set_sit"));
        }
    }

    public GlimmerVariant getVariant() {
        int ordinal = Mth.clamp(this.entityData.get(VARIANT), 0, GlimmerVariant.values().length - 1);
        return GlimmerVariant.values()[ordinal];
    }

    public void setVariant(GlimmerVariant variant) {
        this.entityData.set(VARIANT, variant.ordinal());
    }

    public void lockVariant(GlimmerVariant variant) {
        this.setVariant(variant);
        this.variantLocked = true;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (VARIANT.equals(key)) {
            this.refreshDimensions();
            if (!this.level().isClientSide()) {
                this.goalSelector.removeAllGoals(goal -> true);
                this.targetSelector.removeAllGoals(goal -> true);
                this.registerGoals();
            }
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        GlimmerVariantBehavior behavior = this.getVariant().getBehavior();
        EntityDimensions base = this.isBaby() ? behavior.babyDimensions() : behavior.adultDimensions();
        float growth = this.getGrowthScale();
        return growth == 1.0F ? base : base.scale(growth);
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.refreshDimensions();
    }

    public boolean isShearCooldownActive() {
        return this.entityData.get(SHEAR_COOLDOWN) > 0;
    }

    public void startShearCooldown(int ticks) {
        this.entityData.set(SHEAR_COOLDOWN, ticks);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new GlimmerFollowOwnerGoal(this, this.getVariant().getBehavior().followSpeedModifier()));
        this.getVariant().getBehavior().registerGoals(this);
    }

    public void addGoal(int priority, Goal goal) {
        this.goalSelector.addGoal(priority, goal);
    }

    public void addTargetGoal(int priority, Goal goal) {
        this.targetSelector.addGoal(priority, goal);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            this.getVariant().getBehavior().onHurtTarget(this, target);
        }
        return hurt;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame() && stack.is(AntarchyObjects.SPIRIT_APPLE.get())) {
            return this.tameWithSpiritApple(player, hand, stack);
        }

        if (this.isTame() && this.isOwnedBy(player) && stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) {
            return this.captureInBottle(player, hand, stack);
        }

        if (this.isTame() && this.isOwnedBy(player) && stack.isEmpty() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                this.cycleMovementState(player);
            }
            return InteractionResult.SUCCESS;
        }

        InteractionResult variantResult = this.getVariant().getBehavior().onInteract(this, player, stack, hand);
        if (variantResult != null) {
            return variantResult;
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult tameWithSpiritApple(Player player, InteractionHand hand, ItemStack stack) {
        if (this.level().isClientSide()) {
            return InteractionResult.CONSUME;
        }

        this.usePlayerItem(player, hand, stack);
        this.tame(player);
        this.setOrderedToSit(false);
        this.level().broadcastEntityEvent(this, (byte) 7);
        GlimmerParticles.tickBurst(this);

        if (this.level() instanceof ServerLevel serverLevel) {
            GlimmerCompanionSavedData.replaceCompanion(serverLevel.getServer(), player.getUUID(), this.getUUID());
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AntarchyAdvancements.award(serverPlayer, this.getVariant().tameAdvancementId());
            AntarchyAdvancementTriggers.tamedGlimmer(serverPlayer, this.getVariant().name().toLowerCase(java.util.Locale.ROOT));
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult captureInBottle(Player player, InteractionHand hand, ItemStack stack) {
        if (this.level().isClientSide()) {
            return InteractionResult.CONSUME;
        }

        ItemStack filled = com.craisinlord.antarchy.content.item.GlimmerBottleItem.create(
                this.getVariant(), this.entityData.get(ABILITY_COOLDOWN), this.entityData.get(SHEAR_COOLDOWN));
        ItemStack result = net.minecraft.world.item.ItemUtils.createFilledResult(stack, player, filled);
        player.setItemInHand(hand, result);
        this.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);

        if (this.level() instanceof ServerLevel serverLevel) {
            GlimmerCompanionSavedData.clearCompanion(serverLevel.getServer(), player.getUUID(), this.getUUID());
        }

        this.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        com.craisinlord.antarchy.content.entity.ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.glimmerHealth());
        if (!this.variantLocked) {
            this.setVariant(selectNaturalVariant(level, this.blockPosition()));
            this.variantLocked = true;
        }
        return result;
    }

    private GlimmerVariant selectNaturalVariant(ServerLevelAccessor level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(GLIMMERING_POOLS)) {
            GlimmerVariant[] poolVariants = new GlimmerVariant[] {
                    GlimmerVariant.FROG,
                    GlimmerVariant.OURANWOOD_DEER,
                    GlimmerVariant.APPLE_COW
            };
            return poolVariants[this.random.nextInt(poolVariants.length)];
        }
        if (biome.is(OURANWOOD_FOREST) || biome.is(SPARSE_OURANWOOD_FOREST) || biome.is(FUNGAL_OURANWOOD_FOREST)) {
            GlimmerVariant[] forestVariants = new GlimmerVariant[] {
                    GlimmerVariant.FROG,
                    GlimmerVariant.OURANWOOD_DEER
            };
            return forestVariants[this.random.nextInt(forestVariants.length)];
        }
        if (biome.is(ELYTHIA_MEADOW)) {
            return GlimmerVariant.APPLE_COW;
        }
        if (biome.is(PEACH_FOREST)) {
            return GlimmerVariant.ELKA;
        }
        return GlimmerVariant.random(this.random);
    }

    public static boolean checkGlimmerSpawnRules(EntityType<GlimmerEntity> type, ServerLevelAccessor level,
                                                   EntitySpawnReason spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(ELYTHIA_RIVER)) {
            return level.getLevel().isDarkOutside() && isNearLumen(level, pos);
        }
        if (!net.minecraft.world.entity.animal.Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random)) {
            return false;
        }
        if (biome.is(GLIMMERING_POOLS)) {
            return true;
        }
        return level.getLevel().isDarkOutside();
    }

    private static boolean isNearLumen(ServerLevelAccessor level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -LUMEN_SPAWN_RADIUS; dx <= LUMEN_SPAWN_RADIUS; dx++) {
            for (int dz = -LUMEN_SPAWN_RADIUS; dz <= LUMEN_SPAWN_RADIUS; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    cursor.setWithOffset(pos, dx, dy, dz);
                    if (level.getFluidState(cursor).is(com.craisinlord.antarchy.content.AntarchyTags.Fluids.LUMEN)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        this.clampBodyToHeadRotation();
        GlimmerParticles.tickAmbient(this, this.getDeltaMovement().horizontalDistanceSqr() > 0.01D);

        if (!this.level().isClientSide()) {
            int fadeTicks = this.entityData.get(FADE_TICKS);
            if (this.isTame() || this.level().getBiome(this.blockPosition()).is(GLIMMERING_POOLS)) {
                if (fadeTicks > 0) {
                    this.entityData.set(FADE_TICKS, 0);
                }
            } else if (fadeTicks > 0 || this.level().isBrightOutside()) {
                if (fadeTicks <= 0) {
                    fadeTicks = FADE_DURATION_TICKS;
                }
                fadeTicks--;
                this.entityData.set(FADE_TICKS, fadeTicks);
                if (fadeTicks <= 0) {
                    this.discard();
                    return;
                }
            }

            int shearCooldown = this.entityData.get(SHEAR_COOLDOWN);
            if (shearCooldown > 0) {
                this.entityData.set(SHEAR_COOLDOWN, shearCooldown - 1);
            }

            this.applyGroundSupport();
            this.getVariant().getBehavior().customTick(this);
            this.tickAbilitiesAndPassive();
            this.tickGrowthDecay();
        }
    }

    private void tickAbilitiesAndPassive() {
        int cooldown = this.entityData.get(ABILITY_COOLDOWN);
        if (cooldown > 0) {
            this.entityData.set(ABILITY_COOLDOWN, cooldown - 1);
        }

        int animTicks = this.entityData.get(ABILITY_ANIM_TICKS);
        if (animTicks > 0) {
            this.entityData.set(ABILITY_ANIM_TICKS, animTicks - 1);
        }

        if (!this.isTame() || !(this.getOwner() instanceof Player owner)) {
            return;
        }

        boolean nearby = owner.level() == this.level()
                && this.distanceToSqr(owner) <= (double) (PASSIVE_RANGE * PASSIVE_RANGE);
        if (!nearby) {
            this.getVariant().getBehavior().clearPassive(this, owner);
            return;
        }

        if (this.tickCount % PASSIVE_EFFECT_REFRESH_TICKS == 0) {
            this.getVariant().getBehavior().tickPassive(this, owner);
        }
        this.getVariant().getBehavior().tickPassiveEveryTick(this, owner);

        if (cooldown <= 0) {
            this.getVariant().getBehavior().tickAbilityCheck(this, owner);
        }
    }

    private void tickGrowthDecay() {
        float scale = this.getGrowthScale();
        if (scale == 1.0F) {
            return;
        }
        double decayPerTick = this.getVariant().getBehavior().growthDecayPerTick();
        if (decayPerTick <= 0.0D) {
            return;
        }
        float next = scale > 1.0F
                ? (float) Math.max(1.0D, scale - decayPerTick)
                : (float) Math.min(1.0D, scale + decayPerTick);
        if (next != scale) {
            this.setGrowthScale(next);
        }
    }

    private void clampBodyToHeadRotation() {
        float maxYaw = this.getMaxHeadYRot();
        float delta = Mth.wrapDegrees(this.yHeadRot - this.yBodyRot);
        if (Math.abs(delta) > maxYaw) {
            this.yBodyRot = Mth.wrapDegrees(this.yHeadRot - Math.copySign(maxYaw, delta));
        }
    }

    private void applyGroundSupport() {
        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.05D, this.getZ());
        VoxelShape shape = this.level().getBlockState(below).getCollisionShape(this.level(), below);
        if (!shape.isEmpty()) {
            double top = below.getY() + shape.max(Direction.Axis.Y);
            if (this.getY() <= top + 0.2D && this.getDeltaMovement().y <= 0.0D) {
                this.setPos(this.getX(), top, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
                this.setOnGround(true);
                return;
            }
        }
        this.setOnGround(false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(VARIANT_KEY, this.getVariant().name());
        output.putInt(SHEAR_COOLDOWN_KEY, this.entityData.get(SHEAR_COOLDOWN));
        output.putBoolean(VARIANT_LOCKED_KEY, this.variantLocked);
        output.putBoolean(FOLLOWING_KEY, this.isFollowingOwner());
        output.putInt(ABILITY_COOLDOWN_KEY, this.entityData.get(ABILITY_COOLDOWN));
        output.putBoolean(ELKA_IDLE_SITTING_KEY, this.isElkaIdleSitting());
        output.putInt(ELKA_IDLE_SIT_TICKS_KEY, this.elkaIdleSitTicksRemaining);
        output.putInt(ELKA_IDLE_SIT_COOLDOWN_KEY, this.elkaIdleSitCooldownTicks);
        output.putInt(ELKA_IDLE_QUIRK_TICKS_KEY, this.getElkaIdleQuirkTicks());
        output.putInt(ELKA_IDLE_QUIRK_VARIANT_KEY, this.getElkaIdleQuirkVariant());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString(VARIANT_KEY).ifPresent(name -> this.setVariant(GlimmerVariant.byName(name)));
        this.entityData.set(SHEAR_COOLDOWN, input.getIntOr(SHEAR_COOLDOWN_KEY, 0));
        this.variantLocked = input.getBooleanOr(VARIANT_LOCKED_KEY, false);
        this.entityData.set(FOLLOWING, input.getBooleanOr(FOLLOWING_KEY, true));
        this.entityData.set(ABILITY_COOLDOWN, input.getIntOr(ABILITY_COOLDOWN_KEY, 0));
        this.entityData.set(ELKA_IDLE_SITTING, input.getBooleanOr(ELKA_IDLE_SITTING_KEY, false));
        this.elkaIdleSitTicksRemaining = input.getIntOr(ELKA_IDLE_SIT_TICKS_KEY, 0);
        this.elkaIdleSitCooldownTicks = input.getIntOr(ELKA_IDLE_SIT_COOLDOWN_KEY, 20 * 60 * 6);
        this.entityData.set(ELKA_IDLE_QUIRK_TICKS, input.getIntOr(ELKA_IDLE_QUIRK_TICKS_KEY, 0));
        this.entityData.set(ELKA_IDLE_QUIRK_VARIANT, input.getIntOr(ELKA_IDLE_QUIRK_VARIANT_KEY, 0));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getVariant().getBehavior().ambientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.getVariant().getBehavior().hurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getVariant().getBehavior().deathSound();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GlimmerEntity>("main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationTest<GlimmerEntity> state) {
        if (this.entityData.get(ABILITY_ANIM_TICKS) > 0) {
            RawAnimation abilityAnim = this.getVariant().getBehavior().abilityAnimation();
            if (abilityAnim != null) {
                return state.setAndContinue(abilityAnim);
            }
        }
        if (state.isMoving()) {
            RawAnimation movingAnim = this.getVariant().getBehavior().movingAnimation(this);
            return state.setAndContinue(movingAnim != null ? movingAnim : WALK_ANIM);
        }
        RawAnimation idleAnim = this.getVariant().getBehavior().idleAnimation(this);
        return state.setAndContinue(idleAnim != null ? idleAnim : IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
