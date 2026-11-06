package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DreamCampfireBlock extends CampfireBlock {
    public DreamCampfireBlock(BlockBehaviour.Properties properties) {
        super(false, 2, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DreamCampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        BlockEntityType<DreamCampfireBlockEntity> dreamCampfireType = AntarchyObjects.DREAM_CAMPFIRE_BLOCK_ENTITY.get();
        if (!(level instanceof ServerLevel serverLevel)) {
            return state.getValue(LIT)
                    ? createTickerHelper(blockEntityType, dreamCampfireType, CampfireBlockEntity::particleTick)
                    : null;
        }
        if (state.getValue(LIT)) {
            RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
            return createTickerHelper(blockEntityType, dreamCampfireType,
                    (tickLevel, tickPos, tickState, campfire) -> CampfireBlockEntity.cookTick(serverLevel, tickPos, tickState, campfire, quickCheck));
        }
        return createTickerHelper(blockEntityType, dreamCampfireType, CampfireBlockEntity::cooldownTick);
    }
}
