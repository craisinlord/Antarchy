package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class CranberryLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final BooleanProperty FRUITING = BooleanProperty.create("fruiting");

    public CranberryLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FRUITING, false));
    }

@Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRUITING);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            super.randomTick(state, level, pos, random);
        } else if (!state.getValue(FRUITING) && level.isDay() && level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(20) == 0) {
            level.setBlock(pos, state.setValue(FRUITING, true), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!state.getValue(FRUITING)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("antarchy", "cranberry")), 2 + level.random.nextInt(3)));
            level.setBlock(pos, state.setValue(FRUITING, false), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (stack.is(Items.SHEARS)) {
                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(hand));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return !state.getValue(FRUITING) && !decaying(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isValidBonemealTarget(level, pos, state, false)) {
            level.setBlock(pos, state.setValue(FRUITING, true), Block.UPDATE_CLIENTS);
        }
    }
}
