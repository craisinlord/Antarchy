package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class BloodCrystalShardItem extends Item {
    public static Consumer<ServerPlayer> SYNC_BLOODGLASS = p -> {};

    private static final int COOLDOWN_TICKS = 60;

    public BloodCrystalShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        BloodglassAccess access = (BloodglassAccess) player;
        int armorLost = access.antarchy$getArmorShieldLostCount();
        int appleLost = access.antarchy$getAppleShieldLostCount();

        if (armorLost <= 0 && appleLost <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        // Restore armor shields first, then apple shields
        if (armorLost > 0) {
            access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() + 1);
            access.antarchy$setArmorShieldLostCount(armorLost - 1);
            if (armorLost - 1 == 0) {
                access.antarchy$setArmorRechargeTimer(0);
            }
        } else {
            access.antarchy$setAppleShieldsActive(access.antarchy$getAppleShieldsActive() + 1);
            access.antarchy$setAppleShieldLostCount(appleLost - 1);
            if (appleLost - 1 == 0) {
                access.antarchy$setAppleRechargeTimer(0);
            }
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        SYNC_BLOODGLASS.accept((ServerPlayer) player);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}
