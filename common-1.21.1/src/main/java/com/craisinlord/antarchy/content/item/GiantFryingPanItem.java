package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.client.renderer.GiantFryingPanRenderer;
import java.util.function.Consumer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiantFryingPanItem extends SwordItem implements GeoItem {
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/giant_frying_pan.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/item/giant_frying_pan/giant_frying_pan_model.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/static_item.animation.json");
    private static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "giant_frying_pan_reach");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "giant_frying_pan_knockback");
    private final Tier tier;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public GiantFryingPanItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
        this.tier = tier;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<net.minecraft.network.chat.Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.antarchy.giant_frying_pan.insert").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.giant_frying_pan.gui").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.giant_frying_pan.campfire").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(this.tier, (int) Math.round(AntarchySettings.giantFryingPanAttackDamage()), -3.8F)
                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, 6.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_KNOCKBACK,
                        new AttributeModifier(KNOCKBACK_MODIFIER_ID, 2.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hurt = super.hurtEnemy(stack, target, attacker);
        if (!hurt || attacker.level().isClientSide) {
            return hurt;
        }

        Vec3 direction = target.position().subtract(attacker.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = attacker.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        if (direction.lengthSqr() >= 1.0E-4D) {
            direction = direction.normalize();
            Vec3 motion = target.getDeltaMovement();
            double x = Math.max(-2.6D, Math.min(2.6D, motion.x + direction.x * 1.8D));
            double z = Math.max(-2.6D, Math.min(2.6D, motion.z + direction.z * 1.8D));
            target.setDeltaMovement(x, motion.y, z);
            target.hurtMarked = true;
        }
        Level level = attacker.level();
        level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.25F);
        return hurt;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof Player player) GiantFryingPanStorage.tick(player, stack, level);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack pan = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, ignored) -> new com.craisinlord.antarchy.content.menu.GiantFryingPanMenu(containerId, inventory),
                        Component.translatable("item.antarchy.giant_frying_pan")));
            }
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(pan, level.isClientSide);
        }
        net.minecraft.world.InteractionHand foodHand = hand == net.minecraft.world.InteractionHand.MAIN_HAND
                ? net.minecraft.world.InteractionHand.OFF_HAND : net.minecraft.world.InteractionHand.MAIN_HAND;
        ItemStack food = player.getItemInHand(foodHand);
        if (!GiantFryingPanStorage.isCampfireInput(player, food)) return net.minecraft.world.InteractionResultHolder.pass(pan);
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        for (int slot = 0; slot < GiantFryingPanStorage.SLOT_COUNT; slot++) {
            if (storage.getItem(slot).isEmpty()) {
                if (!level.isClientSide) {
                    storage.setItem(slot, food.copyWithCount(1));
                    food.shrink(1);
                    level.playSound(null, player.blockPosition(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.PLAYERS, 0.8F, 1.0F);
                }
                return net.minecraft.world.InteractionResultHolder.sidedSuccess(pan, level.isClientSide);
            }
        }
        return net.minecraft.world.InteractionResultHolder.fail(pan);
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
            private GiantFryingPanRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GiantFryingPanRenderer(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }
}
