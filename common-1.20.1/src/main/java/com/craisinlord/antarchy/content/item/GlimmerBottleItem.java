package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancements;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerCompanionSavedData;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GlimmerBottleItem extends Item {
    private static final String ABILITY_COOLDOWN_TAG = "AbilityCooldown";

    public GlimmerBottleItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack create(GlimmerVariant variant) {
        return create(variant, 0);
    }

    public static ItemStack create(GlimmerVariant variant, int abilityCooldown) {
        ItemStack stack = new ItemStack(AntarchyObjects.GLIMMER_BOTTLE.get());
        AntarchyObjects.GLIMMER_VARIANT_COMPONENT.set(stack, variant);
        if (abilityCooldown > 0) {
            com.craisinlord.antarchy.content.component.AntarchyItemTag.update(stack, tag -> tag.putInt(ABILITY_COOLDOWN_TAG, abilityCooldown));
        }
        return stack;
    }

    @Nullable
    public static GlimmerVariant getVariant(ItemStack stack) {
        return AntarchyObjects.GLIMMER_VARIANT_COMPONENT.get(stack);
    }

    public static int getStoredAbilityCooldown(ItemStack stack) {
        return com.craisinlord.antarchy.content.component.AntarchyItemTag.getOrEmpty(stack).getInt(ABILITY_COOLDOWN_TAG);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        GlimmerVariant variant = getVariant(stack);
        if (variant == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }

        GlimmerEntity glimmer = AntarchyObjects.GLIMMER.get().create(level);
        if (glimmer == null) {
            return InteractionResultHolder.fail(stack);
        }

        glimmer.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        glimmer.lockVariant(variant);
        glimmer.tame(player);
        glimmer.setOrderedToSit(false);
        glimmer.setAbilityCooldown(getStoredAbilityCooldown(stack));
        level.addFreshEntity(glimmer);
        GlimmerCompanionSavedData.replaceCompanion(((ServerLevel) level).getServer(), player.getUUID(), glimmer.getUUID());
        GlimmerParticles.tickBurst(glimmer);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AntarchyAdvancements.award(serverPlayer, variant.tameAdvancementId());
        }

        ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
        return InteractionResultHolder.success(result);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        GlimmerVariant variant = getVariant(stack);
        if (variant != null) {
            tooltip.add(Component.translatable("item.antarchy.glimmer_bottle.contains", variant.displayName()));
            String key = variant.name().toLowerCase(java.util.Locale.ROOT);
            tooltip.add(Component.translatable("item.antarchy.glimmer_bottle.passive." + key).withStyle(net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.antarchy.glimmer_bottle.ability." + key).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
    }
}
