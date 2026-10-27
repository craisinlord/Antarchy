package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import com.craisinlord.antarchy.content.item.AntarchyItemProperties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class UltimatePickaxeItem extends Item {
    private final ToolMaterial material;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;

    public UltimatePickaxeItem(ToolMaterial material, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(AntarchyItemProperties.configEnchantable(
                AntarchyItemProperties.configAttributes(
                        properties.pickaxe(material, (float) attackDamage.getAsDouble(), attackSpeed),
                        () -> AntarchyItemProperties.toolAttributes(
                                material,
                                (float) attackDamage.getAsDouble(),
                                (float) AntarchySettings.ultimatePickaxeAttackSpeed()
                        )
                ),
                AntarchySettings::ultimateToolEnchantability
        ));
        this.material = material;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        UltimateToolHelper.appendModeTooltip(UltimateToolHelper.ToolKind.PICKAXE, tooltipComponents);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return UltimateToolHelper.getDestroySpeed(
                UltimateToolHelper.ToolKind.PICKAXE,
                this.material,
                stack,
                state,
                super.getDestroySpeed(stack, state)
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult toggleResult = UltimateToolHelper.handleToggleUse(level, player, hand);
        return toggleResult == InteractionResult.PASS ? super.use(level, player, hand) : toggleResult;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult toggleResult = UltimateToolHelper.handleToggleUseOn(context);
        return toggleResult == InteractionResult.PASS ? super.useOn(context) : toggleResult;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        boolean result = super.mineBlock(stack, level, state, pos, miningEntity);
        UltimateToolHelper.handleAreaMining(UltimateToolHelper.ToolKind.PICKAXE, stack, level, state, pos, miningEntity);
        return result;
    }
}
