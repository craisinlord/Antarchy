package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.context.BlockPlaceContext;

public class AntNestBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final Supplier<List<EntityType<? extends BaseAntEntity>>> antTypesSupplier;
    private final Supplier<? extends BlockEntityType<AntNestBlockEntity>> blockEntityTypeSupplier;
    private final int maxOccupants;
    private final boolean populateInitialAnts;
    private final boolean damageSteppers;
    private final MapCodec<AntNestBlock> codec;

    public AntNestBlock(
            Supplier<? extends EntityType<? extends BaseAntEntity>> antTypeSupplier,
            Supplier<? extends BlockEntityType<AntNestBlockEntity>> blockEntityTypeSupplier,
            BlockBehaviour.Properties properties
    ) {
        this(() -> List.of(antTypeSupplier.get()), blockEntityTypeSupplier, 7, true, false, properties);
    }

    public AntNestBlock(
            Supplier<? extends EntityType<? extends BaseAntEntity>> antTypeSupplier,
            Supplier<? extends BlockEntityType<AntNestBlockEntity>> blockEntityTypeSupplier,
            boolean damageSteppers,
            BlockBehaviour.Properties properties
    ) {
        this(() -> List.of(antTypeSupplier.get()), blockEntityTypeSupplier, 7, true, damageSteppers, properties);
    }

    public AntNestBlock(
            Supplier<List<EntityType<? extends BaseAntEntity>>> antTypesSupplier,
            Supplier<? extends BlockEntityType<AntNestBlockEntity>> blockEntityTypeSupplier,
            int maxOccupants,
            boolean populateInitialAnts,
            boolean damageSteppers,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.antTypesSupplier = antTypesSupplier;
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.maxOccupants = maxOccupants;
        this.populateInitialAnts = populateInitialAnts;
        this.damageSteppers = damageSteppers;
        this.codec = Block.simpleCodec(ignored -> this);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<AntNestBlock> codec() {
        return this.codec;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AntNestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(),
                (tickLevel, tickPos, tickState, nest) -> {
                    if (tickLevel instanceof ServerLevel serverLevel) {
                        AntNestBlockEntity.serverTick(serverLevel, tickPos, tickState, nest);
                    }
                });
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && this.populateInitialAnts && level.getBlockEntity(pos) instanceof AntNestBlockEntity nestBlockEntity) {
            nestBlockEntity.populateInitialAnts((ServerLevel) level);
        }
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level instanceof Level actualLevel && actualLevel.getBlockEntity(pos) instanceof AntNestBlockEntity nestBlockEntity) {
            BlockItem.setBlockEntityData(stack, AntarchyObjects.ANT_NEST_BLOCK_ENTITY.get(), nestBlockEntity.saveWithoutMetadata(actualLevel.registryAccess()));
        }
        return stack;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        var silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack) > 0;
        ItemStack droppedNest = hasSilkTouch ? this.getCloneItemStack(level, pos, state) : ItemStack.EMPTY;

        super.playerDestroy(level, player, pos, state, blockEntity, stack);

        if (hasSilkTouch) {
            if (!droppedNest.isEmpty() && level instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, pos, droppedNest);
            }
            return;
        }

        if (blockEntity instanceof AntNestBlockEntity nestBlockEntity && level instanceof ServerLevel serverLevel) {
            nestBlockEntity.releaseAll(serverLevel);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (this.damageSteppers && entity instanceof LivingEntity && !entity.isSteppingCarefully()) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }

        super.stepOn(level, pos, state, entity);
    }

    public boolean canAccept(EntityType<?> antType) {
        for (EntityType<? extends BaseAntEntity> supportedType : this.antTypes()) {
            if (supportedType == antType) {
                return true;
            }
        }
        return false;
    }

    public int maxOccupants() {
        return this.maxOccupants;
    }

    public boolean shouldPopulateInitialAnts() {
        return this.populateInitialAnts;
    }

    public EntityType<? extends BaseAntEntity> initialAntType(RandomSource random) {
        List<EntityType<? extends BaseAntEntity>> antTypes = this.antTypes();
        if (antTypes.isEmpty()) {
            throw new IllegalStateException("Ant nest block must declare at least one supported ant type");
        }
        return antTypes.get(random.nextInt(antTypes.size()));
    }

    private List<EntityType<? extends BaseAntEntity>> antTypes() {
        return this.antTypesSupplier.get();
    }
}
