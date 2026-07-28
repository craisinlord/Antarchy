package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.block.DuctTapeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Input.class)
public abstract class DuctTapeInputMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void blockInputInDuctTape(boolean isSneaking, float slowFactor, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isNoGravity() && !DuctTapeBlock.isTouchingTape(mc.level, mc.player)) return;

        Input self = (Input) (Object) this;
        self.left = false;
        self.right = false;
        self.up = false;
        self.down = false;
        self.leftImpulse = 0.0F;
        self.forwardImpulse = 0.0F;
        self.jumping = false;
        self.shiftKeyDown = false;
    }
}
