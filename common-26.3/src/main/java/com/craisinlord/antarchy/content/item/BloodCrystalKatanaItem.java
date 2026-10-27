package com.craisinlord.antarchy.content.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.BloodCrystalKatanaTrailClientState;

public class BloodCrystalKatanaItem extends Item {
    private static final Identifier ATTACK_RANGE_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("antarchy", "blood_crystal_katana_attack_range");
    private static final float FULL_SWING_THRESHOLD = 0.9F;
    private static final Map<UUID, Long> LAST_DASH_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> DASH_UNTIL_TICK = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface TrailCallback {
        void trigger(ServerPlayer player, int durationTicks);
    }

    private static TrailCallback trailCallback = (player, durationTicks) -> {};

    private final ToolMaterial material;
    private final int attackDamage;
    private final float attackSpeed;

    public BloodCrystalKatanaItem(ToolMaterial material, Item.Properties properties, int attackDamage, float attackSpeed) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material, attackDamage, attackSpeed),
                () -> AntarchyItemProperties.toolAttributes(material, attackDamage, attackSpeed)
                        .withModifierAdded(
                                Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(ATTACK_RANGE_MODIFIER_ID, AntarchySettings.bloodCrystalKatanaReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND
                        )
        ));
        this.material = material;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    public static void setTrailCallback(TrailCallback callback) {
        trailCallback = callback == null ? (player, durationTicks) -> {} : callback;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof ServerPlayer player) {
            performDash(player, player.getAttackStrengthScale(0.0F) >= FULL_SWING_THRESHOLD);
        }
    }

    public static void performDash(ServerPlayer player, boolean fullSwing) {
        long now = player.level().getGameTime();
        Long last = LAST_DASH_TICK.get(player.getUUID());
        if (last != null && now - last < AntarchySettings.bloodCrystalKatanaDashCooldownTicks()) {
            return;
        }
        LAST_DASH_TICK.put(player.getUUID(), now);
        DASH_UNTIL_TICK.put(player.getUUID(), now + AntarchySettings.bloodCrystalKatanaTrailDurationTicks());

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
        }

        Vec3 launch = horizontal.normalize().scale(AntarchySettings.bloodCrystalKatanaLaunchStrength())
                .add(0.0D, 0.12D, 0.0D);
        player.setDeltaMovement(player.getDeltaMovement().add(launch));
        player.needsSync = true;
        player.syncVelocity = true;

        trailCallback.trigger(player, AntarchySettings.bloodCrystalKatanaTrailDurationTicks());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                AntarchySoundEvents.BLOOD_CRYSTAL_KATANA_DASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        int invulnTicks = AntarchySettings.bloodCrystalKatanaInvulnTicks();
        if (fullSwing && invulnTicks > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, invulnTicks, 4, false, false, false));
        }
    }

    public static boolean isDashing(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        if (entity.level().isClientSide()) {
            return BloodCrystalKatanaTrailClientState.getRemainingTicks(entity.getId()) > 0;
        }
        Long until = DASH_UNTIL_TICK.get(entity.getUUID());
        if (until == null) {
            return false;
        }
        if (entity.level().getGameTime() >= until) {
            DASH_UNTIL_TICK.remove(entity.getUUID());
            return false;
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.blood_crystal_katana.launch").withStyle(ChatFormatting.RED));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.blood_crystal_katana.dash").withStyle(ChatFormatting.RED));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.blood_crystal_katana.trail").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
