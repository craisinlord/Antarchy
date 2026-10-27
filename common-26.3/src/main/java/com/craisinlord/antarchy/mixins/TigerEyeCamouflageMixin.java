package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageAccess;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class TigerEyeCamouflageMixin implements TigerEyeCamouflageAccess {
    @Unique private boolean antarchy$tigerEyeCamouflageActive;
    @Unique private int antarchy$tigerEyeCamouflageBlockStateId;

    @Override
    public boolean antarchy$isTigerEyeCamouflageActive() {
        return antarchy$tigerEyeCamouflageActive;
    }

    @Override
    public void antarchy$setTigerEyeCamouflageActive(boolean active) {
        antarchy$tigerEyeCamouflageActive = active;
    }

    @Override
    public int antarchy$getTigerEyeCamouflageBlockStateId() {
        return antarchy$tigerEyeCamouflageBlockStateId;
    }

    @Override
    public void antarchy$setTigerEyeCamouflageBlockStateId(int blockStateId) {
        antarchy$tigerEyeCamouflageBlockStateId = blockStateId;
    }
}
