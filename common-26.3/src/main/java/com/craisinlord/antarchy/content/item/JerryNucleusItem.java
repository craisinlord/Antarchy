package com.craisinlord.antarchy.content.item;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.craisinlord.antarchy.content.block.BedBugEggBlock;
import com.craisinlord.antarchy.content.block.CrawlingBlightEggBlock;
import com.craisinlord.antarchy.content.block.JerryEggBlock;
import com.craisinlord.antarchy.content.block.SpringbugEggBlock;
import com.craisinlord.antarchy.content.block.SkulkingFrightEggBlock;
import com.craisinlord.antarchy.content.block.SpitBugEggBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.turtle.Turtle;
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
                || block instanceof CrawlingBlightEggBlock
                || block instanceof SkulkingFrightEggBlock
                || block instanceof SpringbugEggBlock
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
        } else if (block instanceof CrawlingBlightEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos, state);
        } else if (block instanceof SkulkingFrightEggBlock eggBlock) {
            eggBlock.hatchWithNucleus(serverLevel, pos, state);
        } else if (block instanceof SpringbugEggBlock eggBlock) {
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
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.75F, 0.95F + level.getRandom().nextFloat() * 0.1F);

        for (int i = 0; i < eggs; i++) {
            Turtle turtle = EntityTypes.TURTLE.create(level, EntitySpawnReason.BREEDING);
            if (turtle == null) {
                continue;
            }
            turtle.setAge(-24000);
            turtle.setHomePos(pos);
            double spawnX = pos.getX() + 0.3D + level.getRandom().nextDouble() * 0.4D;
            double spawnZ = pos.getZ() + 0.3D + level.getRandom().nextDouble() * 0.4D;
            turtle.snapTo(spawnX, pos.getY() + 0.1D, spawnZ, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(turtle);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.jerry_nucleus.hatch").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipComponents, tooltipFlag);
    }
}
