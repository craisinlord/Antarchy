package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.minecart.AntimetalDismountHelper;
import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import com.craisinlord.antarchy.content.minecart.AntimetalMinecartPhysics;
import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin implements AntimetalMinecartAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> ANTARCHY$ON_ANTIMETAL_RAIL =
            SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
    private static final int ANTARCHY$INVERTED_REFRESH_TICKS = 20;
    private static final double ANTARCHY$STANDING_PASSENGER_OFFSET = 0.1875D;

    @Unique
    private boolean antarchy$antimetalActive;
    @Unique
    @Nullable
    private BlockPos antarchy$antimetalRailPos;
    @Unique
    @Nullable
    private BlockState antarchy$antimetalRailState;

    @Shadow
    protected abstract void moveAlongTrack(BlockPos pos, BlockState state);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void antarchy$defineData(CallbackInfo ci) {
        ((AbstractMinecart) (Object) this).getEntityData().define(ANTARCHY$ON_ANTIMETAL_RAIL, false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$findAntimetalRail(CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        BlockPos cell = AntimetalMinecartPhysics.findCell(self.level(), self.getX(), self.getY(), self.getZ());
        antarchy$antimetalActive = cell != null;
        antarchy$antimetalRailPos = cell;
        antarchy$antimetalRailState = cell != null ? self.level().getBlockState(cell) : null;
        self.getEntityData().set(ANTARCHY$ON_ANTIMETAL_RAIL, antarchy$antimetalActive);
        if (antarchy$antimetalActive) {
            antarchy$applyInvertedGravity(self);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;isRail(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean antarchy$redirectIsRail(BlockState state) {
        if (antarchy$antimetalActive) {
            return true;
        }
        return BaseRailBlock.isRail(state);
    }

    @Inject(method = "moveAlongTrack", at = @At("HEAD"), cancellable = true)
    private void antarchy$redirectMoveAlongTrack(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (antarchy$antimetalActive && antarchy$antimetalRailPos != null && antarchy$antimetalRailState != null) {
            AntimetalMinecartPhysics.moveAlongTrack((AbstractMinecart) (Object) this, antarchy$antimetalRailPos, antarchy$antimetalRailState);
            ci.cancel();
        }
    }

    @Unique
    private void antarchy$applyInvertedGravity(AbstractMinecart cart) {
        for (Entity passenger : cart.getIndirectPassengers()) {
            if (!(passenger instanceof LivingEntity living)) {
                continue;
            }
            MobEffectInstance current = living.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
            if (current == null || current.getDuration() < 10) {
                living.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), ANTARCHY$INVERTED_REFRESH_TICKS, 0, true, false, false));
            }
        }
    }

    @Inject(method = "getPassengersRidingOffset", at = @At("RETURN"), cancellable = true)
    private void antarchy$mirrorPassengerOffset(CallbackInfoReturnable<Double> cir) {
        if (this.antarchy$isOnAntimetalRail()) {
            double normalOffset = cir.getReturnValue();
            cir.setReturnValue(2.0D * AntimetalRailHelper.CART_MODEL_PIVOT_Y - normalOffset - ANTARCHY$STANDING_PASSENGER_OFFSET);
        }
    }

    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"), cancellable = true)
    private void antarchy$dismountBelowRail(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
        if (this.antarchy$isOnAntimetalRail()) {
            AbstractMinecart self = (AbstractMinecart) (Object) this;
            cir.setReturnValue(AntimetalDismountHelper.findDismountPosition(self, passenger));
        }
    }

    @Inject(method = "getPosOffs", at = @At("HEAD"), cancellable = true)
    private void antarchy$sampleAntimetalPosOffs(double x, double y, double z, double offset, CallbackInfoReturnable<Vec3> cir) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (this.antarchy$isOnAntimetalRail()) {
            cir.setReturnValue(AntimetalMinecartPhysics.getPosOffs(self, x, y, z, offset));
        }
    }

    @Inject(method = "getPos", at = @At("HEAD"), cancellable = true)
    private void antarchy$sampleAntimetalPos(double x, double y, double z, CallbackInfoReturnable<Vec3> cir) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (this.antarchy$isOnAntimetalRail()) {
            cir.setReturnValue(AntimetalMinecartPhysics.getPos(self, x, y, z));
        }
    }

    @Override
    public boolean antarchy$isOnAntimetalRail() {
        return ((AbstractMinecart) (Object) this).getEntityData().get(ANTARCHY$ON_ANTIMETAL_RAIL);
    }

    @Override
    public void antarchy$setOnAntimetalRail(boolean value) {
        ((AbstractMinecart) (Object) this).getEntityData().set(ANTARCHY$ON_ANTIMETAL_RAIL, value);
    }

    @Override
    @Nullable
    public BlockPos antarchy$getAntimetalRailPos() {
        return antarchy$antimetalRailPos;
    }

    @Override
    public void antarchy$setAntimetalRailPos(@Nullable BlockPos pos) {
        antarchy$antimetalRailPos = pos;
    }
}
