package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class UltimatePickaxeItem extends PickaxeItem {
    private final Tier tier;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;

    public UltimatePickaxeItem(Tier tier, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(tier, properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return createAttributes(this.tier, (float) this.attackDamage.getAsDouble(), (float) AntarchySettings.ultimatePickaxeAttackSpeed());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        UltimateToolHelper.appendThreeByThreeTooltip(tooltipComponents);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return UltimateToolHelper.getDestroySpeed(
                UltimateToolHelper.ToolKind.PICKAXE,
                this.tier,
                stack,
                state,
                super.getDestroySpeed(stack, state)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> toggleResult = UltimateToolHelper.handleToggleUse(level, player, hand);
        return toggleResult.getResult() == InteractionResult.PASS ? super.use(level, player, hand) : toggleResult;
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

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.ultimateToolEnchantability();
    }
}
