package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class ThoraxisWaterBucketMixin {
    private static final Identifier THORAXIS_DIMENSION_ID =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");

    @Shadow
    @Final
    public Fluid content;

    @Shadow
    protected abstract void playEmptySound(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos);

    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    private void antarchy$replaceWaterWithAntiwaterInThoraxis(
            @Nullable LivingEntity player,
            Level level,
            BlockPos pos,
            @Nullable BlockHitResult hitResult,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.content != Fluids.WATER || !level.dimension().identifier().equals(THORAXIS_DIMENSION_ID)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        boolean canReplace = state.canBeReplaced(this.content);

        if (!state.isAir() && !canReplace && !state.getFluidState().isSource()) {
            cir.setReturnValue(false);
            return;
        }

        if (!level.isClientSide() && canReplace && !state.liquid()) {
            level.destroyBlock(pos, true);
        }

        boolean placed = level.setBlock(
                pos,
                AntarchyFabricMisc.ANTIWATER.get().defaultFluidState().createLegacyBlock(),
                11
        );
        if (placed || state.getFluidState().isSource()) {
            this.playEmptySound(player, level, pos);
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(false);
    }
}
