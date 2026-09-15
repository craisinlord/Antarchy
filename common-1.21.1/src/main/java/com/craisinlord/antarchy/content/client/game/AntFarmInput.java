package com.craisinlord.antarchy.content.client.game;

import org.lwjgl.glfw.GLFW;

public final class AntFarmInput {
    private AntFarmInput() {
    }

    public static AntFarmGame.Direction directionForKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> AntFarmGame.Direction.UP;
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> AntFarmGame.Direction.DOWN;
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> AntFarmGame.Direction.LEFT;
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> AntFarmGame.Direction.RIGHT;
            default -> AntFarmGame.Direction.NONE;
        };
    }

    public static boolean isStartOrRetry(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
    }
}
