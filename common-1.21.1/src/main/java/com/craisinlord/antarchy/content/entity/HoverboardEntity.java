package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.mixins.LivingEntityJumpingAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

public class HoverboardEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation MOVE_ANIM = RawAnimation.begin().thenLoop("move");

    private static final double MAX_HOVER_HEIGHT = 5.0D;
    private static final double ACCELERATION = 0.08D;
    private static final double MAX_SPEED = 1.8D;
    private static final double ASCEND_SPEED = 0.15D;
    private static final double SETTLE_DESCEND_SPEED = 0.08D;
    private static final double DAMPING = 0.955D;
    private static final double RIDER_Y_OFFSET = 0.65D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public HoverboardEntity(EntityType<? extends HoverboardEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || !this.isAlive()) {
            return false;
        }
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        this.ejectPassengers();
        this.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
        this.spawnAtLocation(this.getDropStack());
        this.discard();
        return true;
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, -1);
    }

    @Nullable
    public DyeColor getColor() {
        int id = this.entityData.get(COLOR);
        return id < 0 ? null : DyeColor.byId(id);
    }

    public void setColor(@Nullable DyeColor color) {
        this.entityData.set(COLOR, color == null ? -1 : color.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getColor() != null) {
            tag.putString("Color", this.getColor().getName());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setColor(tag.contains("Color") ? DyeColor.byName(tag.getString("Color"), null) : null);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof Player player ? player : null;
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        moveFunction.accept(passenger, this.getX(), this.getY() + RIDER_Y_OFFSET, this.getZ());
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.getControllingPassenger() instanceof Player rider) {
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            this.setXRot(0.0F);

            Vec3 look = Vec3.directionFromRotation(0.0F, this.getYRot());
            float forward = rider.zza;
            boolean holdingJump = ((LivingEntityJumpingAccessor) rider).antarchy$isJumping();
            double verticalMotion = this.resolveVerticalMotion(holdingJump);

            Vec3 currentHorizontal = new Vec3(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
            Vec3 horizontalMotion = currentHorizontal.scale(DAMPING).add(look.scale(forward * ACCELERATION));

            double horizontalSpeed = horizontalMotion.horizontalDistance();
            if (horizontalSpeed > MAX_SPEED) {
                double scale = MAX_SPEED / horizontalSpeed;
                horizontalMotion = new Vec3(horizontalMotion.x * scale, 0.0D, horizontalMotion.z * scale);
            }

            this.setDeltaMovement(horizontalMotion.x, verticalMotion, horizontalMotion.z);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        double verticalMotion = this.resolveVerticalMotion(false);
        Vec3 horizontalMotion = new Vec3(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z).scale(DAMPING);
        this.setDeltaMovement(horizontalMotion.x, verticalMotion, horizontalMotion.z);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private double resolveVerticalMotion(boolean holdingJump) {
        double groundHeight = this.findGroundHeight();
        double maxY = groundHeight + MAX_HOVER_HEIGHT;

        if (this.getY() > maxY) {
            return -SETTLE_DESCEND_SPEED;
        }
        if (holdingJump) {
            return this.getY() >= maxY ? 0.0D : ASCEND_SPEED;
        }
        return this.getY() <= groundHeight ? 0.0D : -SETTLE_DESCEND_SPEED;
    }

    private double findGroundHeight() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        int minY = this.level().getMinBuildHeight();
        while (pos.getY() > minY && this.level().getBlockState(pos).isAir()) {
            pos.move(Direction.DOWN);
        }
        return pos.getY() + 1.0D;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide) {
            this.spawnAtLocation(this.getDropStack());
        }
    }

    public ItemStack getDropStack() {
        String path = this.getColor() == null ? "hoverboard" : "hoverboard_" + this.getColor().getName();
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
        return new ItemStack(item);
    }

    @Override
    public ItemStack getPickResult() {
        return this.getDropStack();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 0, this::movementController));
    }

    private PlayState movementController(AnimationState<HoverboardEntity> state) {
        return state.setAndContinue(this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D ? MOVE_ANIM : IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
