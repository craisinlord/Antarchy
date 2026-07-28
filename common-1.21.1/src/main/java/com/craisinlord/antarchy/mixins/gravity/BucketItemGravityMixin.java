package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.advancements.CriteriaTriggers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class BucketItemGravityMixin {
    @Shadow
    @Final
    private Fluid content;

    @Shadow
    public abstract boolean emptyContents(Player player, Level level, BlockPos pos, BlockHitResult hitResult);

    @Shadow
    public abstract void checkExtraContent(Player player, Level level, ItemStack stack, BlockPos pos);

    @Inject(method = "use", at = @At("RETURN"), cancellable = true)
    private void antarchy$allowInvertedFluidPlacement(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        InteractionResultHolder<ItemStack> result = cir.getReturnValue();
        if (result.getResult().consumesAction() || this.content == Fluids.EMPTY || !AntarchyGravityApi.isGravityInverted(player)) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        HitResult hitResult = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Direction face = blockHitResult.getDirection();
        if (face.getAxis() != Direction.Axis.Y) {
            return;
        }

        BlockPos clickedPos = blockHitResult.getBlockPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Block clickedBlock = clickedState.getBlock();
        BlockPos normalTargetPos = clickedBlock instanceof LiquidBlockContainer && this.content == Fluids.WATER
                ? clickedPos
                : clickedPos.relative(face);
        BlockPos invertedTargetPos = clickedPos.relative(face.getOpposite());
        if (invertedTargetPos.equals(normalTargetPos)) {
            return;
        }

        if (!level.mayInteract(player, clickedPos) || !player.mayUseItemAt(invertedTargetPos, face.getOpposite(), stack)) {
            return;
        }

        if (!this.emptyContents(player, level, invertedTargetPos, null)) {
            return;
        }

        this.checkExtraContent(player, level, stack, invertedTargetPos);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, invertedTargetPos, stack);
        }
        player.awardStat(Stats.ITEM_USED.get((BucketItem) (Object) this));
        ItemStack successStack = ItemUtils.createFilledResult(stack, player, BucketItem.getEmptySuccessItem(stack, player));
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(successStack, level.isClientSide()));
    }
}
