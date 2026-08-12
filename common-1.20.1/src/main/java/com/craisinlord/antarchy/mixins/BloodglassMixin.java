package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class BloodglassMixin implements BloodglassAccess {
    @Unique private int antarchy$armorShieldsActive;
    @Unique private int antarchy$armorShieldLostCount;
    @Unique private int antarchy$armorRechargeTimer;
    @Unique private int antarchy$appleShieldsActive;
    @Unique private int antarchy$appleShieldLostCount;
    @Unique private int antarchy$appleRechargeTimer;

    @Override public int antarchy$getArmorShieldsActive()    { return antarchy$armorShieldsActive; }
    @Override public void antarchy$setArmorShieldsActive(int v) { antarchy$armorShieldsActive = Math.max(0, v); }
    @Override public int antarchy$getArmorShieldLostCount()  { return antarchy$armorShieldLostCount; }
    @Override public void antarchy$setArmorShieldLostCount(int v) { antarchy$armorShieldLostCount = Math.max(0, v); }
    @Override public int antarchy$getArmorRechargeTimer()    { return antarchy$armorRechargeTimer; }
    @Override public void antarchy$setArmorRechargeTimer(int v) { antarchy$armorRechargeTimer = Math.max(0, v); }

    @Override public int antarchy$getAppleShieldsActive()    { return antarchy$appleShieldsActive; }
    @Override public void antarchy$setAppleShieldsActive(int v) { antarchy$appleShieldsActive = Math.max(0, v); }
    @Override public int antarchy$getAppleShieldLostCount()  { return antarchy$appleShieldLostCount; }
    @Override public void antarchy$setAppleShieldLostCount(int v) { antarchy$appleShieldLostCount = Math.max(0, v); }
    @Override public int antarchy$getAppleRechargeTimer()    { return antarchy$appleRechargeTimer; }
    @Override public void antarchy$setAppleRechargeTimer(int v) { antarchy$appleRechargeTimer = Math.max(0, v); }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void antarchy$saveBloodglass(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("antarchy.ArmorShieldsActive", antarchy$armorShieldsActive);
        tag.putInt("antarchy.ArmorShieldLostCount", antarchy$armorShieldLostCount);
        tag.putInt("antarchy.ArmorRechargeTimer", antarchy$armorRechargeTimer);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void antarchy$loadBloodglass(CompoundTag tag, CallbackInfo ci) {
        antarchy$armorShieldsActive = Math.max(0, tag.getInt("antarchy.ArmorShieldsActive"));
        antarchy$armorShieldLostCount = Math.max(0, tag.getInt("antarchy.ArmorShieldLostCount"));
        antarchy$armorRechargeTimer = Math.max(0, tag.getInt("antarchy.ArmorRechargeTimer"));
    }
}
