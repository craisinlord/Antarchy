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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class UltimateHoeItem extends Item {
    private final ToolMaterial material;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;

    public UltimateHoeItem(ToolMaterial material, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(properties.hoe(material, (float) attackDamage.getAsDouble(), (float) AntarchySettings.ultimateHoeAttackSpeed()));
        this.material = material;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        UltimateToolHelper.appendModeTooltip(UltimateToolHelper.ToolKind.HOE, tooltipComponents);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return UltimateToolHelper.getDestroySpeed(
                UltimateToolHelper.ToolKind.HOE,
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
        if (toggleResult != InteractionResult.PASS) {
            return toggleResult;
        }

        InteractionResult result = super.useOn(context);
        if (result.consumesAction()) {
            UltimateToolHelper.handleAreaUseOn(context, areaContext -> super.useOn(areaContext));
        }
        return result;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        boolean result = super.mineBlock(stack, level, state, pos, miningEntity);
        UltimateToolHelper.handleAreaMining(UltimateToolHelper.ToolKind.HOE, stack, level, state, pos, miningEntity);
        return result;
    }
}
