package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.client.renderer.GiantFryingPanRenderer;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerLevel;
import com.geckolib.renderer.GeoItemRenderer;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import com.craisinlord.antarchy.content.menu.GiantFryingPanMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class GiantFryingPanItem extends Item implements GeoItem {
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "giant_frying_pan");
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "textures/item/giant_frying_pan/giant_frying_pan_model.png");
    private static final Identifier ANIMATION_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "static_item");
    private static final Identifier REACH_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "giant_frying_pan_reach");
    private static final Identifier KNOCKBACK_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "giant_frying_pan_knockback");
    private final ToolMaterial material;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public GiantFryingPanItem(ToolMaterial material, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material, (int) Math.round(AntarchySettings.giantFryingPanAttackDamage()), -3.8F),
                () -> createPanAttributes(material)));
        this.material = material;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<net.minecraft.network.chat.Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.giant_frying_pan.insert").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.giant_frying_pan.gui").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.giant_frying_pan.campfire").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.giant_frying_pan.dig").withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.giant_frying_pan.launch").withStyle(ChatFormatting.GOLD));
    }

    public static ItemAttributeModifiers createPanAttributes(ToolMaterial material) {
        return AntarchyItemProperties.toolAttributes(material, (int) Math.round(AntarchySettings.giantFryingPanAttackDamage()), -3.8F)
                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, 6.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_KNOCKBACK,
                        new AttributeModifier(KNOCKBACK_MODIFIER_ID, 2.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide()) {
            return;
        }

        GiantFryingPanToolHelper.launch(target, attacker);
        Level level = attacker.level();
        level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.25F);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return GiantFryingPanToolHelper.getDestroySpeed(this.material, stack, state, super.getDestroySpeed(stack, state));
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return GiantFryingPanToolHelper.isShovelBlock(state) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
        }
        GiantFryingPanToolHelper.mineArea(stack, level, state, pos, miningEntity);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, net.minecraft.world.entity.Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        GiantFryingPanToolHelper.ensureEnchantments(stack, level.registryAccess());
        if (entity instanceof Player player) GiantFryingPanStorage.tick(player, stack, level);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        GiantFryingPanToolHelper.ensureEnchantments(stack, player.level().registryAccess());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack pan = player.getItemInHand(hand);
        InteractionHand foodHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack food = player.getItemInHand(foodHand);
        if (player.isShiftKeyDown()) {
            openMenu(level, player, hand);
            return InteractionResult.SUCCESS;
        }
        if (!GiantFryingPanStorage.isCampfireInput(player, food)) {
            return InteractionResult.PASS;
        }
        return insertFromHand(level, player, pan, food)
                ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
    }

    public static InteractionResult handleMainHandUse(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack pan = player.getOffhandItem();
        ItemStack held = player.getMainHandItem();
        if (!(pan.getItem() instanceof GiantFryingPanItem) || held.getItem() instanceof GiantFryingPanItem) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            openMenu(level, player, InteractionHand.OFF_HAND);
            return InteractionResult.SUCCESS;
        }
        if (!GiantFryingPanStorage.isCampfireInput(player, held)) return InteractionResult.PASS;
        return insertFromHand(level, player, pan, held) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static void openHeldMenu(Player player) {
        if (!player.isShiftKeyDown()) return;
        if (player.getMainHandItem().getItem() instanceof GiantFryingPanItem) {
            openMenu(player.level(), player, InteractionHand.MAIN_HAND);
        } else if (player.getOffhandItem().getItem() instanceof GiantFryingPanItem) {
            openMenu(player.level(), player, InteractionHand.OFF_HAND);
        }
    }

    private static void openMenu(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || player.containerMenu instanceof GiantFryingPanMenu) return;
        int panSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new GiantFryingPanMenu(containerId, inventory, panSlot),
                Component.translatable("item.antarchy.giant_frying_pan")));
    }

    private static boolean insertFromHand(Level level, Player player, ItemStack pan, ItemStack food) {
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        ItemStack single = food.copyWithCount(1);
        if (!storage.insertOne(single)) {
            return false;
        }
        if (!player.getAbilities().instabuild) food.shrink(1);
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return true;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pan, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        ItemStack food = slot.getItem();
        if (!GiantFryingPanStorage.isCampfireInput(player, food) || !slot.mayPickup(player)) return false;
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        boolean inserted = storage.hasSpace(food) && storage.insertOne(slot.safeTake(1, 1, player));
        if (inserted) player.playSound(SoundEvents.CAMPFIRE_CRACKLE, 0.8F, 1.0F);
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pan, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || other.isEmpty() || !slot.allowModification(player)) return false;
        if (!GiantFryingPanStorage.isCampfireInput(player, other)) return false;
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        ItemStack single = other.copyWithCount(1);
        boolean inserted = storage.insertOne(single);
        if (inserted) {
            other.shrink(1);
            player.playSound(SoundEvents.CAMPFIRE_CRACKLE, 0.8F, 1.0F);
        }
        return true;
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
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GiantFryingPanRenderer(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }
}
