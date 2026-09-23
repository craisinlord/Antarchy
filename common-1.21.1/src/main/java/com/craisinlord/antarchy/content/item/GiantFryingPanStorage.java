package com.craisinlord.antarchy.content.item;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class GiantFryingPanStorage implements Container {
    public static final int SLOT_COUNT = 9;
    private static final String SLOTS = "antarchy.giant_frying_pan_slots";
    private static final String SLOT = "Slot";
    private static final String ITEM = "Item";
    private static final String TIME = "Time";
    private final ItemStack pan;
    private final Player player;

    public GiantFryingPanStorage(ItemStack pan, Player player) {
        this.pan = pan;
        this.player = player;
    }

    public static ItemStack findPan(Player player) {
        ItemStack selected = player.getMainHandItem();
        if (selected.getItem() instanceof GiantFryingPanItem) return selected;
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof GiantFryingPanItem) return offhand;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GiantFryingPanItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static int findPanSlot(Player player) {
        if (player.getMainHandItem().getItem() instanceof GiantFryingPanItem) return player.getInventory().selected;
        if (player.getOffhandItem().getItem() instanceof GiantFryingPanItem) return 40;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().items.get(i).getItem() instanceof GiantFryingPanItem) return i;
        }
        return -1;
    }

    public static void tick(Player player, ItemStack pan, Level level) {
        if (level.isClientSide || !(pan.getItem() instanceof GiantFryingPanItem)) return;
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack input = storage.getItem(slot);
            if (input.isEmpty()) continue;
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe = level.getServer().getRecipeManager().getRecipeFor(
                    RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(input), level);
            if (recipe.isEmpty()) {
                storage.setCookTime(slot, 0);
                continue;
            }
            int time = storage.getCookTime(slot) + 1;
            if (time < 600) {
                storage.setCookTime(slot, time);
                continue;
            }
            ItemStack cooked = recipe.get().value().assemble(new SingleRecipeInput(input), level.registryAccess());
            input.shrink(1);
            storage.setItem(slot, input);
            storage.setCookTime(slot, 0);
            if (!cooked.isEmpty()) {
                var output = new net.minecraft.world.entity.item.ItemEntity(level, player.getX(), player.getY() + 1.2D, player.getZ(), cooked);
                output.setDeltaMovement(player.getLookAngle().scale(0.35D).add(0.0D, 0.2D, 0.0D));
                output.setPickUpDelay(10);
                level.addFreshEntity(output);
            }
        }
    }

    public static boolean isCampfireInput(Player player, ItemStack stack) {
        return !stack.isEmpty() && (player.getServer() == null || player.getServer().getRecipeManager().getRecipeFor(
                RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), player.level()).isPresent());
    }

    private ListTag slotList() {
        return pan.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                .copyTag().getList(SLOTS, Tag.TAG_COMPOUND);
    }

    private CompoundTag slotTag(int slot) {
        for (Tag raw : slotList()) {
            CompoundTag tag = (CompoundTag) raw;
            if (tag.getInt(SLOT) == slot) return tag;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt(SLOT, slot);
        return tag;
    }

    private void writeSlot(int slot, ItemStack stack, int cookTime) {
        HolderLookup.Provider registries = player.registryAccess();
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, pan, root -> {
            ListTag list = root.getList(SLOTS, Tag.TAG_COMPOUND);
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.getCompound(i).getInt(SLOT) == slot) list.remove(i);
            }
            if (!stack.isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt(SLOT, slot);
                tag.putInt(TIME, cookTime);
                tag.put(ITEM, stack.save(registries));
                list.add(tag);
            }
            root.put(SLOTS, list);
        });
    }

    public int getCookTime(int slot) {
        return slotTag(slot).getInt(TIME);
    }

    public void setCookTime(int slot, int value) {
        ItemStack stack = getItem(slot);
        if (!stack.isEmpty()) writeSlot(slot, stack, value);
    }

    @Override
    public int getContainerSize() { return SLOT_COUNT; }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < SLOT_COUNT; i++) if (!getItem(i).isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        CompoundTag tag = slotTag(slot);
        if (!tag.contains(ITEM, Tag.TAG_COMPOUND)) return ItemStack.EMPTY;
        return ItemStack.parseOptional(player.registryAccess(), tag.getCompound(ITEM));
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = stack.split(amount);
        writeSlot(slot, stack, 0);
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = getItem(slot);
        writeSlot(slot, ItemStack.EMPTY, 0);
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) { writeSlot(slot, stack, 0); }

    @Override
    public void setChanged() { }

    @Override
    public boolean stillValid(Player player) { return player.isAlive(); }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) writeSlot(i, ItemStack.EMPTY, 0);
    }
}
