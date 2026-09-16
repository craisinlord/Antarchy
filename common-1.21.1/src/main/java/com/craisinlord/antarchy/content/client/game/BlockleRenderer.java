package com.craisinlord.antarchy.content.client.game;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class BlockleRenderer {
    private static final int EMPTY = 0xFF102010;
    private static final int GREY = 0xFF3A403A;
    private static final int YELLOW = 0xFFB59F3B;
    private static final int GREEN = 0xFF538D4E;
    private static final int PALE = 0xFFB8FFB8;

    private BlockleRenderer() {
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, int width, int height,
                              List<String> guesses, List<String> colors, String input, String status, String phase,
                              ResourceLocation answerId) {
        graphics.drawString(font, Component.literal("BLOCKLE"), x, y, 0xFF65FF65, false);
        int cell = 18;
        int gap = 3;
        int boardX = x + Math.max(0, (width - cell * 5 - gap * 4) / 2);
        int boardY = y + 18;
        for (int row = 0; row < 6; row++) {
            String guess = row < guesses.size() ? guesses.get(row) : row == guesses.size() ? input : "";
            String rowColors = row < colors.size() ? colors.get(row) : "";
            for (int column = 0; column < 5; column++) {
                int left = boardX + column * (cell + gap);
                int top = boardY + row * (cell + gap);
                int fill = EMPTY;
                if (column < rowColors.length()) fill = switch (rowColors.charAt(column)) {
                    case 'G' -> GREEN;
                    case 'Y' -> YELLOW;
                    default -> GREY;
                };
                graphics.fill(left, top, left + cell, top + cell, fill);
                if (column < guess.length()) {
                    String letter = Character.toString(Character.toUpperCase(guess.charAt(column)));
                    graphics.drawCenteredString(font, letter, left + cell / 2, top + 5, PALE);
                }
            }
        }
        String message = status.isBlank() ? "ENTER TO SUBMIT" : status;
        if (phase.equals("PLAYING") && input.length() < 5) message = input.length() + "/5 LETTERS";
        graphics.drawString(font, Component.literal(message), x, y + 145, PALE, false);
        if (!phase.equals("PLAYING") && answerId != null) {
            ItemStack answerStack = BuiltInRegistries.ITEM.getOptional(answerId).map(ItemStack::new).orElse(ItemStack.EMPTY);
            if (!answerStack.isEmpty()) {
                graphics.setColor(0.35F, 1.0F, 0.35F, 1.0F);
                graphics.renderItem(answerStack, x + width - 28, y + 137);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}
