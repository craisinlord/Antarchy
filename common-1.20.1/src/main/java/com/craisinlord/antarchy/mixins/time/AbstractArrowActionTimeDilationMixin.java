package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowActionTimeDilationMixin {
    @Shadow private int life;
    @Shadow protected int inGroundTime;
    @Shadow public int shakeTime;

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shakeTime:I", opcode = Opcodes.PUTFIELD))
    private void antarchy$slowShakeTime(AbstractArrow arrow, int value) {
        if (TimeDilationApi.consumeTick(arrow, "arrow_shake")) this.shakeTime = value;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;inGroundTime:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void antarchy$slowInGroundTime(AbstractArrow arrow, int value) {
        if (TimeDilationApi.consumeTick(arrow, "arrow_in_ground")) this.inGroundTime = value;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;inGroundTime:I", opcode = Opcodes.PUTFIELD, ordinal = 1))
    private void antarchy$resetInGroundTime(AbstractArrow arrow, int value) {
        this.inGroundTime = value;
    }

    @Redirect(method = "tickDespawn", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;life:I", opcode = Opcodes.PUTFIELD))
    private void antarchy$slowLifetime(AbstractArrow arrow, int value) {
        if (TimeDilationApi.consumeTick(arrow, "arrow_lifetime")) this.life = value;
    }
}
