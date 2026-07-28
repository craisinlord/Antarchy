package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.portal.PermanentPortalManager;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelPortalIgnitionMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void antarchy$ignitePermanentPortal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!AntarchySettings.permanentPortalsEnabled() || !AntarchySettings.permanentPortalsFlintAndSteelEnabled()) {
            return;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        for (PermanentPortalType type : PermanentPortalType.values()) {
            if (!type.isEnabled() || !clickedState.is(type.frameTag())) {
                continue;
            }
            if (!PermanentPortalManager.tryIgnitePortal(level, clickedPos, type)) {
                continue;
            }

            if (!level.isClientSide() && context.getPlayer() != null) {
                ItemStack stack = context.getItemInHand();
                stack.hurtAndBreak(1, context.getPlayer(), handToSlot(context.getHand()));
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
            return;
        }
    }

    private static EquipmentSlot handToSlot(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
}
