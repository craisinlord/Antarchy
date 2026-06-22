package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.Item;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;

public class CorneaStalkBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<CorneaStalkBlock> CODEC = Block.simpleCodec(CorneaStalkBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    private static final int MAX_AGE = 3;
    private static final int RIPE_AGE = 2;
    private static final VoxelShape[] FLOOR_SHAPES = new VoxelShape[] {
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 7.0D, 11.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 13.0D, 13.0D),
            Block.box(2.5D, 0.0D, 2.5D, 13.5D, 14.5D, 13.5D)
    };
    private static final VoxelShape[] HANGING_SHAPES = new VoxelShape[] {
            Block.box(5.0D, 9.0D, 5.0D, 11.0D, 16.0D, 11.0D),
            Block.box(4.0D, 6.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(3.0D, 3.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.box(2.5D, 1.5D, 2.5D, 13.5D, 16.0D, 13.5D)
    };
    private static final ResourceLocation CORNEA_EAR_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "cornea_ear");

    public CorneaStalkBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(HANGING, false));
    }

    @Override
    public MapCodec<CorneaStalkBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPES[state.getValue(AGE)] : FLOOR_SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(AntarchyTags.Blocks.CORNEA_STALK_PLANTABLE);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = state.getValue(HANGING) ? pos.above() : pos.below();
        return this.mayPlaceOn(level.getBlockState(supportPos), level, supportPos);
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) {
            return;
        }

        BlockPos growthEndPos = state.getValue(HANGING) ? pos.below() : pos.above();
        if (level.getMaxLocalRawBrightness(growthEndPos) >= 9 && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && state.getValue(AGE) > 0 && entity instanceof LivingEntity livingEntity && !livingEntity.isSteppingCarefully()) {
            livingEntity.hurt(AntarchyDamageSources.corneaStalkPrick(level), 1.0F);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hitResult
    ) {
        if (!state.is(this) || state.getValue(AGE) < RIPE_AGE) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        if (!level.isClientSide) {
            this.dropCorneaFruit(level, pos, level.random);
            level.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_ALL);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return BuiltInRegistries.ITEM.getOptional(CORNEA_EAR_ID)
                .map(ItemStack::new)
                .orElseGet(() -> super.getCloneItemStack(level, pos, state));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, blockEntity, stack);
        var silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack) > 0) {
            return;
        }

        if (state.getValue(AGE) >= RIPE_AGE && level instanceof ServerLevel serverLevel) {
            this.dropCorneaFruit(serverLevel, pos, serverLevel.random);
        }
    }

    private void dropCorneaFruit(Level level, BlockPos pos, RandomSource random) {
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(CORNEA_EAR_ID);
        item.ifPresent(value -> Block.popResource(level, pos, new ItemStack(value, 1 + random.nextInt(2))));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HANGING);
    }
}
