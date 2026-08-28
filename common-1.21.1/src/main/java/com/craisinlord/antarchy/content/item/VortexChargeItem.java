package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.vortex.VortexChargeProjectileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class VortexChargeItem extends Item {
    private final Supplier<? extends EntityType<? extends VortexChargeProjectileEntity>> projectileType;

    public VortexChargeItem(Properties properties,
            Supplier<? extends EntityType<? extends VortexChargeProjectileEntity>> projectileType) {
        super(properties);
        this.projectileType = projectileType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemStack);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.6F,
                0.8F + level.getRandom().nextFloat() * 0.4F);

        if (!level.isClientSide) {
            VortexChargeProjectileEntity projectile = new VortexChargeProjectileEntity(projectileType.get(), player, level);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.consume(itemStack);
    }
}
