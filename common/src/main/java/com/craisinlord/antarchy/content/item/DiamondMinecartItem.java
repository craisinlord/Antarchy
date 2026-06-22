package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import java.util.function.Supplier;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class DiamondMinecartItem extends Item {
    private final Supplier<EntityType<DiamondMinecartEntity>> entityTypeSupplier;

    public DiamondMinecartItem(Supplier<EntityType<DiamondMinecartEntity>> entityTypeSupplier, Properties properties) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!BaseRailBlock.isRail(clickedState)) {
            return InteractionResult.FAIL;
        }
        BlockPos cartPos = clickedPos;

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        BlockState railState = serverLevel.getBlockState(cartPos);
        RailShape railShape = railState.getBlock() instanceof BaseRailBlock railBlock
                ? railState.getValue(railBlock.getShapeProperty())
                : RailShape.NORTH_SOUTH;
        double yOffset = railShape.isAscending() ? 0.5625D : 0.0625D;

        DiamondMinecartEntity minecart = new DiamondMinecartEntity(
                this.entityTypeSupplier.get(),
                serverLevel,
                cartPos.getX() + 0.5D,
                cartPos.getY() + yOffset,
                cartPos.getZ() + 0.5D,
                this
        );
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            minecart.setCustomName(stack.getHoverName());
        }

        serverLevel.addFreshEntity(minecart);
        level.gameEvent(GameEvent.ENTITY_PLACE, cartPos, GameEvent.Context.of(player, railState));
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
