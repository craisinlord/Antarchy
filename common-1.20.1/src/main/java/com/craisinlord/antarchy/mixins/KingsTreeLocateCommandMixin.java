package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGrid;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocateCommand.class)
public abstract class KingsTreeLocateCommandMixin {
    @Inject(method = "locateStructure", at = @At("HEAD"), cancellable = true)
    private static void antarchy$locateKingsTree(CommandSourceStack source,
            ResourceOrTagKeyArgument.Result<Structure> requestedStructure,
            CallbackInfoReturnable<Integer> callback) {
        boolean isKingsTree = requestedStructure.unwrap().map(KingsTreeGrid.STRUCTURE_KEY::equals, tag -> false);
        if (!isKingsTree || !source.getLevel().dimension().equals(KingsTreeGrid.ELYTHIA)) return;
        BlockPos origin = BlockPos.containing(source.getPosition());
        BlockPos target = KingsTreeGrid.nearestTreeCenter(origin);
        Holder.Reference<Structure> holder = source.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getHolderOrThrow(KingsTreeGrid.STRUCTURE_KEY);
        callback.setReturnValue(LocateCommand.showLocateResult(source, requestedStructure, origin,
                Pair.of(target, holder), "commands.locate.structure.success", false, Duration.ZERO));
    }
}
