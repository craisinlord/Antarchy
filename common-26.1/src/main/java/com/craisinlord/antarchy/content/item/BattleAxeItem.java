package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.item.ultimate.UltimateToolHelper;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;

public class BattleAxeItem extends AxeItem implements GeoItem {
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "battle_axe");
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "textures/item/battle_axe/battle_axe.png");
    private static final Identifier ANIMATION_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "static_item");
    private final ToolMaterial material;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BattleAxeItem(ToolMaterial material, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(material, (float) attackDamage.getAsDouble(), (float) AntarchySettings.battleAxeAttackSpeed(), properties);
        this.material = material;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        if (AntarchySettings.ultimateToolsVeinMiningEnabled()) {
            tooltipComponents.accept(Component.translatable("tooltip.antarchy.battle_axe_tree_felling").withStyle(ChatFormatting.GRAY));
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<BattleAxeItem> renderer;

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
