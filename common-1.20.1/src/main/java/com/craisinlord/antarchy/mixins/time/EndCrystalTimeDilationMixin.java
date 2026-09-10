package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(EndCrystal.class)
public abstract class EndCrystalTimeDilationMixin {
    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;time:I", opcode = Opcodes.PUTFIELD))
    private void antarchy$slowCrystalTime(EndCrystal crystal, int value, Operation<Void> original) {
        if (TimeDilationApi.consumeTick(crystal, "end_crystal_time")) {
            original.call(crystal, value);
        }
    }
}
