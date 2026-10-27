package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public final class ScorpionWhipItem extends Item {
    private static final Identifier ATTACK_RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "scorpion_whip_attack_range");
    private final ToolMaterial material;

    public ScorpionWhipItem(ToolMaterial material, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material, (float) Math.round(AntarchySettings.scorpionWhipBaseDamage()), -2.6F),
                () -> createAttributes(material)
        ));
        this.material = material;
    }

    private static ItemAttributeModifiers createAttributes(ToolMaterial material) {
        return AntarchyItemProperties.toolAttributes(
                material,
                (int) Math.round(AntarchySettings.scorpionWhipBaseDamage()),
                -2.6F
        ).withModifierAdded(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ATTACK_RANGE_MODIFIER_ID, AntarchySettings.scorpionWhipReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        boolean pulled = ScorpionWhipTetherManager.pullAndDetach(serverPlayer);
        return pulled ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (target.level().isClientSide() || !(attacker instanceof ServerPlayer serverPlayer)) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.POISON, AntarchySettings.scorpionWhipPoisonDurationTicks(), 0));
        if (!ScorpionWhipTetherManager.hasTether(serverPlayer) && ScorpionWhipTetherManager.canTether(target)) {
            ScorpionWhipTetherManager.attach(serverPlayer, target);
            ServerLevel level = serverPlayer.level();
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5D), target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.0D);
            level.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.8F, 0.65F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.scorpion_whip.lash").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.scorpion_whip.reel").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
