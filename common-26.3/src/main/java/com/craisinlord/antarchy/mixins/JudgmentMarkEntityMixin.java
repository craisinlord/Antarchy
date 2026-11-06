package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.JudgmentMarkAccess;
import com.craisinlord.antarchy.content.util.SyncedEntityFlags;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JudgmentMarkEntityMixin implements JudgmentMarkAccess {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickJudgmentState(CallbackInfo ci) {
        com.craisinlord.antarchy.content.effect.JudgmentMarkManager.tickTarget((LivingEntity) (Object) this);
    }

    @Override
    public boolean antarchy$isJudgmentMarked() {
        return SyncedEntityFlags.get((LivingEntity) (Object) this, SyncedEntityFlags.Flag.JUDGMENT_MARKED);
    }

    @Override
    public void antarchy$setJudgmentMarked(boolean marked) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide()) {
            SyncedEntityFlags.set(entity, SyncedEntityFlags.Flag.JUDGMENT_MARKED, marked);
        }
    }
}
