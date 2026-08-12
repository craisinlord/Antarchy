package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.AbstractAntimetalRailBlock;
import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartItem.class)
public abstract class MinecartItemMixin {
    @Shadow
    @Final
    AbstractMinecart.Type type;

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void antarchy$placeOnAntimetalRail(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractAntimetalRailBlock)) {
            return;
        }
        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        double x = pos.getX() + 0.5D;
        double y = AntimetalRailHelper.attachY(pos.getY(), 0);
        double z = pos.getZ() + 0.5D;
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        AbstractMinecart cart = AbstractMinecart.createMinecart(serverLevel, x, y, z, this.type);
        if (cart != null) {
            if (stack.hasCustomHoverName()) {
                cart.setCustomName(stack.getHoverName());
            }
            serverLevel.addFreshEntity(cart);
            serverLevel.gameEvent(GameEvent.ENTITY_PLACE, pos, GameEvent.Context.of(player));
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
    }
}
