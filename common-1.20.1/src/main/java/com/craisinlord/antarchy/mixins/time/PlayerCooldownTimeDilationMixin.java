package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerCooldownTimeDilationMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;tick()V"))
    private void antarchy$advanceCooldowns(ItemCooldowns cooldowns) {
        Player player = (Player) (Object) this;
        if (TimeDilationApi.consumeTick(player, "player_item_cooldowns")) {
            cooldowns.tick();
        }
    }
}
