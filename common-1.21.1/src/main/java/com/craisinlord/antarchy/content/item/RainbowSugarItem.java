package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.ant.AntTeleportHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RainbowSugarItem extends Item {
    private static final String INFINITY_NAMESPACE = "infinity";
    private static final int NAUSEA_DURATION_TICKS = 100;
    private static final int POISON_DURATION_TICKS = 80;

    public RainbowSugarItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_DURATION_TICKS, 0));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0));

        if (!level.isClientSide && livingEntity instanceof ServerPlayer serverPlayer && isInInfinityDimension(serverPlayer.serverLevel())) {
            ServerLevel returnLevel = AntTeleportHelper.resolveReturnDestinationLevel(serverPlayer);
            var returnPos = AntTeleportHelper.getDestinationPosition(serverPlayer, returnLevel);
            serverPlayer.teleportTo(returnLevel, returnPos.x, returnPos.y, returnPos.z, serverPlayer.getYRot(), serverPlayer.getXRot());
            serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.rainbow_sugar").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static boolean isInInfinityDimension(ServerLevel level) {
        return INFINITY_NAMESPACE.equals(level.dimension().location().getNamespace());
    }
}
