package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DorrieEntity extends Animal implements GeoEntity {
    private static final ResourceLocation CHEEP_ITEM_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "cheep");
    private static final String INVENTORY_TAG = "Inventory";
    private static final int SADDLE_SLOT = 0;
    private static final int INVENTORY_SIZE = 17;

    private static final EntityDataAccessor<Boolean> HAS_SADDLE =
            SynchedEntityData.defineId(DorrieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TAMED =
            SynchedEntityData.defineId(DorrieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> JUMP_CHARGE =
            SynchedEntityData.defineId(DorrieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUIRK_TICKS =
            SynchedEntityData.defineId(DorrieEntity.class, EntityDataSerializers.INT);

    private static final int MAX_CHARGE_TICKS = 22;
    private static final float WATER_RIDE_SPEED = 0.42F;
    private static final float LAND_BEACH_SPEED = 0.07F;
    private static final double ASCEND_SPEED = 0.06D;
    private static final int QUIRK_DURATION_TICKS = 65;
    private static final int MIN_QUIRK_COOLDOWN_TICKS = 260;
    private static final int MAX_QUIRK_COOLDOWN_TICKS = 420;
    private static final double MOVING_ANIM_THRESHOLD_SQ = 0.0025D;

    private static final RawAnimation IDLE_GROUND_ANIM = RawAnimation.begin().thenLoop("idle_ground");
    private static final RawAnimation IDLE_WATER_ANIM = RawAnimation.begin().thenLoop("idle_water");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation JUMP_START_ANIM = RawAnimation.begin().thenPlay("jump_start");
    private static final RawAnimation JUMP_LOOP_ANIM = RawAnimation.begin().thenLoop("jump_idle");
    private static final RawAnimation JUMP_LAND_ANIM = RawAnimation.begin().thenPlay("jump_end");
    private static final RawAnimation QUIRK_ANIM = RawAnimation.begin().thenPlay("quirk");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final MoveControl landMoveControl;
    private final MoveControl waterMoveControl;
    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            DorrieEntity.this.onInventoryChanged();
        }
    };

    private static final int RIDES_TO_TAME = 4;
    private static final int BUCK_DELAY_TICKS = 60;

    private int chargeTicks = 0;
    private boolean isCharging = false;
    private boolean isLeaping = false;
    private boolean wasInAir = false;
    private int landAnimTicks = 0;
    private int rideAttempts = 0;
    private int buckTimer = 0;
    private int swimForwardTicks = 0;
    private int quirkCooldownTicks = MIN_QUIRK_COOLDOWN_TICKS;
    private boolean usingLandNavigation = true;
    private boolean pressingJump = false;

    public DorrieEntity(EntityType<? extends DorrieEntity> entityType, Level level) {
        super(entityType, level);
        this.landMoveControl = new MoveControl(this);
        this.waterMoveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 1.0F, true);
        this.moveControl = this.landMoveControl;
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_SADDLE, false);
        builder.define(TAMED, false);
        builder.define(JUMP_CHARGE, 0);
        builder.define(QUIRK_TICKS, 0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return this.createLandNavigation(level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new HuntCheepGoal(this));
        this.goalSelector.addGoal(2, new DolphinSwimGoal());
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.45D, 4));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D, 10));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.dorrieHealth())
                .add(Attributes.MOVEMENT_SPEED, 1.15D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnReason, net.minecraft.world.entity.SpawnGroupData spawnData) {
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.dorrieHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    public boolean hasSaddle() {
        return this.entityData.get(HAS_SADDLE);
    }

    public void setSaddle(boolean saddle) {
        this.entityData.set(HAS_SADDLE, saddle);
    }

    public SimpleContainer inventory() {
        return this.inventory;
    }

    public boolean isTamed() {
        return this.entityData.get(TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(TAMED, tamed);
    }

    public int getJumpCharge() {
        return this.entityData.get(JUMP_CHARGE);
    }

    private void setJumpCharge(int charge) {
        this.entityData.set(JUMP_CHARGE, charge);
    }

    public int getQuirkTicks() {
        return this.entityData.get(QUIRK_TICKS);
    }

    private void setQuirkTicks(int ticks) {
        boolean wasIdle = this.getQuirkTicks() <= 0;
        this.entityData.set(QUIRK_TICKS, ticks);
        if (wasIdle && ticks > 0) {
            this.triggerAnim("main_controller", "quirk");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Tamed", this.isTamed());
        tag.putInt("RideAttempts", this.rideAttempts);
        ListTag inventoryTag = new ListTag();
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag stackTag = new CompoundTag();
            stackTag.putByte("Slot", (byte) slot);
            stackTag.put("Item", stack.save(this.registryAccess()));
            inventoryTag.add(stackTag);
        }
        tag.put(INVENTORY_TAG, inventoryTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTamed(tag.getBoolean("Tamed"));
        this.rideAttempts = tag.getInt("RideAttempts");
        this.inventory.clearContent();
        ListTag inventoryTag = tag.getList(INVENTORY_TAG, 10);
        for (int i = 0; i < inventoryTag.size(); i++) {
            CompoundTag stackTag = inventoryTag.getCompound(i);
            int slot = stackTag.getByte("Slot") & 255;
            if (slot < 0 || slot >= this.inventory.getContainerSize()) {
                continue;
            }
            this.inventory.setItem(slot, ItemStack.parseOptional(this.registryAccess(), stackTag.getCompound("Item")));
        }
        this.syncSaddleState();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTamed()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (player.isSecondaryUseActive()) {
            this.openInventory(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.hasSaddle() && stack.isEmpty() && !player.isPassengerOfSameVehicle(this)) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!stack.isEmpty() && this.isFood(stack)) {
            return super.mobInteract(player, hand);
        }

        this.openInventory(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (!this.isTamed() || !this.hasSaddle()) {
            return null;
        }
        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float forward = player.zza;
        float strafe = player.xxa * 0.5F;
        float pitch = Mth.clamp(player.getXRot(), -60.0F, 60.0F);
        double vertical = 0.0D;
        if (player.zza > 0.02F) {
            vertical = -pitch / 45.0D;
        } else if (Math.abs(pitch) > 10.0F) {
            vertical = -pitch / 60.0D;
        }
        return new Vec3(strafe, Mth.clamp(vertical, -1.0D, 1.0D), forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        float speed = (this.isInWater() || this.isInLava())
                ? WATER_RIDE_SPEED * (1.0F + Math.min(this.swimForwardTicks, 60) * 0.0125F)
                : LAND_BEACH_SPEED;
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * speed;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateNavigationMode();
        this.setNoGravity(false);

        if (this.getControllingPassenger() instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            if (this.getQuirkTicks() > 0) {
                this.setQuirkTicks(0);
            }
            if (this.isInWater() || this.isInLava()) {
                if (player.zza > 0.05F) {
                    this.swimForwardTicks = Math.min(this.swimForwardTicks + 1, 60);
                } else {
                    this.swimForwardTicks = Math.max(this.swimForwardTicks - 2, 0);
                }
            } else {
                this.swimForwardTicks = 0;
            }
        } else {
            this.swimForwardTicks = 0;
        }

        if (this.isVehicle() && this.getQuirkTicks() > 0) {
            this.setQuirkTicks(0);
        }

        if (!this.level().isClientSide && !this.isTamed() && this.getFirstPassenger() instanceof Player rider) {
            buckTimer++;
            if (buckTimer >= BUCK_DELAY_TICKS) {
                rider.stopRiding();
                buckTimer = 0;
                rideAttempts++;
                if (rideAttempts >= RIDES_TO_TAME) {
                    this.setTamed(true);
                    this.playSound(SoundEvents.DOLPHIN_JUMP, 0.8F, 1.0F);
                } else {
                    this.playSound(SoundEvents.DOLPHIN_HURT, 0.8F, 1.0F);
                }
            }
        } else if (!this.isTamed() && this.getFirstPassenger() == null) {
            buckTimer = 0;
        }

        if (!this.level().isClientSide && this.getControllingPassenger() instanceof Player && isCharging) {
            chargeTicks = Math.min(chargeTicks + 1, MAX_CHARGE_TICKS);
            setJumpCharge((int) ((chargeTicks / (float) MAX_CHARGE_TICKS) * 100));
        }

        if (!this.level().isClientSide) {
            this.tickQuirkAnimation();
        }

        if (landAnimTicks > 0) {
            landAnimTicks--;
        }

        boolean inAirNow = !this.onGround() && !this.isInWater() && !this.isInLava();
        if (wasInAir && !inAirNow && isLeaping) {
            isLeaping = false;
            landAnimTicks = 12;
            if (!this.level().isClientSide) {
                this.triggerAnim("main_controller", "land");
            }
            if (this.isInWater()) {
                this.playSound(SoundEvents.DOLPHIN_SPLASH, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }
        }
        wasInAir = inAirNow;
    }

    public void setPressingJump(boolean pressing) {
        this.pressingJump = pressing;
    }

    public void startJumpCharge() {
        if (!this.level().isClientSide) {
            isCharging = true;
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.isInWater() || this.isInLava()) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.getControllingPassenger() instanceof Player rider && this.isVehicle()) {
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();

            Vec3 riddenInput = this.getRiddenInput(rider, travelVector);
            this.setSpeed(this.getRiddenSpeed(rider));

            if (!this.isInWater() && !this.isInLava() && !this.onGround()) {
                Vec3 mov = this.getDeltaMovement();
                this.setDeltaMovement(mov.x * 0.91D, mov.y - 0.08D, mov.z * 0.91D);
                this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
                return;
            }

            if (this.isInWater() || this.isInLava()) {
                this.moveRelative(this.getSpeed(), new Vec3(riddenInput.x, 0.0D, riddenInput.z));
                Vec3 motion = this.getDeltaMovement();
                double vertical = 0.0D;
                if (this.isUnderWater()) {
                    vertical = riddenInput.y * 0.025D;
                } else if (riddenInput.y < 0.0D) {
                    vertical = riddenInput.y * 0.02D;
                }
                if (this.pressingJump) {
                    vertical += ASCEND_SPEED;
                }
                this.setDeltaMovement(
                        motion.x * 0.92D,
                        motion.y * 0.90D + vertical,
                        motion.z * 0.92D
                );
                this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
                return;
            }
        }

        super.travel(travelVector);
    }

    public void applyJumpImpulseClient() {
        float power = this.getJumpCharge() / 100f;
        if (power > 0.05f) {
            Vec3 look = this.getLookAngle();
            Vec3 flatLook = new Vec3(look.x, 0.0D, look.z).normalize();
            Vec3 current = this.getDeltaMovement();
            double forwardAdd = 0.15D + power * 0.45D;
            double vertical = 0.55D + power * 0.80D;
            this.setDeltaMovement(
                    current.x + flatLook.x * forwardAdd,
                    vertical,
                    current.z + flatLook.z * forwardAdd
            );
        }
    }

    public void releaseJump() {
        if (!this.level().isClientSide && isCharging) {
            float power = chargeTicks / (float) MAX_CHARGE_TICKS;
            if (power > 0.05F) {
                Vec3 look = this.getLookAngle();
                Vec3 flatLook = new Vec3(look.x, 0.0D, look.z).normalize();
                Vec3 current = this.getDeltaMovement();
                double forwardAdd = 0.15D + power * 0.45D;
                double vertical = 0.55D + power * 0.80D;
                this.setDeltaMovement(
                        current.x + flatLook.x * forwardAdd,
                        vertical,
                        current.z + flatLook.z * forwardAdd
                );
                this.hasImpulse = true;
                isLeaping = true;
                this.triggerAnim("main_controller", "jump_start");
            }
            isCharging = false;
            chargeTicks = 0;
            setJumpCharge(0);
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            Vec3 forward = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            moveFunction.accept(passenger,
                    this.getX() - forward.x * 0.65D,
                    this.getY() + 0.5D,
                    this.getZ() - forward.z * 0.65D);
        }
    }

    public double getPassengersRidingOffset() {
        return 0.6D;
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.level().isClientSide) {
            return;
        }
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
            }
        }
    }

    private void onInventoryChanged() {
        this.syncSaddleState();
    }

    private void syncSaddleState() {
        ItemStack saddleStack = this.inventory.getItem(SADDLE_SLOT);
        this.setSaddle(!saddleStack.isEmpty() && saddleStack.is(Items.SADDLE));
    }

    private void openInventory(Player player) {
        if (this.level().isClientSide) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, ignored) -> new com.craisinlord.antarchy.content.menu.DorrieInventoryMenu(containerId, playerInventory, this.inventory, this),
                this.getDisplayName()
        ));
    }

    @Override
    @Nullable
    public DorrieEntity getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }

    @Override
    public float getSpeed() {
        float base = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        return this.isInWater() ? base * 0.15F : base * 0.04F;
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.DOLPHIN_SWIM;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController)
                .triggerableAnim("land", JUMP_LAND_ANIM)
                .triggerableAnim("jump_start", JUMP_START_ANIM)
                .triggerableAnim("quirk", QUIRK_ANIM));
    }

    private PlayState mainAnimController(AnimationState<DorrieEntity> state) {
        state.getController().setAnimationSpeed(1.0D);

        if (landAnimTicks > 0) {
            return state.setAndContinue(JUMP_LAND_ANIM);
        }
        if (isLeaping) {
            return state.setAndContinue(wasInAir ? JUMP_LOOP_ANIM : JUMP_START_ANIM);
        }
        if (this.getQuirkTicks() > 0) {
            return state.setAndContinue(QUIRK_ANIM);
        }

        boolean moving = this.isMovingForAnimation(state);
        if (this.onGround()) {
            if (moving) {
                state.getController().setAnimationSpeed(0.75D);
                return state.setAndContinue(WALK_ANIM);
            }
            return state.setAndContinue(IDLE_GROUND_ANIM);
        }

        if (this.isInWater() || this.isInLava()) {
            return state.setAndContinue(moving ? SWIM_ANIM : IDLE_WATER_ANIM);
        }
        return state.setAndContinue(moving ? SWIM_ANIM : IDLE_GROUND_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.isCheepItem(stack);
    }

    private boolean isCheepItem(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.getOptional(CHEEP_ITEM_ID).orElse(Items.AIR));
    }

    private PathNavigation createLandNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(true);
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    private PathNavigation createWaterNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    private void updateNavigationMode() {
        boolean shouldUseLandNavigation = !this.isInWater() && !this.isUnderWater() && !this.isInLava();
        if (this.navigation == null || this.usingLandNavigation != shouldUseLandNavigation) {
            this.usingLandNavigation = shouldUseLandNavigation;
            if (this.navigation != null) {
                this.navigation.stop();
            }
            this.navigation = shouldUseLandNavigation
                    ? this.createLandNavigation(this.level())
                    : this.createWaterNavigation(this.level());
            this.moveControl = shouldUseLandNavigation ? this.landMoveControl : this.waterMoveControl;
        }
    }

    private void tickQuirkAnimation() {
        if (this.getQuirkTicks() > 0) {
            if (this.canUseQuirkAnimation()) {
                this.setQuirkTicks(this.getQuirkTicks() - 1);
            } else {
                this.setQuirkTicks(0);
                this.quirkCooldownTicks = this.nextQuirkCooldown();
            }
            return;
        }

        if (this.quirkCooldownTicks > 0) {
            this.quirkCooldownTicks--;
            return;
        }

        if (this.canUseQuirkAnimation()) {
            this.setQuirkTicks(QUIRK_DURATION_TICKS);
            this.quirkCooldownTicks = this.nextQuirkCooldown();
        }
    }

    private int nextQuirkCooldown() {
        return MIN_QUIRK_COOLDOWN_TICKS + this.random.nextInt(MAX_QUIRK_COOLDOWN_TICKS - MIN_QUIRK_COOLDOWN_TICKS + 1);
    }

    private boolean canUseQuirkAnimation() {
        return this.isInWater()
                && !this.isUnderWater()
                && !this.isVehicle()
                && !this.isLeaping
                && this.landAnimTicks <= 0
                && !this.isCharging
                && !this.isMovingForAnimation(null);
    }

    @Nullable
    private Vec3 findWaterSurfaceTarget(double radius, int minYDelta, int maxYDelta) {
        int baseY = Mth.floor(this.getY());
        for (int attempt = 0; attempt < 12; attempt++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * radius * 2.0D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * radius * 2.0D;
            int x = Mth.floor(this.getX() + offsetX);
            int z = Mth.floor(this.getZ() + offsetZ);
            Vec3 target = this.findWaterSurfaceTarget(x, z, baseY + minYDelta, baseY + maxYDelta);
            if (target != null) {
                return target;
            }
        }
        return this.findWaterSurfaceTarget(Mth.floor(this.getX()), Mth.floor(this.getZ()),
                baseY + minYDelta, baseY + maxYDelta);
    }

    @Nullable
    private Vec3 findWaterSurfaceTarget(int x, int z, int minY, int maxY) {
        int top = Math.min(maxY, this.level().getMaxBuildHeight() - 2);
        int bottom = Math.max(minY, this.level().getMinBuildHeight() + 1);
        for (int y = top; y >= bottom; y--) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!this.isWaterAt(pos) || this.isWaterAt(pos.above())) {
                continue;
            }
            return new Vec3(x + 0.5D, y + 0.85D, z + 0.5D);
        }
        return null;
    }

    @Nullable
    private Vec3 findSubmergedTarget(double radius, int minYDelta, int maxYDelta) {
        int baseY = Mth.floor(this.getY());
        for (int attempt = 0; attempt < 16; attempt++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * radius * 2.0D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * radius * 2.0D;
            int x = Mth.floor(this.getX() + offsetX);
            int z = Mth.floor(this.getZ() + offsetZ);
            Vec3 target = this.findSubmergedTarget(x, z, baseY + minYDelta, baseY + maxYDelta);
            if (target != null) {
                return target;
            }
        }
        return this.findSubmergedTarget(Mth.floor(this.getX()), Mth.floor(this.getZ()),
                baseY + minYDelta, baseY + maxYDelta);
    }

    @Nullable
    private Vec3 findSubmergedTarget(int x, int z, int minY, int maxY) {
        int top = Math.min(maxY, this.level().getMaxBuildHeight() - 2);
        int bottom = Math.max(minY, this.level().getMinBuildHeight() + 1);
        for (int y = top; y >= bottom; y--) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!this.isWaterAt(pos) || !this.isWaterAt(pos.below())) {
                continue;
            }
            return new Vec3(x + 0.5D, y + 0.3D, z + 0.5D);
        }
        return null;
    }

    private boolean isWaterAt(BlockPos pos) {
        return this.level().getFluidState(pos).is(FluidTags.WATER);
    }

    private boolean isMovingForAnimation(@Nullable AnimationState<DorrieEntity> state) {
        if (state != null && state.isMoving()) {
            return true;
        }
        if (this.getControllingPassenger() instanceof Player player) {
            return Math.abs(player.zza) > 0.05F || Math.abs(player.xxa) > 0.05F;
        }
        return this.getDeltaMovement().horizontalDistanceSqr() > MOVING_ANIM_THRESHOLD_SQ
                || this.getNavigation().isInProgress();
    }

    private class DolphinSwimGoal extends Goal {
        private static final int COOLDOWN_MIN_TICKS = 120;
        private static final int COOLDOWN_MAX_TICKS = 180;
        private static final int DIVE_MIN_TICKS = 50;
        private static final int DIVE_MAX_TICKS = 110;
        private static final int ROAM_MIN_TICKS = 70;
        private static final int ROAM_MAX_TICKS = 140;
        private static final int TARGET_REFRESH_TICKS = 20;
        private static final double AQUATIC_SPEED = 1.15D;
        private static final double SEARCH_RADIUS = 12.0D;

        @Nullable
        private Vec3 targetPos;
        private int phaseTicks;
        private int targetRefreshTicks;
        private int cooldownTicks;
        private Phase phase = Phase.ROAM;

        DolphinSwimGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (DorrieEntity.this.level().isClientSide
                    || DorrieEntity.this.isVehicle()
                    || DorrieEntity.this.getControllingPassenger() != null
                    || DorrieEntity.this.isCharging
                    || DorrieEntity.this.isLeaping
                    || DorrieEntity.this.landAnimTicks > 0
                    || DorrieEntity.this.getTarget() != null) {
                return false;
            }
            if (!DorrieEntity.this.isInWater()) {
                return false;
            }
            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
                return false;
            }
            if (DorrieEntity.this.random.nextInt(3) != 0) {
                return false;
            }
            this.targetPos = DorrieEntity.this.isUnderWater()
                    ? DorrieEntity.this.findSubmergedTarget(SEARCH_RADIUS, -5, 2)
                    : DorrieEntity.this.findSubmergedTarget(SEARCH_RADIUS, -5, 1);
            return this.targetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPos != null
                    && !DorrieEntity.this.isVehicle()
                    && DorrieEntity.this.getControllingPassenger() == null
                    && DorrieEntity.this.isAlive()
                    && DorrieEntity.this.isInWater();
        }

        @Override
        public void start() {
            this.phase = DorrieEntity.this.isUnderWater() ? Phase.ROAM : Phase.DIVE;
            this.phaseTicks = this.phase == Phase.DIVE
                    ? DorrieEntity.this.random.nextInt(DIVE_MAX_TICKS - DIVE_MIN_TICKS + 1) + DIVE_MIN_TICKS
                    : DorrieEntity.this.random.nextInt(ROAM_MAX_TICKS - ROAM_MIN_TICKS + 1) + ROAM_MIN_TICKS;
            this.targetRefreshTicks = 0;
            if (this.targetPos == null) {
                this.targetPos = this.pickTargetForPhase();
            }
            if (this.targetPos == null) {
                this.stop();
            }
        }

        @Override
        public void stop() {
            this.targetPos = null;
            this.phaseTicks = 0;
            this.targetRefreshTicks = 0;
            DorrieEntity.this.getNavigation().stop();
            this.cooldownTicks = DorrieEntity.this.random.nextInt(COOLDOWN_MAX_TICKS - COOLDOWN_MIN_TICKS + 1) + COOLDOWN_MIN_TICKS;
        }

        @Override
        public void tick() {
            if (this.targetPos == null) {
                this.stop();
                return;
            }

            if (this.phaseTicks > 0) {
                this.phaseTicks--;
            }
            if (this.targetRefreshTicks > 0) {
                this.targetRefreshTicks--;
            }

            if (this.phase == Phase.DIVE && this.phaseTicks <= 0) {
                this.phase = Phase.ROAM;
                this.phaseTicks = DorrieEntity.this.random.nextInt(ROAM_MAX_TICKS - ROAM_MIN_TICKS + 1) + ROAM_MIN_TICKS;
                this.targetPos = this.pickTargetForPhase();
            } else if (this.phase == Phase.ROAM && this.phaseTicks <= 0) {
                this.phase = Phase.SURFACE;
                this.phaseTicks = DorrieEntity.this.random.nextInt(DIVE_MAX_TICKS - DIVE_MIN_TICKS + 1) + DIVE_MIN_TICKS;
                this.targetPos = DorrieEntity.this.findWaterSurfaceTarget(SEARCH_RADIUS, -1, 4);
            } else if (this.phase == Phase.SURFACE && this.phaseTicks <= 0 && this.isCloseToTarget()) {
                this.stop();
                return;
            }

            if (this.targetPos == null || this.targetRefreshTicks <= 0 || this.isCloseToTarget()) {
                this.targetPos = this.pickTargetForPhase();
                this.targetRefreshTicks = TARGET_REFRESH_TICKS;
            }

            if (this.targetPos == null) {
                this.stop();
                return;
            }

            DorrieEntity.this.getLookControl().setLookAt(this.targetPos.x, this.targetPos.y, this.targetPos.z, 25.0F, 20.0F);
            DorrieEntity.this.getNavigation().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, AQUATIC_SPEED);
        }

        @Nullable
        private Vec3 pickTargetForPhase() {
            return this.phase == Phase.SURFACE
                    ? DorrieEntity.this.findWaterSurfaceTarget(SEARCH_RADIUS, -1, 4)
                    : DorrieEntity.this.findSubmergedTarget(SEARCH_RADIUS, -5, 2);
        }

        private boolean isCloseToTarget() {
            return this.targetPos != null && DorrieEntity.this.distanceToSqr(this.targetPos) < 4.0D;
        }

        private enum Phase {
            DIVE,
            ROAM,
            SURFACE
        }
    }

    private static class HuntCheepGoal extends Goal {
        private static final int COOLDOWN_TICKS = 600;
        private static final double HUNT_RANGE = 16.0D;
        private static final double ATTACK_RANGE_SQ = 2.5D * 2.5D;

        private final DorrieEntity dorrie;
        private CheepEntity target;
        private int cooldown = 0;

        HuntCheepGoal(DorrieEntity dorrie) {
            this.dorrie = dorrie;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (dorrie.level().isClientSide) {
                return false;
            }
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (dorrie.getControllingPassenger() != null) {
                return false;
            }
            List<CheepEntity> nearby = dorrie.level().getEntitiesOfClass(
                    CheepEntity.class, dorrie.getBoundingBox().inflate(HUNT_RANGE));
            if (nearby.isEmpty()) {
                return false;
            }
            nearby.sort((a, b) -> Double.compare(a.distanceToSqr(dorrie), b.distanceToSqr(dorrie)));
            target = nearby.get(0);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && target.isAlive()
                    && dorrie.getControllingPassenger() == null
                    && dorrie.distanceToSqr(target) <= HUNT_RANGE * HUNT_RANGE * 4;
        }

        @Override
        public void start() {
            dorrie.getNavigation().moveTo(target, 1.4D);
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            dorrie.getLookControl().setLookAt(target, 30.0F, 30.0F);
            dorrie.getNavigation().moveTo(target, 1.4D);
            if (dorrie.distanceToSqr(target) <= ATTACK_RANGE_SQ) {
                target.hurt(dorrie.damageSources().mobAttack(dorrie),
                        (float) dorrie.getAttributeValue(Attributes.ATTACK_DAMAGE));
                stop();
            }
        }

        @Override
        public void stop() {
            target = null;
            dorrie.getNavigation().stop();
            cooldown = COOLDOWN_TICKS;
        }
    }
}
