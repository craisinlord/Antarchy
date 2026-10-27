package com.craisinlord.antarchy.content.client.game;

import com.mojang.blaze3d.platform.InputConstants;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.craisinlord.antos.content.client.ThemedGraphics;
import net.minecraft.sounds.SoundEvents;
import java.util.function.IntConsumer;

public final class AntmanProgram {
    private final AntmanGame game;
    private final IntConsumer scoreSaver;
    private boolean installed;
    private int savedScore;
    private int savedHighScore;

    public AntmanProgram(boolean installed, int highScore, int score, IntConsumer scoreSaver) {
        this.installed = installed;
        this.savedScore = score;
        this.savedHighScore = highScore;
        this.game = new AntmanGame(highScore, score);
        this.scoreSaver = scoreSaver;
    }

    public void setInstalled(boolean installed) { this.installed = installed; if (!installed) game.reset(); }
    public boolean installed() { return installed; }
    public AntmanGame game() { return game; }
    public void setStoredScores(int score, int highScore) { savedScore = score; savedHighScore = highScore; game.reset(score); }
    public void tick() {
        if (!game.tick()) return;
        if (game.collectedDiamond() && Minecraft.getInstance().player != null) Minecraft.getInstance().player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 1.4F);
        if (game.score() != savedScore || game.highScore() != savedHighScore) { savedScore = game.score(); savedHighScore = game.highScore(); scoreSaver.accept(savedScore); }
    }
    public boolean keyPressed(int keyCode) {
        if (!installed) return false;
        AntmanGame.Direction direction = switch (keyCode) {
            case InputConstants.KEY_UP, InputConstants.KEY_W -> AntmanGame.Direction.UP;
            case InputConstants.KEY_DOWN, InputConstants.KEY_S -> AntmanGame.Direction.DOWN;
            case InputConstants.KEY_LEFT, InputConstants.KEY_A -> AntmanGame.Direction.LEFT;
            case InputConstants.KEY_RIGHT, InputConstants.KEY_D -> AntmanGame.Direction.RIGHT;
            default -> null;
        };
        if (direction != null) { game.start(); game.setDirection(direction); return true; }
        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) { if (game.phase() != AntmanGame.Phase.PLAYING) { game.reset(); game.start(); savedScore = 0; scoreSaver.accept(0); } return true; }
        return false;
    }
    public void render(ThemedGraphics graphics, Font font, int x, int y, int width, int height) { AntmanRenderer.render(graphics, font, game, x, y, width, height); }
}
