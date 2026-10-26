package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.item.ultimate.UltimateToolHelper;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoItem;
import com.craisinlord.antarchy.content.client.AntarchyGeoItemRenderer;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BattleAxeItem extends AxeItem implements GeoItem, com.craisinlord.antarchy.content.client.AntarchyGeoItem {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation("antarchy", "geo/battle_axe.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("antarchy", "textures/item/battle_axe/battle_axe.png");
    private static final ResourceLocation ANIMATION_LOCATION = new ResourceLocation("antarchy", "animations/static_item.animation.json");
    private final Tier tier;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BattleAxeItem(Tier tier, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(tier, (float) attackDamage.getAsDouble(), (float) AntarchySettings.battleAxeAttackSpeed(), properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot) {
        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return com.craisinlord.antarchy.compat.item.WeaponAttributes.base(this.tier, (float) this.attackDamage.getAsDouble(), (float) AntarchySettings.battleAxeAttackSpeed()).build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        if (AntarchySettings.ultimateToolsVeinMiningEnabled()) {
            tooltipComponents.add(Component.translatable("tooltip.antarchy.battle_axe_tree_felling").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        boolean result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (AntarchySettings.ultimateToolsVeinMiningEnabled()
                && miningEntity instanceof ServerPlayer player
                && !player.isShiftKeyDown()) {
            UltimateToolHelper.mineTree(stack, level, state, pos, player, AntarchySettings.ultimateToolsTreeFellingMaxLogs());
        }
        return result;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
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
            private AnimatedHeldItemRenderer<BattleAxeItem> renderer;

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
