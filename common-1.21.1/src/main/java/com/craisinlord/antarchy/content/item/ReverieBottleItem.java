package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ReverieBottleItem extends Item {
    private static final String STORED_REVERIE_TAG = "StoredReverie";

    public ReverieBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        this.writeDefaultReverieData(stack);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        this.ensureStoredReverie(stack);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!this.releaseReverie(serverLevel, player, stack, hand)) {
            return InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        this.ensureStoredReverie(stack);

        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        return this.releaseReverie(serverLevel, player, stack, context.getHand())
                ? InteractionResult.CONSUME
                : InteractionResult.FAIL;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        this.ensureStoredReverie(stack);

        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        return this.releaseReverie(serverLevel, player, stack, hand)
                ? InteractionResult.CONSUME
                : InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.reverie_bottle.capture").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.reverie_bottle.release").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private boolean releaseReverie(ServerLevel serverLevel, Player player, ItemStack stack, InteractionHand hand) {
        CompoundTag entityTag = this.getStoredReverieTag(stack);
        if (entityTag == null) {
            return false;
        }

        ReverieEntity reverie = AntarchyObjects.REVERIE.get().create(serverLevel);
        if (reverie == null) {
            return false;
        }

        reverie.readAdditionalSaveData(entityTag);
        Vec3 spawnPos = player.position().add(player.getLookAngle().scale(1.25D)).add(0.0D, 0.15D, 0.0D);
        reverie.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), player.getXRot());
        reverie.setDeltaMovement(player.getDeltaMovement().scale(0.25D));
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            reverie.setCustomName(stack.getHoverName());
        }

        serverLevel.addFreshEntity(reverie);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        this.clearStoredReverie(stack);
        this.returnGlassBottle(player, stack, hand);
        return true;
    }

    private void returnGlassBottle(Player player, ItemStack stack, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
            return;
        }

        ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.getCount() <= 1) {
            player.setItemInHand(hand, glassBottle);
            return;
        }

        stack.shrink(1);
        if (!player.getInventory().add(glassBottle)) {
            player.drop(glassBottle, false);
        }
    }

    private boolean hasStoredReverie(ItemStack stack) {
        return this.getStoredReverieTag(stack) != null;
    }

    private CompoundTag getStoredReverieTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(STORED_REVERIE_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(STORED_REVERIE_TAG).copy();
    }

    private void ensureStoredReverie(ItemStack stack) {
        if (this.hasStoredReverie(stack)) {
            return;
        }

        this.writeDefaultReverieData(stack);
    }

    private void clearStoredReverie(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(STORED_REVERIE_TAG));
    }

    private void writeDefaultReverieData(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (!tag.contains(STORED_REVERIE_TAG, Tag.TAG_COMPOUND)) {
                tag.put(STORED_REVERIE_TAG, new CompoundTag());
            }
        });
    }
}
