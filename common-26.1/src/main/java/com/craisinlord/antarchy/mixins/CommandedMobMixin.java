package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class CommandedMobMixin implements CommandedEntityAccess {
    @Unique
    private UUID antarchy$commanderUuid;
    @Unique
    private boolean antarchy$commandedTargetOwned;
    @Unique
    private boolean antarchy$royalInvested;

    @Override
    public UUID antarchy$getCommanderUuid() {
        return antarchy$commanderUuid;
    }

    @Override
    public void antarchy$setCommanderUuid(UUID uuid) {
        antarchy$commanderUuid = uuid;
    }

    @Override
    public boolean antarchy$isCommandedTargetOwned() {
        return antarchy$commandedTargetOwned;
    }

    @Override
    public void antarchy$setCommandedTargetOwned(boolean owned) {
        antarchy$commandedTargetOwned = owned;
    }

    @Override
    public boolean antarchy$isRoyalInvested() {
        return antarchy$royalInvested;
    }

    @Override
    public void antarchy$setRoyalInvested(boolean invested) {
        antarchy$royalInvested = invested;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void antarchy$saveCommandedData(ValueOutput tag, CallbackInfo ci) {
        if (antarchy$commanderUuid != null) {
            tag.store("AntarchyCommander", UUIDUtil.CODEC, antarchy$commanderUuid);
            tag.putBoolean("AntarchyCommandedTargetOwned", antarchy$commandedTargetOwned);
            tag.putBoolean("AntarchyRoyalInvested", antarchy$royalInvested);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void antarchy$loadCommandedData(ValueInput tag, CallbackInfo ci) {
        tag.read("AntarchyCommander", UUIDUtil.CODEC).ifPresent(uuid -> {
            antarchy$commanderUuid = uuid;
            antarchy$commandedTargetOwned = tag.getBooleanOr("AntarchyCommandedTargetOwned", false);
            antarchy$royalInvested = tag.getBooleanOr("AntarchyRoyalInvested", false);
        });
    }
}
