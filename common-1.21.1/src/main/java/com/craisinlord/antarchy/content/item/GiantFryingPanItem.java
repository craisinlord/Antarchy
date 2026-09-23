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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.menu.GiantFryingPanMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockState;
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
        tooltipComponents.add(Component.translatable("tooltip.antarchy.giant_frying_pan.dig").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.giant_frying_pan.launch").withStyle(ChatFormatting.GOLD));
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

        GiantFryingPanToolHelper.launch(target, attacker);
        Level level = attacker.level();
        level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.25F);
        return hurt;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return GiantFryingPanToolHelper.getDestroySpeed(this.tier, stack, state, super.getDestroySpeed(stack, state));
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return GiantFryingPanToolHelper.isShovelBlock(state) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
        }
        GiantFryingPanToolHelper.mineArea(stack, level, state, pos, miningEntity);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide) GiantFryingPanToolHelper.ensureEnchantments(stack, level.registryAccess());
        if (entity instanceof Player player) GiantFryingPanStorage.tick(player, stack, level);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        GiantFryingPanToolHelper.ensureEnchantments(stack, level.registryAccess());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pan = player.getItemInHand(hand);
        InteractionHand foodHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack food = player.getItemInHand(foodHand);
        Antarchy.LOGGER.info("[FryingPan] use client={} hand={} shift={} pan={} otherHand={}",
                level.isClientSide, hand, player.isShiftKeyDown(), pan, food);
        if (player.isShiftKeyDown()) {
            openMenu(level, player, hand);
            return InteractionResultHolder.sidedSuccess(pan, level.isClientSide);
        }
        if (!GiantFryingPanStorage.isCampfireInput(player, food)) {
            Antarchy.LOGGER.info("[FryingPan] use client={} other hand item is not a campfire input", level.isClientSide);
            return InteractionResultHolder.pass(pan);
        }
        return insertFromHand(level, player, pan, food)
                ? InteractionResultHolder.sidedSuccess(pan, level.isClientSide)
                : InteractionResultHolder.fail(pan);
    }

    public static InteractionResult handleMainHandUse(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack pan = player.getOffhandItem();
        ItemStack held = player.getMainHandItem();
        if (!(pan.getItem() instanceof GiantFryingPanItem) || held.getItem() instanceof GiantFryingPanItem) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            Antarchy.LOGGER.info("[FryingPan] offhand pan shift-use client={} mainHand={}", level.isClientSide, held);
            openMenu(level, player, InteractionHand.OFF_HAND);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!GiantFryingPanStorage.isCampfireInput(player, held)) return InteractionResult.PASS;
        Antarchy.LOGGER.info("[FryingPan] offhand pan insert client={} mainHand={}", level.isClientSide, held);
        return insertFromHand(level, player, pan, held) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.FAIL;
    }

    private static void openMenu(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return;
        int panSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        Antarchy.LOGGER.info("[FryingPan] opening menu for {} panSlot={}", player.getName().getString(), panSlot);
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new GiantFryingPanMenu(containerId, inventory, panSlot),
                Component.translatable("item.antarchy.giant_frying_pan")));
    }

    private static boolean insertFromHand(Level level, Player player, ItemStack pan, ItemStack food) {
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        ItemStack single = food.copyWithCount(1);
        if (!storage.insertOne(single)) {
            Antarchy.LOGGER.info("[FryingPan] insert failed client={} food={} (pan full or not cookable)", level.isClientSide, food);
            return false;
        }
        if (!player.getAbilities().instabuild) food.shrink(1);
        Antarchy.LOGGER.info("[FryingPan] inserted client={} food={}", level.isClientSide, single.getItem());
        if (!level.isClientSide) {
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
        Antarchy.LOGGER.info("[FryingPan] cursor pan onto slot client={} food={} inserted={}", player.level().isClientSide, food.getItem(), inserted);
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
        Antarchy.LOGGER.info("[FryingPan] cursor food onto pan client={} food={} inserted={}", player.level().isClientSide, single.getItem(), inserted);
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
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GiantFryingPanRenderer(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }
}
