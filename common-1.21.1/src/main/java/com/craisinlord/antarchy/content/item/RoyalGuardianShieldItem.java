package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import java.util.Comparator;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class RoyalGuardianShieldItem extends ShieldItem {
    private static final int COMMAND_COOLDOWN_TICKS = 20 * 15;
    private static final int COMMAND_DURATION_TICKS = 20 * 30;
    private static final double COMMAND_RANGE = 16.0D;
    private final Supplier<Ingredient> repairIngredient;

    public RoyalGuardianShieldItem(Item.Properties properties, Supplier<Ingredient> repairIngredient) {
        super(properties);
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return this.repairIngredient.get().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalWeaponEnchantability();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_guardian_shield.command")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        Holder<MobEffect> commanded = RoyalEffectHooks.commandedHolder();
        if (commanded == null) {
            return InteractionResultHolder.fail(stack);
        }
        Mob target = level.getEntitiesOfClass(Mob.class,
                        new AABB(player.blockPosition()).inflate(COMMAND_RANGE),
                        mob -> RoyalEffectEligibility.canApplyCommanded(mob))
                .stream()
                .min(Comparator.comparingDouble((Mob mob) -> player.distanceToSqr(mob)).thenComparingInt(Mob::getId))
                .orElse(null);
        if (target == null || !target.addEffect(new MobEffectInstance(commanded, COMMAND_DURATION_TICKS), player)) {
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, COMMAND_COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }
}
