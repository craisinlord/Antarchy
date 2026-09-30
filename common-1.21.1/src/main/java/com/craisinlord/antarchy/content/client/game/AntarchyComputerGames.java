package com.craisinlord.antarchy.content.client.game;

import com.craisinlord.antos.api.client.game.ComputerGame;
import com.craisinlord.antos.api.client.game.ComputerGameRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/** Registers Antarchy's native games with the AntOS Games app. */
public final class AntarchyComputerGames {
    private AntarchyComputerGames() {}

    public static void register() {
        registerIfMissing(game("basilisk", "BASILISK", () -> {
            // AntOS 1.2.1 creates game sessions without passing the computer position.
            // Keep Basilisk playable without a position-bound score callback.
            BasiliskProgram program = new BasiliskProgram(true, 0, 0, score -> {});
            return new GameSession(program::tick, program::render, program::keyPressed, ignored -> false);
        }));
    }

    private static void registerIfMissing(ComputerGame game) {
        if (ComputerGameRegistry.get(game.id()) == null) ComputerGameRegistry.register(game);
    }

    private static ComputerGame game(String path, String title, ProgramFactory factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("antarchy", path);
        ResourceLocation disk = ResourceLocation.fromNamespaceAndPath("antarchy", path + "_game");
        return new ComputerGame() {
            @Override public ResourceLocation id() { return id; }
            @Override public ResourceLocation diskId() { return disk; }
            @Override public String title() { return title; }
            @Override public Session create() { return factory.create(); }
        };
    }

    @FunctionalInterface
    private interface ProgramFactory { ComputerGame.Session create(); }

    @FunctionalInterface private interface GameRenderer { void render(GuiGraphics graphics, Font font, int x, int y, int width, int height); }
    private static final class GameSession implements ComputerGame.Session {
        private final Runnable ticker;
        private final GameRenderer renderer;
        private final IntPredicate keyHandler;
        private final Predicate<Character> charHandler;
        private GameSession(Runnable ticker, GameRenderer renderer, IntPredicate keyHandler, Predicate<Character> charHandler) {
            this.ticker = ticker; this.renderer = renderer; this.keyHandler = keyHandler; this.charHandler = charHandler;
        }
        @Override public void tick() { ticker.run(); }
        @Override public void render(GuiGraphics graphics, Font font, int x, int y, int width, int height) { renderer.render(graphics, font, x, y, width, height); }
        @Override public boolean keyPressed(int keyCode) { return keyHandler.test(keyCode); }
        @Override public boolean charTyped(char codePoint) { return charHandler.test(codePoint); }
    }
}
