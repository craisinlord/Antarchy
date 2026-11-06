package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.GiantLilyPadBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGiantLilyPadJumpMixin {
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void antarchy$launchFromGiantLilyPad(CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            GiantLilyPadBlock.launchFromJump(player);
        }
    }
}
