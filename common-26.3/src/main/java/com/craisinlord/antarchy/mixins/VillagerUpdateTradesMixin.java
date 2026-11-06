package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.trades.ComputerScientistTradeManager;
import com.craisinlord.antarchy.content.entity.trades.PestControlTradeManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerUpdateTradesMixin {
    @Unique
    private static final ResourceKey<VillagerProfession> ANTARCHY$COMPUTER_SCIENTIST =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.fromNamespaceAndPath(Antarchy.MODID, "computer_scientist"));
    @Unique
    private static final ResourceKey<VillagerProfession> ANTARCHY$PEST_CONTROL =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.fromNamespaceAndPath(Antarchy.MODID, "pest_control"));

    @Inject(method = "updateTrades(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
    private void antarchy$addAntarchyProfessionTrades(ServerLevel level, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        VillagerData data = self.getVillagerData();
        Holder<VillagerProfession> profession = data.profession();
        if (profession.is(ANTARCHY$COMPUTER_SCIENTIST)) {
            ComputerScientistTradeManager.addOffers(self, data.level(), self.getRandom(), self.getOffers());
        } else if (profession.is(ANTARCHY$PEST_CONTROL)) {
            PestControlTradeManager.addOffers(self, data.level(), self.getRandom(), self.getOffers());
        }
    }
}
