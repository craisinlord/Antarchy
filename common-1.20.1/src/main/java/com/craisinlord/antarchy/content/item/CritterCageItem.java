package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.TagKey;

public class CritterCageItem extends Item {
    public static final String ENTITY_DATA_TAG = "EntityData";
    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("bosses"));
    private static final int EMPTY_STATE = 0;
    private static final int FILLED_PLAIN_STATE = 1;
    private static final int FILLED_COLORED_STATE = 2;

    public CritterCageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isFilled(stack)) {
            if (!level.isClientSide) {
                this.launchProjectile(level, player, hand, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        return this.releaseAtLookTarget(serverLevel, player, hand, stack)
                ? InteractionResultHolder.consume(player.getItemInHand(hand))
                : InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!this.isFilled(stack)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return this.placeBlock(context) ? InteractionResult.CONSUME : InteractionResult.FAIL;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        return this.releaseAtBlockHit(serverLevel, context)
                ? InteractionResult.CONSUME
                : InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!this.isFilled(stack)) {
            tooltip.add(Component.translatable("tooltip.antarchy.critter_cage.empty").withStyle(ChatFormatting.GRAY));
            super.appendHoverText(stack, level, tooltip, flag);
            return;
        }

        tooltip.add(Component.translatable("tooltip.antarchy.critter_cage.contains", this.getDisplayName(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.antarchy.critter_cage.release").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.antarchy.critter_cage.place").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public boolean isFilled(ItemStack stack) {
        return this.getEntityTypeId(stack) != null && this.getEntityData(stack) != null;
    }

    public int getItemState(ItemStack stack) {
        if (!this.isFilled(stack)) {
            return EMPTY_STATE;
        }
        return this.hasSpawnEggColors(stack) ? FILLED_COLORED_STATE : FILLED_PLAIN_STATE;
    }

    public ResourceLocation getEntityTypeId(ItemStack stack) {
        return AntarchyObjects.CRITTER_CAGE_ENTITY_TYPE_COMPONENT.get(stack);
    }

    public CompoundTag getEntityData(ItemStack stack) {
        CompoundTag tag = com.craisinlord.antarchy.content.component.AntarchyItemTag.getOrEmpty(stack);
        if (!tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(ENTITY_DATA_TAG).copy();
    }

    public int getPrimaryColor(ItemStack stack) {
        return AntarchyObjects.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.getOrDefault(stack, -1);
    }

    public int getSecondaryColor(ItemStack stack) {
        return AntarchyObjects.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.getOrDefault(stack, -1);
    }

    public Component getDisplayName(ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            return stack.getHoverName();
        }

        EntityType<?> type = this.getEntityType(stack);
        return type != null ? type.getDescription() : Component.literal("Unknown");
    }

    public EntityType<?> getEntityType(ItemStack stack) {
        ResourceLocation id = this.getEntityTypeId(stack);
        return id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }

    public static ItemStack createFilledStack(Entity entity) {
        ItemStack stack = new ItemStack(AntarchyObjects.CRITTER_CAGE.get());
        if (entity == null) {
            return stack;
        }

        EntityType<?> type = entity.getType();
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (typeId != null) {
            AntarchyObjects.CRITTER_CAGE_ENTITY_TYPE_COMPONENT.set(stack, typeId);
        }

        CompoundTag entityData = new CompoundTag();
        entity.saveWithoutId(entityData);
        com.craisinlord.antarchy.content.component.AntarchyItemTag.update(stack, tag -> tag.put(ENTITY_DATA_TAG, entityData));

        if (entity.hasCustomName()) {
            stack.setHoverName(entity.getCustomName());
        }

        applySpawnEggColors(stack, type);
        return stack;
    }

    public static boolean canCapture(Entity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player) {
            return false;
        }
        if (entity.getType().is(BOSS_TAG)) {
            return false;
        }
        if (entity.isPassenger() || entity.isVehicle() || !entity.getPassengers().isEmpty()) {
            return false;
        }
        if (entity.getType().is(AntarchyTags.Entities.CRITTER_CAGE_BLACKLIST)) {
            return false;
        }

        AABB box = entity.getBoundingBox();
        return box.getXsize() <= AntarchySettings.critterCageMaxCapturableWidth()
                && box.getZsize() <= AntarchySettings.critterCageMaxCapturableWidth()
                && box.getYsize() <= AntarchySettings.critterCageMaxCapturableHeight();
    }

    public static void applySpawnEggColors(ItemStack stack, EntityType<?> entityType) {
        SpawnEggItem egg = findSpawnEgg(entityType);
        if (egg == null) {
            AntarchyObjects.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.remove(stack);
            AntarchyObjects.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.remove(stack);
            return;
        }

        AntarchyObjects.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.set(stack, egg.getColor(0));
        AntarchyObjects.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.set(stack, egg.getColor(1));
    }

    private static SpawnEggItem findSpawnEgg(EntityType<?> entityType) {
        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) entityType;
        return livingType == null ? null : SpawnEggItem.byId(livingType);
    }

    private boolean releaseAtLookTarget(ServerLevel level, Player player, InteractionHand hand, ItemStack stack) {
        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        Vec3 target = hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult
                ? blockHitResult.getLocation().add(Vec3.atLowerCornerOf(blockHitResult.getDirection().getNormal()).scale(0.5D))
                : player.getEyePosition().add(player.getLookAngle().scale(2.0D));
        return this.releaseAtPosition(level, player, hand, stack, target);
    }

    private boolean releaseAtBlockHit(ServerLevel level, UseOnContext context) {
        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        Vec3 target = Vec3.atCenterOf(targetPos);
        return this.releaseAtPosition(level, context.getPlayer(), context.getHand(), context.getItemInHand(), target);
    }

    private boolean releaseAtPosition(ServerLevel level, Player player, InteractionHand hand, ItemStack stack, Vec3 position) {
        EntityType<?> type = this.getEntityType(stack);
        if (type == null) {
            return false;
        }

        if (!level.getWorldBorder().isWithinBounds(BlockPos.containing(position))) {
            return false;
        }

        CompoundTag entityData = this.getEntityData(stack);
        if (entityData == null) {
            return false;
        }

        CompoundTag savedData = entityData.copy();
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (entityId != null) {
            savedData.putString("id", entityId.toString());
        }

        Entity entity = EntityType.loadEntityRecursive(savedData, level, loaded -> loaded);
        if (entity == null) {
            return false;
        }

        entity.setUUID(java.util.UUID.randomUUID());
        entity.moveTo(position.x, position.y, position.z, player != null ? player.getYRot() : 0.0F, 0.0F);
        if (!level.noCollision(entity)) {
            return false;
        }

        level.addFreshEntity(entity);
        this.replaceFilledWithEmpty(player, hand, stack);
        return true;
    }

    private void replaceFilledWithEmpty(Player player, InteractionHand hand, ItemStack stack) {
        if (player != null && player.getAbilities().instabuild) {
            return;
        }

        ItemStack emptyCage = new ItemStack(AntarchyObjects.CRITTER_CAGE.get());
        if (stack.getCount() <= 1) {
            if (player != null) {
                player.setItemInHand(hand, emptyCage);
            }
            return;
        }

        stack.shrink(1);
        if (player == null || !player.getInventory().add(emptyCage)) {
            if (player != null) {
                player.drop(emptyCage, false);
            }
        }
    }

    private boolean placeBlock(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        if (!serverLevel.getBlockState(placePos).canBeReplaced()) {
            return false;
        }

        BlockState state = AntarchyObjects.CRITTER_CAGE_BLOCK.get().defaultBlockState();
        if (!serverLevel.setBlock(placePos, state, Block.UPDATE_ALL)) {
            return false;
        }

        BlockEntity blockEntity = serverLevel.getBlockEntity(placePos);
        if (!(blockEntity instanceof CritterCageBlockEntity critterCageBlockEntity)) {
            serverLevel.setBlock(placePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return false;
        }

        critterCageBlockEntity.loadFromItem(context.getItemInHand(), context.getLevel().registryAccess());
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return true;
    }

    private boolean hasSpawnEggColors(ItemStack stack) {
        return this.getPrimaryColor(stack) >= 0 && this.getSecondaryColor(stack) >= 0;
    }

    private void launchProjectile(Level level, Player player, InteractionHand hand, ItemStack stack) {
        var projectileType = AntarchyObjects.CRITTER_CAGE_PROJECTILE.get();
        var projectile = projectileType.create(level);
        com.craisinlord.antarchy.content.entity.CritterCageProjectileEntity critterCageProjectile = projectile;
        if (critterCageProjectile == null) {
            return;
        }

        critterCageProjectile.setOwner(player);
        critterCageProjectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        critterCageProjectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(critterCageProjectile);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
