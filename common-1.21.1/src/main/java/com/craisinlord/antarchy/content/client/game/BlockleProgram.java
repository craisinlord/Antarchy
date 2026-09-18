package com.craisinlord.antarchy.content.client.game;

import com.craisinlord.antarchy.content.client.BlockleClientState;
import com.craisinlord.antarchy.content.network.AntarchyGameResultPayload;
import com.craisinlord.antarchy.content.network.AntarchyGameNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class BlockleProgram {
    private final BlockPos position;
    private final List<String> guesses = new ArrayList<>();
    private final List<String> colors = new ArrayList<>();
    private String input = "";
    private String status = "LOADING...";
    private String phase = "PLAYING";
    private String answer = "";
    private ResourceLocation answerId;
    private long day = -1L;
    private boolean installed;
    private boolean pending;
    private boolean received;
    private String lastResponse = "";

    public BlockleProgram(BlockPos position) {
        this.position = position;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
        if (!installed) clear();
    }

    public boolean installed() {
        return installed;
    }

    public void requestState() {
        if (installed) AntarchyGameNetworking.requestBlockleState(position);
    }

    public void tick() {
        AntarchyGameResultPayload result = BlockleClientState.get(position);
        if (result == null) return;
        if (result.data().equals(lastResponse)) return;
        lastResponse = result.data();
        String[] fields = result.data().split("\u0000", 3);
        if (fields.length < 2 || (!fields[0].equals("18") && !fields[0].equals("19"))) return;
        if (!fields[1].isBlank()) {
            status = fields[1].toUpperCase();
            pending = false;
            return;
        }
        if (fields.length == 3) {
            parse(fields[2]);
            pending = false;
            input = "";
            received = true;
        }
    }

    public boolean keyPressed(int keyCode) {
        if (!installed) return false;
        if (keyCode == 259) {
            if (!input.isEmpty()) input = input.substring(0, input.length() - 1);
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (!received) return true;
            if (input.length() == 5 && phase.equals("PLAYING") && !pending) {
                AntarchyGameNetworking.submitBlockleGuess(position, input);
                pending = true;
                status = "SUBMITTING";
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint) {
        if (!installed || !received || phase.equals("SOLVED") || phase.equals("FAILED")) return false;
        if (codePoint >= 'A' && codePoint <= 'Z') codePoint = Character.toLowerCase(codePoint);
        if (codePoint >= 'a' && codePoint <= 'z' && input.length() < 5) {
            input += codePoint;
            status = "";
            return true;
        }
        return false;
    }

    public void render(GuiGraphics graphics, Font font, int x, int y, int width, int height) {
        BlockleRenderer.render(graphics, font, x, y, width, height, guesses, colors, input, status, phase, answerId);
    }

    private void parse(String encoded) {
        String[] values = encoded.split("\\|");
        if (values.length == 0) return;
        try { day = Long.parseLong(values[0]); } catch (NumberFormatException ignored) { return; }
        guesses.clear();
        colors.clear();
        phase = "PLAYING";
        answer = "";
        for (int index = 1; index < values.length; index++) {
            String value = values[index];
            if (value.equals("PLAYING") || value.equals("SOLVED") || value.equals("FAILED")) {
                phase = value;
                if (index + 1 < values.length && (phase.equals("SOLVED") || phase.equals("FAILED"))) {
                    answer = values[index + 1];
                    if (index + 2 < values.length) {
                        try { answerId = ResourceLocation.parse(values[index + 2]); } catch (IllegalArgumentException ignored) { answerId = null; }
                    }
                }
                break;
            }
            String[] guess = value.split(",", 2);
            if (guess.length == 2 && guess[0].length() == 5 && guess[1].length() == 5) {
                guesses.add(guess[0]);
                colors.add(guess[1]);
            }
        }
        status = phase.equals("FAILED") && !answer.isBlank() ? "ANSWER: " + answer.toUpperCase() : phase.equals("PLAYING") ? "" : phase;
    }

    private void clear() {
        guesses.clear();
        colors.clear();
        input = "";
        status = "LOADING...";
        phase = "PLAYING";
        answer = "";
        answerId = null;
        day = -1L;
        pending = false;
        received = false;
        lastResponse = "";
    }
}
