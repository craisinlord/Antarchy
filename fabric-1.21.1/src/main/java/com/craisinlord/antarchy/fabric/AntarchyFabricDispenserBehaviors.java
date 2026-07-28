package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.dispenser.AntimetalMinecartDispenseBehavior;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public final class AntarchyFabricDispenserBehaviors {
    private static final DispenseItemBehavior SPAWN_EGG_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack);

            try {
                entityType.spawn(source.level(), stack, null, source.pos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
            } catch (Exception exception) {
                Antarchy.LOGGER.error("Error while dispensing spawn egg from dispenser at {}", source.pos(), exception);
                return ItemStack.EMPTY;
            }

            stack.shrink(1);
            source.level().gameEvent(null, GameEvent.ENTITY_PLACE, source.pos());
            return stack;
        }
    };
    private static final DispenseItemBehavior ANTIMETAL_MINECART_BEHAVIOR = new AntimetalMinecartDispenseBehavior();

    private AntarchyFabricDispenserBehaviors() {
    }

    public static void register() {
        registerSpawnEggs();
        registerPotions();
        registerMinecarts();
    }

    private static void registerSpawnEggs() {
        for (var holder : AntarchyFabricItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof SpawnEggItem) {
                DispenserBlock.registerBehavior(item, SPAWN_EGG_BEHAVIOR);
            }
        }
    }

    private static void registerPotions() {
        DispenserBlock.registerProjectileBehavior(Items.SPLASH_POTION);
        DispenserBlock.registerProjectileBehavior(Items.LINGERING_POTION);
    }

    private static void registerMinecarts() {
        DispenserBlock.registerBehavior(Items.MINECART, ANTIMETAL_MINECART_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.CHEST_MINECART, ANTIMETAL_MINECART_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.FURNACE_MINECART, ANTIMETAL_MINECART_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.TNT_MINECART, ANTIMETAL_MINECART_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.HOPPER_MINECART, ANTIMETAL_MINECART_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.COMMAND_BLOCK_MINECART, ANTIMETAL_MINECART_BEHAVIOR);
    }
}
