package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.craisinlord.antarchy.content.component.AntarchyItemTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public final class TemporalTunerItem extends Item {
    public static final double STEP = 0.1D;
    private static final String RATE_TAG = "TemporalTunerRate";

    public TemporalTunerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isAvailable(Player player) {
        return hasTunerAnywhere(player)
                && (player.getMainHandItem().getItem() instanceof TemporalTunerItem
                || player.getOffhandItem().getItem() instanceof TemporalTunerItem
                || hasHotbarTuner(player));
    }

    private static boolean hasTunerAnywhere(Player player) {
        for (java.util.List<ItemStack> compartment : java.util.List.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand)) {
            for (ItemStack stack : compartment) {
                if (!stack.isEmpty() && stack.getItem() instanceof TemporalTunerItem) {
                    return true;
                }
            }
        }
        return false;
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
            AntarchyItemTag.update(stack, tag -> tag.putDouble(RATE_TAG, rate));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.description").withStyle(ChatFormatting.RED));
        double rate = AntarchyItemTag.getOrEmpty(stack).getDouble(RATE_TAG);
        if (rate <= 0.0D) {
            rate = 1.0D;
        }
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.speed", (int) Math.round(rate * 100.0D)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.controls", (int) (AntarchySettings.temporalTunerMinRate() * 100), (int) (AntarchySettings.temporalTunerMaxRate() * 100)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.antarchy.temporal_tuner.availability").withStyle(ChatFormatting.RED));
    }
}
