package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class StinkyCrouchMixin {
    @Inject(method = "setShiftKeyDown", at = @At("HEAD"))
    private void antarchy$playStinkyCrouchSound(boolean crouching, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!crouching || entity.isShiftKeyDown() || !(entity instanceof Player player) || player.isSpectator()
                || !(player.level() instanceof ServerLevel level) || !player.hasEffect(AntarchyObjects.STINKY_EFFECT.get())) {
            return;
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                AntarchySoundEvents.STINK_BUG_FART.get(),
                SoundSource.PLAYERS,
                0.85F,
                0.9F + level.random.nextFloat() * 0.2F
        );
    }
}
