package com.craisinlord.antarchy.content.client.game;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AntmanRenderer {
    private static final int BLACK = 0xFF020602;
    private static final int DARK_GREEN = 0xFF123012;
    private static final int GREEN = 0xFF65FF65;
    private static final int PALE_GREEN = 0xFFB8FFB8;
    private static final int ANT_BODY = 0xFFC98A3D;
    private static final int GHOST_RED = 0xFFFF4A4A;
    private static final int GHOST_PINK = 0xFFFF8FD2;
    private static final int GHOST_FRIGHTENED = 0xFF4A8CFF;
    private static final int GHOST_EYE = 0xFFF8FFFF;
    private AntmanRenderer() { }

    public static void render(GuiGraphics graphics, Font font, AntmanGame game, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BLACK);
        graphics.drawString(font, "SCORE " + String.format("%04d", game.score()), x + 5, y + 4, GREEN, false);
        graphics.drawString(font, "HI " + String.format("%04d", game.highScore()), x + width - 55, y + 4, PALE_GREEN, false);
        graphics.drawString(font, "LIVES " + game.lives(), x + 5, y + height - 13, PALE_GREEN, false);
        int tile = Math.max(1, Math.min((width - 8) / game.width(), (height - 34) / game.height()));
        int boardWidth = tile * game.width();
        int boardHeight = tile * game.height();
        int boardX = x + (width - boardWidth) / 2;
        int boardY = y + 20 + (height - 34 - boardHeight) / 2;
        for (int cellY = 0; cellY < game.height(); cellY++) for (int cellX = 0; cellX < game.width(); cellX++) if (game.isWall(cellX, cellY)) graphics.fill(boardX + cellX * tile, boardY + cellY * tile, boardX + (cellX + 1) * tile, boardY + (cellY + 1) * tile, DARK_GREEN);
        renderItem(graphics, new ItemStack(Items.DIAMOND), boardX + game.diamondX() * tile, boardY + game.diamondY() * tile, tile);
        if (game.cornDogX() >= 0) renderItem(graphics, new ItemStack(AntarchyObjects.COOKED_CORNDOG.get()), boardX + game.cornDogX() * tile, boardY + game.cornDogY() * tile, tile);
        int playerX = boardX + (int) (game.playerX() * tile);
        int playerY = boardY + (int) (game.playerY() * tile);
        drawAnt(graphics, playerX, playerY, tile, game.animationFrame(), ANT_BODY, game.direction());
        for (AntmanGame.Enemy enemy : game.enemies()) {
            int enemyX = boardX + (int) (enemy.x() * tile);
            int enemyY = boardY + (int) (enemy.y() * tile);
            int color = game.cornDogTicks() > 0 ? GHOST_FRIGHTENED : (enemy.kind() == AntmanGame.EnemyKind.CREEPER ? GHOST_RED : GHOST_PINK);
            drawGhost(graphics, enemyX, enemyY, tile, game.animationFrame(), color);
        }
        if (game.phase() != AntmanGame.Phase.PLAYING) {
            String title = game.phase() == AntmanGame.Phase.READY ? "ANTMAN.EXE" : "GAME OVER";
            String prompt = game.phase() == AntmanGame.Phase.READY ? "ENTER TO START" : "ENTER TO RETRY";
            int centerY = y + height / 2;
            graphics.fill(x + 4, centerY - 16, x + width - 4, centerY + 16, BLACK);
            graphics.drawString(font, title, x + (width - font.width(title)) / 2, centerY - 12, GREEN, false);
            graphics.drawString(font, prompt, x + (width - font.width(prompt)) / 2, centerY + 2, PALE_GREEN, false);
        }
    }

    private static void drawAnt(GuiGraphics graphics, int centerX, int centerY, int tile, int frame, int color, AntmanGame.Direction direction) {
        int size = Math.max(4, tile - 1);
        int left = centerX - size / 2;
        int top = centerY - size / 2;
        int segment = Math.max(1, size / 4);
        graphics.fill(left + segment * 2, top + segment, left + segment * 3, top + segment * 3, color);
        graphics.fill(left + segment, top + segment * 2, left + segment * 4, top + segment * 4, color);
        graphics.fill(left + segment * 3, top + segment * 2, left + segment * 5, top + segment * 4, color);
        int legOffset = frame == 0 ? 1 : -1;
        graphics.fill(left + segment, top + segment * 3, left + segment * 2, top + segment * 4 + legOffset, color);
        graphics.fill(left + segment * 3, top + segment * 3, left + segment * 4, top + segment * 4 - legOffset, color);
        if (direction == AntmanGame.Direction.LEFT || direction == AntmanGame.Direction.RIGHT) {
            int headX = direction == AntmanGame.Direction.LEFT ? left : left + segment * 4;
            graphics.fill(headX, top + segment, headX + segment, top + segment * 2, BLACK);
        }
    }

    private static void drawGhost(GuiGraphics graphics, int centerX, int centerY, int tile, int frame, int color) {
        int size = Math.max(4, tile - 1);
        int left = centerX - size / 2;
        int top = centerY - size / 2;
        int eyeWidth = Math.max(1, size / 5);
        int bodyBottom = top + size - 1;
        graphics.fill(left + 1, top, left + size - 1, bodyBottom - 1, color);
        graphics.fill(left, top + 2, left + size, bodyBottom - 2, color);
        int wave = frame == 0 ? 0 : 1;
        graphics.fill(left, bodyBottom - 1 - wave, left + size / 3, bodyBottom + 1, color);
        graphics.fill(left + size / 3, bodyBottom - wave, left + size * 2 / 3, bodyBottom + 1, color);
        graphics.fill(left + size * 2 / 3, bodyBottom - 1 - wave, left + size, bodyBottom + 1, color);
        int eyeY = top + Math.max(1, size / 3);
        graphics.fill(left + size / 3 - eyeWidth / 2, eyeY, left + size / 3 + eyeWidth, eyeY + eyeWidth + 1, GHOST_EYE);
        graphics.fill(left + size * 2 / 3 - eyeWidth / 2, eyeY, left + size * 2 / 3 + eyeWidth, eyeY + eyeWidth + 1, GHOST_EYE);
        graphics.fill(left + size / 3, eyeY + 1, left + size / 3 + 1, eyeY + eyeWidth + 1, BLACK);
        graphics.fill(left + size * 2 / 3, eyeY + 1, left + size * 2 / 3 + 1, eyeY + eyeWidth + 1, BLACK);
    }

    private static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, int tile) {
        float scale = Math.min(1.0F, Math.max(0.45F, tile / 16.0F));
        graphics.pose().pushPose();
        graphics.pose().translate(x + tile / 2.0F, y + tile / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        RenderSystem.setShaderColor(0.38F, 1.0F, 0.38F, 1.0F);
        graphics.renderItem(stack, -8, -8);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();
    }
}
