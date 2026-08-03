package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancements;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerCompanionSavedData;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import java.util.List;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GlimmerBottleItem extends Item {
    private static final String ABILITY_COOLDOWN_TAG = "AbilityCooldown";
    private static final String SHEAR_COOLDOWN_TAG = "ShearCooldown";

    public GlimmerBottleItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack create(GlimmerVariant variant) {
        return create(variant, 0, 0);
    }

    public static ItemStack create(GlimmerVariant variant, int abilityCooldown) {
        return create(variant, abilityCooldown, 0);
    }

    public static ItemStack create(GlimmerVariant variant, int abilityCooldown, int shearCooldown) {
        ItemStack stack = new ItemStack(AntarchyObjects.GLIMMER_BOTTLE.get());
        stack.set(AntarchyObjects.GLIMMER_VARIANT_COMPONENT.get(), variant);
        if (abilityCooldown > 0 || shearCooldown > 0) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                if (abilityCooldown > 0) {
                    tag.putInt(ABILITY_COOLDOWN_TAG, abilityCooldown);
                }
                if (shearCooldown > 0) {
                    tag.putInt(SHEAR_COOLDOWN_TAG, shearCooldown);
                }
            });
        }
        return stack;
    }

    @Nullable
    public static GlimmerVariant getVariant(ItemStack stack) {
        return stack.get(AntarchyObjects.GLIMMER_VARIANT_COMPONENT.get());
    }

    public static int getStoredAbilityCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(ABILITY_COOLDOWN_TAG);
    }

    public static int getStoredShearCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(SHEAR_COOLDOWN_TAG);
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
        glimmer.startShearCooldown(getStoredShearCooldown(stack));
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        GlimmerVariant variant = getVariant(stack);
        if (variant != null) {
            tooltip.add(Component.translatable("item.antarchy.glimmer_bottle.contains", variant.displayName()));
            String key = variant.name().toLowerCase(java.util.Locale.ROOT);
            tooltip.add(Component.translatable("item.antarchy.glimmer_bottle.passive." + key).withStyle(net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.antarchy.glimmer_bottle.ability." + key).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
    }
}
