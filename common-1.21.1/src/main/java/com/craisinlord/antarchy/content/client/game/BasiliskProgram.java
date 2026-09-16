package com.craisinlord.antarchy.content.client.game;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.function.IntConsumer;

public final class BasiliskProgram {
    private final BasiliskGame game;
    private boolean installed;
    private int savedScore;
    private int savedHighScore;
    private final IntConsumer scoreSaver;

    public BasiliskProgram(boolean installed, int highScore, int score, IntConsumer scoreSaver) {
        this.installed = installed;
        this.savedHighScore = highScore;
        this.savedScore = score;
        this.game = new BasiliskGame(highScore, score);
        this.scoreSaver = scoreSaver;
    }

    public void setInstalled(boolean installed) { this.installed = installed; if (!installed) game.reset(); }
    public boolean installed() { return installed; }
    public BasiliskGame game() { return game; }
    public void setStoredScores(int score, int highScore) { savedScore = score; savedHighScore = highScore; game.reset(score); }
    public void tick() { if (game.tick() && (game.score() != savedScore || game.highScore() != savedHighScore)) { if (game.score() > savedScore) Minecraft.getInstance().player.playSound(AntarchySoundEvents.BASILISK_BITE.get(), 0.5F, 1.2F); savedScore = game.score(); savedHighScore = game.highScore(); scoreSaver.accept(savedScore); } }
    public boolean keyPressed(int keyCode) {
        if (!installed) return false;
        BasiliskGame.Direction direction = switch (keyCode) {
            case 265, 87 -> BasiliskGame.Direction.UP;
            case 264, 83 -> BasiliskGame.Direction.DOWN;
            case 263, 65 -> BasiliskGame.Direction.LEFT;
            case 262, 68 -> BasiliskGame.Direction.RIGHT;
            default -> null;
        };
        if (direction != null) { game.start(); game.setDirection(direction); return true; }
        if (keyCode == 257 || keyCode == 335) { if (game.phase() != BasiliskGame.Phase.PLAYING) { game.reset(); game.start(); savedScore = 0; scoreSaver.accept(0); } return true; }
        return false;
    }
    public void render(GuiGraphics graphics, Font font, int x, int y, int width, int height) { BasiliskRenderer.render(graphics, font, game, x, y, width, height); }
}
