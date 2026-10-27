package com.craisinlord.antarchy.content.item;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.InteractionResult;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class LucidPearlItem extends Item {

    private final Supplier<? extends EntityType<? extends LucidEyeProjectileEntity>> projectileType;

    public LucidPearlItem(Properties properties,
                          Supplier<? extends EntityType<? extends LucidEyeProjectileEntity>> projectileType) {
        super(properties);
        this.projectileType = projectileType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.FAIL;
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide()) {
            LucidEyeProjectileEntity projectile = new LucidEyeProjectileEntity(
                    projectileType.get(), player, level);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this.getDefaultInstance(), 20);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.lucid_pearl").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.lucid_pearl.detail").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
