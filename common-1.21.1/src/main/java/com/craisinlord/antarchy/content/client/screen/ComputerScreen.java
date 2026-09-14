package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import com.craisinlord.antarchy.content.guide.ComputerGuideData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ComputerScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 210;
    private static final String[] CATEGORIES = {"general", "elythia", "thoraxis", "cavaryn"};
    private static final int[] COLORS = {0xFF65FF65, 0xFF65FF65, 0xFFFF5A5A, 0xFFFFA044};
    private final ResourceLocation positionDimension;
    private final net.minecraft.core.BlockPos position;
    private int categoryIndex;
    private int selectedIndex;
    private List<ComputerGuideData.Entry> visibleEntries = List.of();

    public ComputerScreen(net.minecraft.core.BlockPos position) {
        super(Component.translatable("screen.antarchy.computer"));
        this.position = position;
        this.positionDimension = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.dimension().location();
    }

    @Override
    protected void init() {
        refreshEntries();
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;
        for (int index = 0; index < CATEGORIES.length; index++) {
            int buttonIndex = index;
            addRenderableWidget(Button.builder(Component.translatable("guide.antarchy.category." + CATEGORIES[index]), button -> {
                categoryIndex = buttonIndex;
                selectedIndex = 0;
                refreshEntries();
            }).bounds(left + 10 + index * 75, top + 10, 70, 20).build());
        }
    }

    private void refreshEntries() {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(position) instanceof ComputerBlockEntity computer) {
            visibleEntries = ComputerGuideData.entriesFor(computer.diskIds()).stream()
                    .filter(entry -> entry.category().equals(CATEGORIES[categoryIndex]))
                    .toList();
        } else {
            visibleEntries = List.of();
        }
        if (selectedIndex >= visibleEntries.size()) {
            selectedIndex = Math.max(0, visibleEntries.size() - 1);
        }
    }

    @Override
    public void tick() {
        refreshEntries();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;
        graphics.fill(0, 0, this.width, this.height, 0x99000000);
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, 0xFF071007);
        graphics.fill(left + 4, top + 4, left + WIDTH - 4, top + HEIGHT - 4, 0xFF102010);
        int accent = COLORS[categoryIndex];
        graphics.fill(left + 8, top + 34, left + WIDTH - 8, top + 36, accent);
        graphics.drawString(this.font, Component.translatable("screen.antarchy.computer"), left + 12, top + 45, accent, false);
        graphics.drawString(this.font, Component.literal(positionDimension == null ? "ARCHIVE OFFLINE" : "ARCHIVE ONLINE"), left + WIDTH - 105, top + 45, 0xFF65FF65, false);
        graphics.fill(left + 10, top + 60, left + 112, top + HEIGHT - 12, 0xFF050805);
        for (int index = 0; index < visibleEntries.size() && index < 6; index++) {
            ComputerGuideData.Entry entry = visibleEntries.get(index);
            int y = top + 68 + index * 20;
            int color = index == selectedIndex ? accent : 0xFF65FF65;
            if (index == selectedIndex) {
                graphics.fill(left + 12, y - 2, left + 110, y + 14, 0xFF173817);
            }
            graphics.drawString(this.font, Component.literal("FILE_" + (index + 1)), left + 16, y, color, false);
            if (isMouseOver(left + 12, y - 2, 98, 16, mouseX, mouseY)) {
                graphics.renderTooltip(this.font, Component.translatable(entry.titleKey()), mouseX, mouseY);
            }
        }
        if (visibleEntries.isEmpty()) {
            graphics.drawString(this.font, Component.literal("NO DATA FOUND"), left + 22, top + 86, accent, false);
            graphics.drawString(this.font, Component.literal("INSERT FLOPPY DISKS"), left + 16, top + 104, 0xFF65FF65, false);
        } else {
            renderEntry(graphics, visibleEntries.get(selectedIndex), left + 124, top + 62, accent);
        }
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderEntry(GuiGraphics graphics, ComputerGuideData.Entry entry, int x, int y, int accent) {
        graphics.drawString(this.font, Component.translatable(entry.titleKey()), x, y, accent, false);
        if (!entry.subtitleKey().isEmpty()) {
            graphics.drawString(this.font, Component.translatable(entry.subtitleKey()), x, y + 16, 0xFF99FF99, false);
        }
        int line = y + 34;
        for (String descriptionKey : entry.descriptionKeys()) {
            for (FormattedCharSequence wrapped : this.font.split(Component.translatable(descriptionKey), 174)) {
                graphics.drawString(this.font, wrapped, x, line, 0xFFB8FFB8, false);
                line += 11;
                if (line > y + 125) {
                    return;
                }
            }
            line += 5;
        }
        if (entry.type().equals("item") && !entry.itemId().isEmpty()) {
            try {
                ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.itemId())));
                graphics.renderItem(stack, x + 130, y + 122);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;
        for (int index = 0; index < visibleEntries.size() && index < 6; index++) {
            if (isMouseOver(left + 12, top + 66 + index * 20, 98, 16, mouseX, mouseY)) {
                selectedIndex = index;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
