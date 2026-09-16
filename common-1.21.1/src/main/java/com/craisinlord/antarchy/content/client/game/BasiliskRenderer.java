package com.craisinlord.antarchy.content.client.game;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class BasiliskRenderer {
    private static final int BLACK = 0xFF020602;
    private static final int DARK_GREEN = 0xFF123012;
    private static final int GREEN = 0xFF65FF65;
    private static final int PALE_GREEN = 0xFFB8FFB8;
    private BasiliskRenderer() { }

    public static void render(GuiGraphics graphics, Font font, BasiliskGame game, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BLACK);
        graphics.drawString(font, "SCORE " + String.format("%04d", game.score()), x + 6, y + 4, GREEN, false);
        graphics.drawString(font, "HI " + String.format("%04d", game.highScore()), x + width - 56, y + 4, PALE_GREEN, false);
        int tile = Math.max(1, Math.min((width - 10) / game.width(), (height - 28) / game.height()));
        int boardWidth = tile * game.width();
        int boardHeight = tile * game.height();
        int boardX = x + (width - boardWidth) / 2;
        int boardY = y + 22 + (height - 22 - boardHeight) / 2;
        graphics.fill(boardX, boardY, boardX + boardWidth, boardY + boardHeight, DARK_GREEN);
        graphics.fill(boardX + 1, boardY + 1, boardX + boardWidth - 1, boardY + boardHeight - 1, BLACK);
        int appleX = boardX + game.foodX() * tile;
        int appleY = boardY + game.foodY() * tile;
        int appleInset = Math.max(1, tile / 4);
        graphics.fill(appleX + appleInset, appleY + appleInset + 1, appleX + tile - appleInset, appleY + tile - appleInset, PALE_GREEN);
        if (tile >= 5) graphics.fill(appleX + tile / 2, appleY + 1, appleX + tile / 2 + 1, appleY + tile / 3 + 1, GREEN);
        for (BasiliskGame.Part part : game.body()) graphics.fill(boardX + part.x() * tile + 1, boardY + part.y() * tile + 1, boardX + (part.x() + 1) * tile - 1, boardY + (part.y() + 1) * tile - 1, GREEN);
        if (game.phase() != BasiliskGame.Phase.PLAYING) {
            String title = game.phase() == BasiliskGame.Phase.READY ? "BASILISK" : "GAME OVER";
            String prompt = game.phase() == BasiliskGame.Phase.READY ? "ENTER TO START" : "ENTER TO RETRY";
            int centerY = y + height / 2;
            graphics.fill(x + 4, centerY - 16, x + width - 4, centerY + 16, BLACK);
            graphics.drawString(font, title, x + (width - font.width(title)) / 2, centerY - 12, GREEN, false);
            graphics.drawString(font, prompt, x + (width - font.width(prompt)) / 2, centerY + 2, PALE_GREEN, false);
        }
    }
}
