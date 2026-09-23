package com.craisinlord.antarchy.content.item;

import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class GiantFryingPanStorage implements Container {
    public static final int SLOT_COUNT = 9;
    public static final int COOK_TIME = 600;
    private static final String SLOTS = "antarchy.giant_frying_pan_slots";
    private static final String SLOT = "Slot";
    private static final String ITEM = "Item";
    private static final String START = "Start";
    private final Player player;
    private final int panSlot;
    private final ItemStack fixedPan;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final long[] startTimes = new long[SLOT_COUNT];
    private final ItemStack[] startItems = new ItemStack[SLOT_COUNT];
    private CustomData loadedFrom;
    private ItemStack loadedPan = ItemStack.EMPTY;

    public GiantFryingPanStorage(ItemStack pan, Player player) {
        this.player = player;
        this.panSlot = -1;
        this.fixedPan = pan;
        this.reloadIfChanged();
    }

    public GiantFryingPanStorage(Player player, int panSlot) {
        this.player = player;
        this.panSlot = panSlot;
        this.fixedPan = null;
        this.reloadIfChanged();
    }

    public ItemStack pan() {
        if (this.fixedPan != null) return this.fixedPan;
        if (this.panSlot < 0 || this.panSlot >= this.player.getInventory().getContainerSize()) return ItemStack.EMPTY;
        return this.player.getInventory().getItem(this.panSlot);
    }

    public int panSlot() {
        return this.panSlot;
    }

    public boolean hasPan() {
        return this.pan().getItem() instanceof GiantFryingPanItem;
    }

    public static ItemStack findPan(Player player) {
        int slot = findPanSlot(player);
        return slot < 0 ? ItemStack.EMPTY : player.getInventory().getItem(slot);
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
        if (!pan.has(DataComponents.CUSTOM_DATA)) return;
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        if (storage.isEmpty()) return;
        long now = level.getGameTime();
        boolean changed = false;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack input = storage.items.get(slot);
            if (input.isEmpty() || now - storage.startTimes[slot] < COOK_TIME) continue;
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe = level.getRecipeManager().getRecipeFor(
                    RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(input), level);
            if (recipe.isEmpty()) continue;
            ItemStack cooked = recipe.get().value().assemble(new SingleRecipeInput(input), level.registryAccess());
            input.shrink(1);
            storage.startTimes[slot] = now;
            changed = true;
            if (!cooked.isEmpty()) {
                ItemEntity output = new ItemEntity(level, player.getX(), player.getY() + 1.2D, player.getZ(), cooked);
                output.setDeltaMovement(player.getLookAngle().scale(0.35D).add(0.0D, 0.2D, 0.0D));
                output.setPickUpDelay(10);
                level.addFreshEntity(output);
            }
        }
        if (changed) storage.setChanged();
    }

    public static boolean isCampfireInput(Player player, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof GiantFryingPanItem) return false;
        Level level = player.level();
        return level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), level).isPresent();
    }

    public boolean insertOne(ItemStack food) {
        if (!isCampfireInput(this.player, food)) return false;
        this.reloadIfChanged();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (this.items.get(slot).isEmpty()) {
                this.setItem(slot, food.split(1));
                return true;
            }
        }
        return false;
    }

    public boolean hasSpace() {
        this.reloadIfChanged();
        for (ItemStack stack : this.items) {
            if (stack.isEmpty()) return true;
        }
        return false;
    }

    public float cookProgress(int slot) {
        this.reloadIfChanged();
        if (this.items.get(slot).isEmpty()) return 0.0F;
        long elapsed = this.player.level().getGameTime() - this.startTimes[slot];
        return Math.max(0.0F, Math.min(1.0F, elapsed / (float) COOK_TIME));
    }

    private void reloadIfChanged() {
        ItemStack pan = this.pan();
        CustomData data = pan.get(DataComponents.CUSTOM_DATA);
        if (pan == this.loadedPan && data == this.loadedFrom) return;
        this.loadedPan = pan;
        this.loadedFrom = data;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            this.items.set(slot, ItemStack.EMPTY);
            this.startTimes[slot] = 0L;
            this.startItems[slot] = null;
        }
        if (data == null) return;
        ListTag list = data.copyTag().getList(SLOTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            int slot = tag.getInt(SLOT);
            if (slot < 0 || slot >= SLOT_COUNT || !tag.contains(ITEM, Tag.TAG_COMPOUND)) continue;
            ItemStack stack = ItemStack.parseOptional(this.player.registryAccess(), tag.getCompound(ITEM));
            this.items.set(slot, stack);
            this.startTimes[slot] = tag.getLong(START);
            this.startItems[slot] = stack;
        }
    }

    private void refreshStartTimes() {
        long now = this.player.level().getGameTime();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = this.items.get(slot);
            if (stack.isEmpty()) {
                this.startItems[slot] = null;
                this.startTimes[slot] = 0L;
            } else if (this.startItems[slot] == null || !ItemStack.isSameItemSameComponents(this.startItems[slot], stack)) {
                this.startItems[slot] = stack;
                this.startTimes[slot] = now;
            }
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        this.reloadIfChanged();
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        this.reloadIfChanged();
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        this.reloadIfChanged();
        ItemStack stack = this.items.get(slot);
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack result = stack.split(amount);
        this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        this.reloadIfChanged();
        ItemStack result = this.items.get(slot);
        this.items.set(slot, ItemStack.EMPTY);
        this.setChanged();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.reloadIfChanged();
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        ItemStack pan = this.pan();
        if (!(pan.getItem() instanceof GiantFryingPanItem)) return;
        this.refreshStartTimes();
        ListTag list = new ListTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = this.items.get(slot);
            if (stack.isEmpty()) continue;
            CompoundTag tag = new CompoundTag();
            tag.putInt(SLOT, slot);
            tag.putLong(START, this.startTimes[slot]);
            tag.put(ITEM, stack.save(this.player.registryAccess()));
            list.add(tag);
        }
        CustomData.update(DataComponents.CUSTOM_DATA, pan, root -> {
            if (list.isEmpty()) root.remove(SLOTS);
            else root.put(SLOTS, list);
        });
        this.loadedPan = pan;
        this.loadedFrom = pan.get(DataComponents.CUSTOM_DATA);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && this.hasPan();
    }

    @Override
    public void clearContent() {
        this.reloadIfChanged();
        for (int slot = 0; slot < SLOT_COUNT; slot++) this.items.set(slot, ItemStack.EMPTY);
        this.setChanged();
    }
}
