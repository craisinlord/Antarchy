package com.craisinlord.antarchy.content.client.game;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class AntFarmRenderer {
    private static final int BLACK = 0xFF020602;
    private static final int DARK_GREEN = 0xFF123012;
    private static final int GREEN = 0xFF65FF65;
    private static final int PALE_GREEN = 0xFFB8FFB8;

    private AntFarmRenderer() {
    }

    public static void render(GuiGraphics graphics, Font font, AntFarmGame game, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BLACK);
        int hudHeight = 18;
        int boardX = x + 5;
        int boardY = y + hudHeight + 4;
        int boardWidth = width - 10;
        int boardHeight = height - hudHeight - 9;
        int tileSize = Math.max(1, Math.min(boardWidth / game.width(), boardHeight / game.height()));
        int mazeWidth = tileSize * game.width();
        int mazeHeight = tileSize * game.height();
        int mazeX = boardX + Math.max(0, (boardWidth - mazeWidth) / 2);
        int mazeY = boardY + Math.max(0, (boardHeight - mazeHeight) / 2);
        graphics.drawString(font, Component.literal("SCORE " + String.format("%06d", game.score())), x + 6, y + 4, GREEN, false);
        graphics.drawString(font, Component.literal("FOOD " + String.format("%03d", game.remainingFood())), x + width - 76, y + 4, PALE_GREEN, false);
        for (int gridY = 0; gridY < game.height(); gridY++) {
            for (int gridX = 0; gridX < game.width(); gridX++) {
                int cellX = mazeX + gridX * tileSize;
                int cellY = mazeY + gridY * tileSize;
                if (game.isWall(gridX, gridY)) {
                    graphics.fill(cellX, cellY, cellX + tileSize, cellY + tileSize, DARK_GREEN);
                } else if (game.hasPellet(gridX, gridY)) {
                    int pelletSize = Math.max(1, tileSize / 4);
                    int offset = (tileSize - pelletSize) / 2;
                    graphics.fill(cellX + offset, cellY + offset, cellX + offset + pelletSize, cellY + offset + pelletSize, PALE_GREEN);
                }
            }
        }
        drawAnt(graphics, mazeX + game.playerX() * tileSize, mazeY + game.playerY() * tileSize, tileSize, GREEN);
        drawAnt(graphics, mazeX + game.enemyX() * tileSize, mazeY + game.enemyY() * tileSize, tileSize, PALE_GREEN);
        if (game.phase() != AntFarmGame.Phase.PLAYING) {
            String message = switch (game.phase()) {
                case READY -> "ANT FARM // PRESS ENTER TO START";
                case WON -> "COLONY FED // ENTER TO PLAY AGAIN";
                case LOST -> "COLONY COMPROMISED // ENTER TO RETRY";
                case PLAYING -> "";
            };
            int messageWidth = font.width(message);
            graphics.fill(x + 8, y + height / 2 - 12, x + width - 8, y + height / 2 + 12, BLACK);
            graphics.drawString(font, Component.literal(message), x + Math.max(8, (width - messageWidth) / 2), y + height / 2 - 4, GREEN, false);
        }
    }

    private static void drawAnt(GuiGraphics graphics, int x, int y, int tileSize, int color) {
        int inset = Math.max(1, tileSize / 4);
        int center = x + tileSize / 2;
        graphics.fill(center - inset, y + inset, center + inset + 1, y + tileSize - inset, color);
        graphics.fill(x + inset, y + tileSize / 2 - inset, x + tileSize - inset, y + tileSize / 2 + inset + 1, color);
        if (tileSize >= 5) graphics.fill(center - inset, y + inset, center + inset + 1, y + inset + 1, BLACK);
    }
}
