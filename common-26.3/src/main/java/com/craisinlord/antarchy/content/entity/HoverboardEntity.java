package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import com.craisinlord.antarchy.mixins.LivingEntityJumpingAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class HoverboardEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TRAIL_ACTIVE = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation MOVE_ANIM = RawAnimation.begin().thenLoop("move");

    private static final double MAX_HOVER_HEIGHT = 5.0D;
    private static final double ASCEND_SPEED = 0.2D;
    private static final double SETTLE_DESCEND_SPEED = 0.08D;
    private static final double FAST_DESCEND_SPEED = 0.9D;
    private static final double FAST_DESCEND_RAMP = 0.2D;
    private static final double DAMPING = 0.955D;
    private static final double RIDER_Y_OFFSET = 0.65D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public HoverboardEntity(EntityType<? extends HoverboardEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return effectInstance.getEffect().value() instanceof com.craisinlord.anthology.effect.InvertedMobEffect;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isAlive() || this.isInvulnerableTo(level, source)) {
            return false;
        }
        this.ejectPassengers();
        this.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
        this.spawnAtLocation(level, this.getDropStack());
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
        builder.define(TRAIL_ACTIVE, false);
    }

    @Nullable
    public DyeColor getColor() {
        int id = this.entityData.get(COLOR);
        return id < 0 ? null : DyeColor.byId(id);
    }

    public void setColor(@Nullable DyeColor color) {
        this.entityData.set(COLOR, color == null ? -1 : color.getId());
    }

    public boolean isTrailActive() {
        return this.entityData.get(TRAIL_ACTIVE);
    }

    private void setTrailActive(boolean active) {
        this.entityData.set(TRAIL_ACTIVE, active);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.getColor() != null) {
            output.putString("Color", this.getColor().getName());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setColor(input.getString("Color").map(name -> DyeColor.byName(name, null)).orElse(null));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
                this.playSound(AntarchySoundEvents.HOVERBOARD_MOUNT.get(), 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide()) {
            this.playSound(AntarchySoundEvents.HOVERBOARD_DISMOUNT.get(), 1.0F, 1.0F);
        }
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
        Vec3 riderOffset = GravityRotationUtil.vecPlayerToWorld(
                0.0D,
                RIDER_Y_OFFSET,
                0.0D,
                GravityApi.getGravityDirection(this)
        );
        moveFunction.accept(passenger, this.getX() + riderOffset.x, this.getY() + riderOffset.y, this.getZ() + riderOffset.z);
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
            this.setTrailActive(forward > 0.0F);
            boolean holdingJump = ((LivingEntityJumpingAccessor) rider).antarchy$isJumping();
            double verticalMotion = this.resolveVerticalMotion(holdingJump);
            double acceleration = AntarchySettings.hoverboardAcceleration();
            double maxSpeed = AntarchySettings.hoverboardMaxSpeed();

            Vec3 currentHorizontal = new Vec3(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
            Vec3 horizontalMotion = currentHorizontal.scale(DAMPING).add(look.scale(forward * acceleration));

            double horizontalSpeed = horizontalMotion.horizontalDistance();
            if (horizontalSpeed > maxSpeed) {
                double scale = maxSpeed / horizontalSpeed;
                horizontalMotion = new Vec3(horizontalMotion.x * scale, 0.0D, horizontalMotion.z * scale);
            }

            this.setDeltaMovement(horizontalMotion.x, verticalMotion, horizontalMotion.z);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        this.setTrailActive(false);
        double verticalMotion = this.resolveVerticalMotion(false);
        Vec3 horizontalMotion = new Vec3(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z).scale(DAMPING);
        this.setDeltaMovement(horizontalMotion.x, verticalMotion, horizontalMotion.z);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private double resolveVerticalMotion(boolean holdingJump) {
        GravityDirection gravityDirection = GravityApi.getGravityDirection(this);
        double supportHeight = this.findSupportHeight(gravityDirection);
        double height = gravityDirection.isInverted() ? supportHeight - this.getY() : this.getY() - supportHeight;
        if (holdingJump) {
            return height >= MAX_HOVER_HEIGHT ? 0.0D : ASCEND_SPEED;
        }
        if (height <= 0.0D) {
            return 0.0D;
        }
        double heightAboveBand = height - MAX_HOVER_HEIGHT;
        if (heightAboveBand > 0.0D) {
            return -Mth.clamp(SETTLE_DESCEND_SPEED + heightAboveBand * FAST_DESCEND_RAMP, SETTLE_DESCEND_SPEED, FAST_DESCEND_SPEED);
        }
        return -SETTLE_DESCEND_SPEED;
    }

    private double findSupportHeight(GravityDirection gravityDirection) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        if (!gravityDirection.isInverted()) {
            int minY = this.level().getMinY();
            while (pos.getY() > minY && this.level().getBlockState(pos).isAir()) {
                pos.move(Direction.DOWN);
            }
            return pos.getY() + 1.0D;
        }

        int maxY = this.level().getMaxY();
        while (pos.getY() < maxY && this.level().getBlockState(pos).isAir()) {
            pos.move(Direction.UP);
        }
        return this.level().getBlockState(pos).isAir() ? this.getY() : pos.getY();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel serverLevel) {
            this.spawnAtLocation(serverLevel, this.getDropStack());
        }
    }

    public ItemStack getDropStack() {
        String path = this.getColor() == null ? "hoverboard" : "hoverboard_" + this.getColor().getName();
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
        return new ItemStack(item);
    }

    @Override
    public ItemStack getPickResult() {
        return this.getDropStack();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<HoverboardEntity>("movement", 0, this::movementController));
    }

    private PlayState movementController(AnimationTest<HoverboardEntity> state) {
        return state.setAndContinue(this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D ? MOVE_ANIM : IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
