package com.craisinlord.antarchy.content.client.game;

import com.craisinlord.antos.api.client.game.ComputerGame;
import com.craisinlord.antos.api.client.game.ComputerGameRegistry;
import com.craisinlord.antarchy.content.network.AntarchyGameNetworking;
import com.craisinlord.antarchy.content.network.AntarchyGamePayload;
import com.craisinlord.antarchy.content.client.AntarchyGameClientState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/** Registers Antarchy's native games with the AntOS Games app. */
public final class AntarchyComputerGames {
    private AntarchyComputerGames() {}

    public static void register() {
        registerIfMissing(game("basilisk", "BASILISK", pos -> {
            BasiliskProgram program = new BasiliskProgram(true, 0, 0, score -> AntarchyGameNetworking.saveBasiliskScore(pos, score));
            var loaded = new java.util.concurrent.atomic.AtomicBoolean();
            AntarchyGameNetworking.requestBasiliskState(pos);
            return new GameSession(() -> { if (!loaded.get()) loaded.set(loadScores(pos, AntarchyGamePayload.BASILISK_STATE, program::setStoredScores)); program.tick(); }, program::render, key -> loaded.get() && program.keyPressed(key), ignored -> false);
        }));
    }

    private static void registerIfMissing(ComputerGame game) {
        if (ComputerGameRegistry.get(game.id()) == null) ComputerGameRegistry.register(game);
    }

    private static boolean loadScores(BlockPos pos, int action, java.util.function.BiConsumer<Integer, Integer> setter) {
        String response = AntarchyGameClientState.getGameState(pos, action);
        if (response == null) return false;
        if (response.isBlank()) { AntarchyGameClientState.clearGameState(pos, action); return true; }
        String[] scores = response.split("\0", 2);
        if (scores.length != 2) return false;
        try { setter.accept(Integer.parseInt(scores[0]), Integer.parseInt(scores[1])); }
        catch (NumberFormatException ignored) { return false; }
        AntarchyGameClientState.clearGameState(pos, action);
        return true;
    }

    private static ComputerGame game(String path, String title, ProgramFactory factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("antarchy", path);
        ResourceLocation disk = ResourceLocation.fromNamespaceAndPath("antarchy", path + "_game");
        return new ComputerGame() {
            @Override public ResourceLocation id() { return id; }
            @Override public ResourceLocation diskId() { return disk; }
            @Override public String title() { return title; }
            @Override public Session create(BlockPos position) { return factory.create(position); }
        };
    }

    @FunctionalInterface
    private interface ProgramFactory { ComputerGame.Session create(BlockPos position); }

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
