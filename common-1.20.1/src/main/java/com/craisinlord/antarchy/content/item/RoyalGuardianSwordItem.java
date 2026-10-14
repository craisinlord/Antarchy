package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.AntarchyGeoItem;
import com.craisinlord.antarchy.content.client.AntarchyGeoItemRenderer;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.component.AntarchyItemTag;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RoyalGuardianSwordItem extends SwordItem implements GeoItem, AntarchyGeoItem {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation("antarchy", "geo/royal_guardian_sword.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("antarchy", "textures/item/royal_guardian/royal_guardian_sword.png");
    private static final ResourceLocation ANIMATION_LOCATION = new ResourceLocation("antarchy", "animations/static_item.animation.json");
    private static final ResourceLocation REACH_MODIFIER_ID = new ResourceLocation("antarchy", "royal_guardian_sword_reach");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = new ResourceLocation("antarchy", "royal_guardian_sword_knockback");
    private static final String MODE_TAG = "antarchy.royal_guardian_sword.mode";
    private static final String DISCHARGE_READY_AT_TAG = "antarchy.royal_guardian_sword.discharge_ready_at";
    private final Tier tier;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RoyalGuardianSwordItem(Tier tier, Item.Properties properties) {
        super(tier, (int) Math.round(AntarchySettings.royalGuardianSwordAttackDamage()), (float) AntarchySettings.royalGuardianSwordAttackSpeed(), properties);
        this.tier = tier;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot) {
        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        com.google.common.collect.ImmutableMultimap.Builder<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> builder =
                com.craisinlord.antarchy.compat.item.WeaponAttributes.base(this.tier, (int) Math.round(AntarchySettings.royalGuardianSwordAttackDamage()), (float) AntarchySettings.royalGuardianSwordAttackSpeed());
        com.craisinlord.antarchy.compat.entity.EntityReach.addModifier(builder, REACH_MODIFIER_ID, AntarchySettings.royalWeaponAttackReachBonus());
        com.craisinlord.antarchy.compat.item.WeaponAttributes.add(builder, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK, KNOCKBACK_MODIFIER_ID, AntarchySettings.royalWeaponAttackKnockbackBonus());
        return builder.build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalWeaponEnchantability();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        return InteractionResultHolder.pass(stack);
    }

    public boolean tryCycleModeFromInput(net.minecraft.world.level.Level level, Player player, ItemStack stack) {
        if (player.getMainHandItem() != stack) return false;
        Mode next = getMode(stack).next();
        setMode(stack, next);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.antarchy.royal_guardian_sword.mode",
                    Component.translatable(next.translationKey)), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.8F + next.ordinal() * 0.2F);
        }
        return true;
    }

    private static void setMode(ItemStack stack, Mode mode) {
        AntarchyItemTag.update(stack, tag -> {
            if (mode == Mode.NONE) tag.remove(MODE_TAG);
            else tag.putInt(MODE_TAG, mode.id);
        });
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Mode mode = getMode(stack);
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_guardian_sword.mode",
                Component.translatable(mode.translationKey)).withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable(mode.tooltipKey).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_guardian_sword.controls",
                AntarchySettings.royalGuardianSwordElementalCooldownTicks() / 20.0D).withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static Mode getMode(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = AntarchyItemTag.getOrEmpty(stack);
        return tag.contains(MODE_TAG) ? Mode.fromId(tag.getInt(MODE_TAG)) : Mode.NONE;
    }

    public static boolean isDischargeReady(ItemStack stack, long gameTime) {
        return AntarchyItemTag.getOrEmpty(stack).getLong(DISCHARGE_READY_AT_TAG) <= gameTime;
    }

    public static void startDischargeCooldown(ItemStack stack, long gameTime) {
        AntarchyItemTag.update(stack, tag -> tag.putLong(DISCHARGE_READY_AT_TAG,
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

    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new AntarchyGeoItemRenderer() {
            private AnimatedHeldItemRenderer<RoyalGuardianSwordItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }

    private final java.util.function.Supplier<Object> renderProvider = com.google.common.base.Suppliers.memoize(() -> {
        Object[] holder = new Object[1];
        createRenderer(o -> holder[0] = o);
        return holder[0];
    });

    @Override
    public java.util.function.Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }
}
