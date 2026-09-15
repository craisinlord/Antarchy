package com.craisinlord.antarchy.content.client.game;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class AntFarmProgram {
    private final AntFarmGame game = new AntFarmGame();
    private boolean gameInstalled;

    public AntFarmProgram(boolean gameInstalled) {
        this.gameInstalled = gameInstalled;
    }

    public void setGameInstalled(boolean gameInstalled) {
        this.gameInstalled = gameInstalled;
        if (!gameInstalled) game.reset();
    }

    public boolean isGameInstalled() { return gameInstalled; }
    public AntFarmGame game() { return game; }

    public void tick() {
        if (gameInstalled) game.tick();
    }

    public boolean keyPressed(int keyCode) {
        if (!gameInstalled) return false;
        AntFarmGame.Direction direction = AntFarmInput.directionForKey(keyCode);
        if (direction != AntFarmGame.Direction.NONE) {
            if (game.phase() == AntFarmGame.Phase.READY) game.start();
            game.setRequestedDirection(direction);
            return true;
        }
        if (AntFarmInput.isStartOrRetry(keyCode)) {
            if (game.phase() == AntFarmGame.Phase.READY) game.start();
            else if (game.phase() == AntFarmGame.Phase.WON || game.phase() == AntFarmGame.Phase.LOST) {
                game.reset();
                game.start();
            }
            return true;
        }
        return false;
    }

    public void render(GuiGraphics graphics, Font font, int x, int y, int width, int height) {
        if (!gameInstalled) {
            graphics.fill(x, y, x + width, y + height, 0xFF020602);
            graphics.drawString(font, "ANT FARM NOT INSTALLED", x + 12, y + height / 2, 0xFF65FF65, false);
            return;
        }
        AntFarmRenderer.render(graphics, font, game, x, y, width, height);
    }
}
