package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.time.TemporalTunerAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public final class TemporalTunerItem extends Item {
    public static final double STEP = 0.1D;
    private static final String RATE_TAG = "TemporalTunerRate";

    public TemporalTunerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isAvailable(Player player) {
        return player.getInventory().contains(stack -> stack.getItem() instanceof TemporalTunerItem)
                && (player.getMainHandItem().getItem() instanceof TemporalTunerItem
                || player.getOffhandItem().getItem() instanceof TemporalTunerItem
                || hasHotbarTuner(player));
    }

    private static boolean hasHotbarTuner(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof TemporalTunerItem) {
                return true;
            }
        }
        return false;
    }

    public static double clampRate(double rate) {
        return Math.max(AntarchySettings.temporalTunerMinRate(), Math.min(AntarchySettings.temporalTunerMaxRate(), rate));
    }

    public static void adjust(Player player, double delta) {
        if (!(player instanceof TemporalTunerAccess access) || !isAvailable(player)) {
            return;
        }
        double previousRate = getRate(player);
        double updatedRate = clampRate(previousRate + delta);
        access.antarchy$setTemporalTunerRate(updatedRate);
        if (Math.abs(updatedRate - previousRate) >= 0.001D) {
            player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 0.6F, 1.0F);
        }
        syncTooltipRate(player, getRate(player));
    }

    public static double getRate(Player player) {
        return player instanceof TemporalTunerAccess access ? clampRate(access.antarchy$getTemporalTunerRate()) : 1.0D;
    }

    public static void syncTooltipRate(Player player, double rate) {
        for (int slot = 0; slot < 9; slot++) {
            updateStackRate(player.getInventory().getItem(slot), rate);
        }
        updateStackRate(player.getOffhandItem(), rate);
    }

    private static void updateStackRate(ItemStack stack, double rate) {
        if (stack.getItem() instanceof TemporalTunerItem) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble(RATE_TAG, rate));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.description").withStyle(ChatFormatting.RED));
        double rate = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble(RATE_TAG);
        if (rate <= 0.0D) {
            rate = 1.0D;
        }
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.speed", (int) Math.round(rate * 100.0D)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.controls", (int) (AntarchySettings.temporalTunerMinRate() * 100), (int) (AntarchySettings.temporalTunerMaxRate() * 100)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.availability").withStyle(ChatFormatting.RED));
    }
}
