package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.block.BedBugEggBlock;
import com.craisinlord.antarchy.content.block.CreepingHorrorEggBlock;
import com.craisinlord.antarchy.content.block.JerryEggBlock;
import com.craisinlord.antarchy.content.block.JumpyBugEggBlock;
import com.craisinlord.antarchy.content.block.LurkingTerrorEggBlock;
import com.craisinlord.antarchy.content.block.SpitBugEggBlock;
import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;

public class JerryNucleusItem extends Item {
    public JerryNucleusItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Block block = state.getBlock();
        boolean handled = block instanceof BedBugEggBlock
                || block instanceof CreepingHorrorEggBlock
                || block instanceof LurkingTerrorEggBlock
                || block instanceof JumpyBugEggBlock
                || block instanceof SpitBugEggBlock
                || block instanceof JerryEggBlock
                || block instanceof TurtleEggBlock;
        if (!handled) {
            return super.useOn(context);
        }

        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        if (block instanceof BedBugEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos, state);
        } else if (block instanceof CreepingHorrorEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos, state);
        } else if (block instanceof LurkingTerrorEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos, state);
        } else if (block instanceof JumpyBugEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos);
        } else if (block instanceof SpitBugEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos);
        } else if (block instanceof JerryEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos);
        } else if (block instanceof TurtleEggBlock) {
            hatchTurtleEggs(serverLevel, pos, state);
        }

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private static void hatchTurtleEggs(ServerLevel level, BlockPos pos, BlockState state) {
        int eggs = state.getValue(TurtleEggBlock.EGGS);
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.75F, 0.95F + level.random.nextFloat() * 0.1F);

        for (int i = 0; i < eggs; i++) {
            Turtle turtle = EntityType.TURTLE.create(level);
            if (turtle == null) {
                continue;
            }
            turtle.setAge(-24000);
            turtle.setHomePos(pos);
            double spawnX = pos.getX() + 0.3D + level.random.nextDouble() * 0.4D;
            double spawnZ = pos.getZ() + 0.3D + level.random.nextDouble() * 0.4D;
            turtle.moveTo(spawnX, pos.getY() + 0.1D, spawnZ, level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(turtle);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.jerry_nucleus.hatch").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
