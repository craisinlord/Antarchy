package com.craisinlord.antarchy.neoforge.mixins.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
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
public abstract class BucketItemThoraxisWaterMixin {
    private static final ResourceLocation THORAXIS_DIMENSION_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");

    @Shadow
    @Final
    public Fluid content;

    @Shadow
    protected abstract void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos);

    @Inject(
            method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/FluidType;isVaporizedOnPlacement(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/neoforged/neoforge/fluids/FluidStack;)Z"
            ),
            cancellable = true
    )
    private void antarchy$replaceWaterVaporizeCheckWithAntiwater(
            @Nullable Player player,
            Level level,
            BlockPos pos,
            @Nullable BlockHitResult hitResult,
            @Nullable ItemStack container,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.content != Fluids.WATER) {
            return;
        }
        if (!level.dimension().location().equals(THORAXIS_DIMENSION_ID)) {
            return;
        }
        if (container == null) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        boolean canReplace = state.canBeReplaced(this.content);

        if (!state.isAir() && !canReplace && !state.getFluidState().isSource()) {
            cir.setReturnValue(false);
            return;
        }

        if (!level.isClientSide) {
            if (canReplace && !state.liquid()) {
                level.destroyBlock(pos, true);
            }
        }

        boolean placed = level.setBlock(pos, AntarchyNeoforgeMisc.ANTIWATER.get().defaultFluidState().createLegacyBlock(), 11);
        if (placed || state.getFluidState().isSource()) {
            this.playEmptySound(player, level, pos);
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(false);
    }
}
