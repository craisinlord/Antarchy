package com.craisinlord.antarchy.content.client.game;

import com.mojang.blaze3d.platform.InputConstants;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.craisinlord.antos.content.client.ThemedGraphics;
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
            case InputConstants.KEY_UP, InputConstants.KEY_W -> BasiliskGame.Direction.UP;
            case InputConstants.KEY_DOWN, InputConstants.KEY_S -> BasiliskGame.Direction.DOWN;
            case InputConstants.KEY_LEFT, InputConstants.KEY_A -> BasiliskGame.Direction.LEFT;
            case InputConstants.KEY_RIGHT, InputConstants.KEY_D -> BasiliskGame.Direction.RIGHT;
            default -> null;
        };
        if (direction != null) { game.start(); game.setDirection(direction); return true; }
        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) { if (game.phase() != BasiliskGame.Phase.PLAYING) { game.reset(); game.start(); savedScore = 0; scoreSaver.accept(0); } return true; }
        return false;
    }
    public void render(ThemedGraphics graphics, Font font, int x, int y, int width, int height) { BasiliskRenderer.render(graphics, font, game, x, y, width, height); }
}
