package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.minecart.AntimetalDismountHelper;
import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import com.craisinlord.antarchy.content.minecart.AntimetalMinecartPhysics;
import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import com.craisinlord.antarchy.content.util.SyncedEntityFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin implements AntimetalMinecartAccess {
    private static final int ANTARCHY$INVERTED_REFRESH_TICKS = 20;

    @Unique
    private boolean antarchy$antimetalActive;
    @Unique
    @Nullable
    private BlockPos antarchy$antimetalRailPos;

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$findAntimetalRail(CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        BlockPos cell = AntimetalMinecartPhysics.findCell(self.level(), self.getX(), self.getY(), self.getZ());
        antarchy$antimetalActive = cell != null;
        antarchy$antimetalRailPos = cell;
        SyncedEntityFlags.set(self, SyncedEntityFlags.Flag.ON_ANTIMETAL_RAIL, antarchy$antimetalActive);
        if (antarchy$antimetalActive) {
            antarchy$applyInvertedGravity(self);
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

    @Unique
    private static final double ANTARCHY$STANDING_PASSENGER_OFFSET = 0.1875D;

    @Inject(method = "getPassengerAttachmentPoint", at = @At("RETURN"), cancellable = true)
    private void antarchy$mirrorPassengerAttachment(Entity passenger, EntityDimensions dimensions, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (this.antarchy$isOnAntimetalRail()) {
            Vec3 original = cir.getReturnValue();
            boolean standing = passenger instanceof Villager || passenger instanceof WanderingTrader;
            double sourceY = standing ? original.y + ANTARCHY$STANDING_PASSENGER_OFFSET : original.y;
            double mirroredY = 2.0D * AntimetalRailHelper.CART_MODEL_PIVOT_Y - sourceY + 1.0D;
            if (standing) {
                mirroredY -= ANTARCHY$STANDING_PASSENGER_OFFSET;
            }
            cir.setReturnValue(new Vec3(original.x, mirroredY, original.z));
        }
    }

    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"), cancellable = true)
    private void antarchy$dismountBelowRail(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
        if (this.antarchy$isOnAntimetalRail()) {
            AbstractMinecart self = (AbstractMinecart) (Object) this;
            cir.setReturnValue(AntimetalDismountHelper.findDismountPosition(self, passenger));
        }
    }

    @Override
    public boolean antarchy$isOnAntimetalRail() {
        return SyncedEntityFlags.get((AbstractMinecart) (Object) this, SyncedEntityFlags.Flag.ON_ANTIMETAL_RAIL);
    }

    @Override
    public void antarchy$setOnAntimetalRail(boolean value) {
        SyncedEntityFlags.set((AbstractMinecart) (Object) this, SyncedEntityFlags.Flag.ON_ANTIMETAL_RAIL, value);
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
