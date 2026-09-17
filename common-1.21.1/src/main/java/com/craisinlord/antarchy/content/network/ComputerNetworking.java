package com.craisinlord.antarchy.content.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public final class ComputerNetworking {
    private static Consumer<ComputerAccessPayload> sender = payload -> {
    };

    private ComputerNetworking() {
    }

    public static void setSender(Consumer<ComputerAccessPayload> sender) {
        ComputerNetworking.sender = sender;
    }

    public static void open(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.OPEN));
    }

    public static void setup(BlockPos pos, String password) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.SETUP, password));
    }

    public static void login(BlockPos pos, String password) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.LOGIN, password));
    }

    public static void logout(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.LOGOUT));
    }

    public static void close(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.CLOSE));
    }

    public static void changePassword(BlockPos pos, String password) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.CHANGE_PASSWORD, password));
    }

    public static void eject(BlockPos pos, ResourceLocation diskId) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.EJECT, diskId.toString()));
    }

    public static void listFiles(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.FILE_LIST));
    }

    public static void openFile(BlockPos pos, String path) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.FILE_OPEN, path));
    }

    public static void createFile(BlockPos pos, String path, String contents) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.FILE_CREATE, path + "\0" + contents));
    }

    public static void saveFile(BlockPos pos, String path, String contents) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.FILE_SAVE, path + "\0" + contents));
    }

    public static void deleteFile(BlockPos pos, String path) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.FILE_DELETE, path));
    }

    public static void moveFile(BlockPos pos, String source, String destination) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.FILE_MOVE, source + "\0" + destination));
    }

    public static void terminalCommand(BlockPos pos, String directory, String command) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.TERMINAL_COMMAND, directory + "\0" + command));
    }

    public static void requestDesktopState(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.DESKTOP_STATE));
    }

    public static void selectWallpaper(BlockPos pos, ResourceLocation id) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.DESKTOP_WALLPAPER, id.toString()));
    }

    public static void requestBasiliskState(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.BASILISK_STATE));
    }

    public static void saveBasiliskScore(BlockPos pos, int score) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.BASILISK_STATE, Integer.toString(Math.max(0, score))));
    }

    public static void requestAntmanState(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.ANTMAN_STATE));
    }

    public static void saveAntmanScore(BlockPos pos, int score) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.ANTMAN_STATE, Integer.toString(Math.max(0, score))));
    }

    public static void requestBlockleState(BlockPos pos) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.BLOCKLE_STATE));
    }

    public static void submitBlockleGuess(BlockPos pos, String guess) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.BLOCKLE_GUESS, guess));
    }

    public static void locateStructure(BlockPos pos, ResourceLocation entryId) {
        sender.accept(new ComputerAccessPayload(pos, ComputerAccessPayload.LOCATE_STRUCTURE, entryId.toString()));
    }
}
