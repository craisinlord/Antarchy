package com.craisinlord.antarchy.neoforge.mixins.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.util.VillagerUndergroundCabinMapTrade", remap = false)
public abstract class AlexsCavesCabinMapTradeThreadMixin {
    @Inject(method = "getOffer", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void antarchy$onlyOnServerThread(Entity trader, RandomSource random,
                                             CallbackInfoReturnable<MerchantOffer> cir) {
        MinecraftServer server = trader.level().getServer();
        if (server == null || !server.isSameThread()) {
            cir.setReturnValue(null);
        }
    }
}
