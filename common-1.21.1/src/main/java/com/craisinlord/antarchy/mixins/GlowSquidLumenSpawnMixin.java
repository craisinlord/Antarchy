package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowSquid.class)
public abstract class GlowSquidLumenSpawnMixin {
    @Inject(method = "checkGlowSquidSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void antarchy$allowGlowSquidsInLumen(
            EntityType<? extends LivingEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (pos.getY() > level.getSeaLevel() - 33 || level.getRawBrightness(pos, 0) != 0) {
            return;
        }

        if (level.getBlockState(pos).is(Blocks.WATER) || level.getFluidState(pos).is(AntarchyTags.Fluids.LUMEN)) {
            cir.setReturnValue(true);
        }
    }
}
