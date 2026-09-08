package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityAccess;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
/*
 * Adds the base gravity state and sync hooks to entities.
 */
public abstract class EntityGravityMixin implements AntarchyGravityAccess {
    @Unique
    private static final String ANTARCHY_GRAVITY_DIRECTION_TAG = "AntarchyGravityDirection";
    @Unique
    private static final String ANTARCHY_PREV_GRAVITY_DIRECTION_TAG = "AntarchyPrevGravityDirection";
    @Unique
    private static final String ANTARCHY_GRAVITY_FORCED_TAG = "AntarchyGravityForced";
    @Unique
    private static final String ANTARCHY_GRAVITY_TRANSITION_DURATION_TAG = "AntarchyGravityTransitionDuration";
    @Unique
    private static final String ANTARCHY_GRAVITY_TRANSITION_REMAINING_TAG = "AntarchyGravityTransitionRemaining";

    @Unique
    private AntarchyGravityDirection antarchy$gravityDirection = AntarchyGravityDirection.DOWN;
    @Unique
    private AntarchyGravityDirection antarchy$prevGravityDirection = AntarchyGravityDirection.DOWN;
    @Unique
    private boolean antarchy$gravityForced;
    @Unique
    private int antarchy$gravityTransitionDuration;
    @Unique
    private int antarchy$gravityTransitionRemaining;

    @Shadow
    protected abstract AABB makeBoundingBox();

    @Shadow
    public abstract void setBoundingBox(AABB boundingBox);

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void antarchy$saveGravityState(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        tag.putString(ANTARCHY_GRAVITY_DIRECTION_TAG, this.antarchy$gravityDirection.getSerializedName());
        tag.putString(ANTARCHY_PREV_GRAVITY_DIRECTION_TAG, this.antarchy$prevGravityDirection.getSerializedName());
        tag.putBoolean(ANTARCHY_GRAVITY_FORCED_TAG, this.antarchy$gravityForced);
        tag.putInt(ANTARCHY_GRAVITY_TRANSITION_DURATION_TAG, this.antarchy$gravityTransitionDuration);
        tag.putInt(ANTARCHY_GRAVITY_TRANSITION_REMAINING_TAG, this.antarchy$gravityTransitionRemaining);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void antarchy$readGravityState(CompoundTag tag, CallbackInfo ci) {
        this.antarchy$gravityDirection = AntarchyGravityDirection.parse(tag.getString(ANTARCHY_GRAVITY_DIRECTION_TAG))
                .orElse(AntarchyGravityDirection.DOWN);
        this.antarchy$prevGravityDirection = AntarchyGravityDirection.parse(tag.getString(ANTARCHY_PREV_GRAVITY_DIRECTION_TAG))
                .orElse(this.antarchy$gravityDirection);
        this.antarchy$gravityForced = tag.getBoolean(ANTARCHY_GRAVITY_FORCED_TAG);
        this.antarchy$gravityTransitionDuration = Math.max(0, tag.getInt(ANTARCHY_GRAVITY_TRANSITION_DURATION_TAG));
        this.antarchy$gravityTransitionRemaining = Math.max(0, tag.getInt(ANTARCHY_GRAVITY_TRANSITION_REMAINING_TAG));

        Entity entity = this.antarchy$entity();
        if (this.antarchy$gravityDirection.isInverted() && this.antarchy$gravityForced
                && entity instanceof Mob mob) {
            MobEffectInstance invertedEffect = mob.getEffect(com.craisinlord.antarchy.content.AntarchyObjects.INVERTED_EFFECT.get());
            boolean aboveUndersideExit = ThoraxisUndersideManager.isAboveUndersideExit(mob);
            int nearbyDimensionalTears = this.antarchy$countNearbyDimensionalTears(mob);
            if (invertedEffect != null
                    && invertedEffect.isAmbient()
                    && !invertedEffect.isVisible()
                    && aboveUndersideExit) {
                this.antarchy$clearLoadedInversion(mob);
                com.craisinlord.antarchy.Antarchy.LOGGER.warn(
                        "[antarchy-gravity] cleared stale underside inversion from loaded mob type={} uuid={} pos=({}, {}, {}) dim={} effectDuration={} nearbyDimensionalTears={}",
                        mob.getType(), mob.getUUID(), mob.getX(), mob.getY(), mob.getZ(),
                        mob.level() != null ? mob.level().dimension().location() : "?",
                        invertedEffect.getDuration(), nearbyDimensionalTears
                );
                return;
            }
            if (invertedEffect != null
                    && mob instanceof BedBugEntity
                    && aboveUndersideExit
                    && ThoraxisUndersideManager.isThoraxis(mob.level())) {
                this.antarchy$clearLoadedInversion(mob);
                com.craisinlord.antarchy.Antarchy.LOGGER.warn(
                        "[antarchy-gravity] cleared loaded inverted bed bug above underside exit type={} uuid={} pos=({}, {}, {}) dim={} effectDuration={} effectAmbient={} effectVisible={} nearbyDimensionalTears={}",
                        mob.getType(), mob.getUUID(), mob.getX(), mob.getY(), mob.getZ(),
                        mob.level() != null ? mob.level().dimension().location() : "?",
                        invertedEffect.getDuration(), invertedEffect.isAmbient(), invertedEffect.isVisible(), nearbyDimensionalTears
                );
                return;
            }
            com.craisinlord.antarchy.Antarchy.LOGGER.info(
                    "[antarchy-gravity] loaded inverted mob type={} uuid={} pos=({}, {}, {}) dim={} hasInvertedEffect={} effectDuration={} effectAmbient={} effectVisible={} aboveUndersideExit={} nearbyDimensionalTears={}",
                    mob.getType(), mob.getUUID(), mob.getX(), mob.getY(), mob.getZ(),
                    mob.level() != null ? mob.level().dimension().location() : "?",
                    invertedEffect != null,
                    invertedEffect != null ? invertedEffect.getDuration() : -1,
                    invertedEffect != null && invertedEffect.isAmbient(),
                    invertedEffect != null && invertedEffect.isVisible(),
                    aboveUndersideExit,
                    nearbyDimensionalTears
            );
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void antarchy$tickGravityTransition(CallbackInfo ci) {
        if (this.antarchy$gravityTransitionRemaining > 0) {
            this.antarchy$gravityTransitionRemaining--;
        }
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("TAIL"))
    private void antarchy$resetFallDistanceInAntiwater(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = this.antarchy$entity();
        AntarchyGravityDirection direction = this.antarchy$gravityDirection;
        if (!direction.isInverted()) {
            return;
        }

        if (!antarchy$isTouchingAntiwater(entity)) {
            return;
        }

        entity.resetFallDistance();
    }

    @Override
    public AntarchyGravityDirection antarchy$getGravityDirection() {
        return this.antarchy$gravityDirection;
    }

    @Override
    public AntarchyGravityDirection antarchy$getPrevGravityDirection() {
        return this.antarchy$prevGravityDirection;
    }

    @Override
    public boolean antarchy$isGravityForced() {
        return this.antarchy$gravityForced;
    }

    @Override
    public int antarchy$getGravityTransitionDuration() {
        return this.antarchy$gravityTransitionDuration;
    }

    @Override
    public int antarchy$getGravityTransitionRemaining() {
        return this.antarchy$gravityTransitionRemaining;
    }

    @Override
    public void antarchy$setGravityState(AntarchyGravityDirection direction, boolean forced, AntarchyGravityTransition transition) {
        AntarchyGravityDirection previousDirection = this.antarchy$gravityDirection;
        boolean changed = this.antarchy$gravityDirection != direction || this.antarchy$gravityForced != forced;
        if (this.antarchy$gravityDirection != direction) {
            this.antarchy$adjustPositionForGravityChange(this.antarchy$gravityDirection, direction);
            this.antarchy$prevGravityDirection = this.antarchy$gravityDirection;
            this.antarchy$gravityDirection = direction;
            this.antarchy$gravityTransitionDuration = transition.durationTicks();
            this.antarchy$gravityTransitionRemaining = transition.durationTicks();
            this.antarchy$refreshGravityBounds();
        }

        this.antarchy$gravityForced = forced;
        if (changed && this.antarchy$entity() instanceof Mob mob) {
            // Any existing path is expressed in the previous gravity frame.
            // Keep goals from following stale nodes after a flip.
            mob.getNavigation().stop();
        }
        if (changed && !this.antarchy$entity().level().isClientSide) {
            AntarchyGravityApi.notifyGravityStateChanged(this.antarchy$entity());
        }
    }

    @Override
    public void antarchy$applySyncedGravityState(
            AntarchyGravityDirection direction,
            AntarchyGravityDirection previousDirection,
            boolean forced,
            int transitionDuration,
            int transitionRemaining
    ) {
        AntarchyGravityDirection oldDirection = this.antarchy$gravityDirection;
        this.antarchy$gravityDirection = direction;
        this.antarchy$prevGravityDirection = previousDirection;
        this.antarchy$gravityForced = forced;
        this.antarchy$gravityTransitionDuration = Math.max(0, transitionDuration);
        this.antarchy$gravityTransitionRemaining = Math.max(0, transitionRemaining);
        if (oldDirection != direction) {
            // Do NOT adjust position here — the server entity's position is already
            // correct (adjusted when gravity first changed on the server). Adjusting
            // again on the client causes a double-shift on relog, which creates
            // a position desync and stuttering as the server repeatedly corrects us.
            this.antarchy$refreshGravityBounds();
        }
    }

    @Unique
    private Entity antarchy$entity() {
        return (Entity) (Object) this;
    }

    @Unique
    private void antarchy$refreshGravityBounds() {
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Unique
    private void antarchy$clearLoadedInversion(Mob mob) {
        mob.removeEffect(com.craisinlord.antarchy.content.AntarchyObjects.INVERTED_EFFECT.get());
        this.antarchy$gravityDirection = AntarchyGravityDirection.DOWN;
        this.antarchy$prevGravityDirection = AntarchyGravityDirection.DOWN;
        this.antarchy$gravityForced = false;
        this.antarchy$gravityTransitionDuration = 0;
        this.antarchy$gravityTransitionRemaining = 0;
        this.antarchy$refreshGravityBounds();
    }

    @Unique
    private int antarchy$countNearbyDimensionalTears(Mob mob) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return -1;
        }
        return serverLevel.getEntitiesOfClass(
                DimensionalTearEntity.class,
                mob.getBoundingBox().inflate(48.0D),
                Entity::isAlive
        ).size();
    }

    @Unique
    private void antarchy$adjustPositionForGravityChange(AntarchyGravityDirection oldDirection, AntarchyGravityDirection newDirection) {
        if (oldDirection == newDirection) {
            return;
        }

        Entity entity = this.antarchy$entity();
        double yOffset = newDirection.isInverted() ? entity.getBbHeight() : -entity.getBbHeight();
        entity.setPos(entity.getX(), entity.getY() + yOffset, entity.getZ());
        entity.setOnGround(false);
        entity.resetFallDistance();
    }

    @ModifyVariable(method = "checkFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double antarchy$invertFallDamageY(double worldY) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity) || entity instanceof Player) {
            return worldY;
        }

        return -worldY;
    }

    @Unique
    private boolean antarchy$isTouchingAntiwater(Entity entity) {
        return antarchy$intersectsAntiwater(entity, entity.getBoundingBox().inflate(0.05D));
    }

    @Unique
    private boolean antarchy$intersectsAntiwater(Entity entity, AABB box) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    if (com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @ModifyVariable(method = "isSupportedBy", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private BlockPos antarchy$mirrorSupportBlock(BlockPos pos) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return pos;
        }

        return pos.above();
    }
}
