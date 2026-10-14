package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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

    @org.spongepowered.asm.mixin.injection.Inject(method = "addAdditionalSaveData", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void antarchy$saveCommandedData(CompoundTag tag, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (antarchy$commanderUuid != null) {
            tag.putUUID("AntarchyCommander", antarchy$commanderUuid);
            tag.putBoolean("AntarchyCommandedTargetOwned", antarchy$commandedTargetOwned);
            tag.putBoolean("AntarchyRoyalInvested", antarchy$royalInvested);
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "readAdditionalSaveData", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void antarchy$loadCommandedData(CompoundTag tag, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (tag.hasUUID("AntarchyCommander")) {
            antarchy$commanderUuid = tag.getUUID("AntarchyCommander");
            antarchy$commandedTargetOwned = tag.getBoolean("AntarchyCommandedTargetOwned");
            antarchy$royalInvested = tag.getBoolean("AntarchyRoyalInvested");
        }
    }
}
