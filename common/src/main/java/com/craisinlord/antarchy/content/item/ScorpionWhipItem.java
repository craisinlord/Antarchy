package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public final class ScorpionWhipItem extends SwordItem {
    private static final ResourceLocation ATTACK_RANGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "scorpion_whip_attack_range");
    private final Tier tier;

    public ScorpionWhipItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
        this.tier = tier;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(
                this.tier,
                (int) Math.round(AntarchySettings.scorpionWhipBaseDamage()),
                -2.6F
        ).withModifierAdded(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ATTACK_RANGE_MODIFIER_ID, AntarchySettings.scorpionWhipReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean pulled = ScorpionWhipTetherManager.pullAndDetach(serverPlayer);
        return pulled ? InteractionResultHolder.sidedSuccess(stack, false) : InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean didHurt = super.hurtEnemy(stack, target, attacker);
        if (!didHurt || target.level().isClientSide() || !(attacker instanceof ServerPlayer serverPlayer)) {
            return didHurt;
        }

        target.addEffect(new MobEffectInstance(MobEffects.POISON, AntarchySettings.scorpionWhipPoisonDurationTicks(), 0));
        if (!ScorpionWhipTetherManager.hasTether(serverPlayer) && ScorpionWhipTetherManager.canTether(target)) {
            ScorpionWhipTetherManager.attach(serverPlayer, target);
            ServerLevel level = serverPlayer.serverLevel();
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5D), target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.0D);
            level.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.8F, 0.65F);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.scorpion_whip.lash").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.scorpion_whip.reel").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
