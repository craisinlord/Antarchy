package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.JudgmentMarkAccess;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JudgmentMarkEntityMixin implements JudgmentMarkAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> ANTARCHY_JUDGMENT_MARKED =
            SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void antarchy$defineJudgmentState(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(ANTARCHY_JUDGMENT_MARKED, false);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickJudgmentState(CallbackInfo ci) {
        com.craisinlord.antarchy.content.effect.JudgmentMarkManager.tickTarget((LivingEntity) (Object) this);
    }

    @Override
    public boolean antarchy$isJudgmentMarked() {
        return ((LivingEntity) (Object) this).getEntityData().get(ANTARCHY_JUDGMENT_MARKED);
    }

    @Override
    public void antarchy$setJudgmentMarked(boolean marked) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide && entity.getEntityData().get(ANTARCHY_JUDGMENT_MARKED) != marked) {
            entity.getEntityData().set(ANTARCHY_JUDGMENT_MARKED, marked);
        }
    }
}
