package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import com.geckolib.renderer.GeoItemRenderer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class RoyalGuardianSwordItem extends Item implements GeoItem {
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "royal_guardian_sword");
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "textures/item/royal_guardian/royal_guardian_sword.png");
    private static final Identifier ANIMATION_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "static_item");
    private static final Identifier REACH_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "royal_guardian_sword_reach");
    private static final Identifier KNOCKBACK_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "royal_guardian_sword_knockback");
    private static final String MODE_TAG = "antarchy.royal_guardian_sword.mode";
    private static final String DISCHARGE_READY_AT_TAG = "antarchy.royal_guardian_sword.discharge_ready_at";
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RoyalGuardianSwordItem(ToolMaterial material, Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(
                AntarchyItemProperties.configAttributes(
                        properties.sword(material,
                                (int) Math.round(AntarchySettings.royalGuardianSwordAttackDamage()),
                                (float) AntarchySettings.royalGuardianSwordAttackSpeed()),
                        () -> createRoyalAttributes(material)),
                AntarchySettings::royalWeaponEnchantability));
        GeoItem.registerSyncedAnimatable(this);
    }

    public static ItemAttributeModifiers createRoyalAttributes(ToolMaterial material) {
        return AntarchyItemProperties.toolAttributes(
                        material,
                        (int) Math.round(AntarchySettings.royalGuardianSwordAttackDamage()),
                        (float) AntarchySettings.royalGuardianSwordAttackSpeed())
                .withModifierAdded(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, AntarchySettings.royalWeaponAttackReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(
                        Attributes.ATTACK_KNOCKBACK,
                        new AttributeModifier(KNOCKBACK_MODIFIER_ID, AntarchySettings.royalWeaponAttackKnockbackBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        return InteractionResult.PASS;
    }

    public boolean tryCycleModeFromInput(net.minecraft.world.level.Level level, Player player, ItemStack stack) {
        if (player.getMainHandItem() != stack) return false;
        Mode next = getMode(stack).next();
        setMode(stack, next);
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.royal_guardian_sword.mode",
                    Component.translatable(next.translationKey)));
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.8F + next.ordinal() * 0.2F);
        }
        return true;
    }

    private static void setMode(ItemStack stack, Mode mode) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (mode == Mode.NONE) tag.remove(MODE_TAG);
            else tag.putInt(MODE_TAG, mode.id);
        });
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Mode mode = getMode(stack);
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_guardian_sword.mode",
                Component.translatable(mode.translationKey)).withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.translatable(mode.tooltipKey).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_guardian_sword.controls",
                AntarchySettings.royalGuardianSwordElementalCooldownTicks() / 20.0D).withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    public static Mode getMode(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(MODE_TAG) ? Mode.fromId(tag.getIntOr(MODE_TAG, 0)) : Mode.NONE;
    }

    public static boolean isDischargeReady(ItemStack stack, long gameTime) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .getLongOr(DISCHARGE_READY_AT_TAG, 0L) <= gameTime;
    }

    public static void startDischargeCooldown(ItemStack stack, long gameTime) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(DISCHARGE_READY_AT_TAG,
                gameTime + Math.max(0, AntarchySettings.royalGuardianSwordElementalCooldownTicks())));
    }

    public enum Mode {
        NONE(-1, "tooltip.antarchy.royal_guardian_sword.mode.none", "tooltip.antarchy.royal_guardian_sword.none"),
        FIRE(0, "tooltip.antarchy.royal_guardian_sword.mode.fire", "tooltip.antarchy.royal_guardian_sword.fire"),
        FROST(1, "tooltip.antarchy.royal_guardian_sword.mode.frost", "tooltip.antarchy.royal_guardian_sword.frost"),
        STORM(2, "tooltip.antarchy.royal_guardian_sword.mode.storm", "tooltip.antarchy.royal_guardian_sword.storm");

        private final int id;
        private final String translationKey;
        private final String tooltipKey;

        Mode(int id, String translationKey, String tooltipKey) {
            this.id = id;
            this.translationKey = translationKey;
            this.tooltipKey = tooltipKey;
        }

        public Mode next() {
            return switch (this) {
                case NONE -> FIRE;
                case FIRE -> FROST;
                case FROST -> STORM;
                case STORM -> NONE;
            };
        }

        private static Mode fromId(int id) {
            for (Mode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return NONE;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<RoyalGuardianSwordItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }
}
