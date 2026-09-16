package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import com.craisinlord.antarchy.content.computer.ComputerFileSystem;
import com.craisinlord.antarchy.content.computer.terminal.TerminalCommandService;
import com.craisinlord.antarchy.content.computer.terminal.TerminalFileSystem;
import com.craisinlord.antarchy.content.computer.terminal.TerminalResult;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import com.craisinlord.antarchy.content.computer.blockle.BlockleAnswers;
import com.craisinlord.antarchy.content.computer.blockle.BlockleDictionary;
import com.craisinlord.antarchy.content.computer.blockle.BlockleGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BiConsumer;

public final class ComputerAccessHandler {
    private static BiConsumer<ServerPlayer, ComputerAccessResultPayload> resultSender = (player, result) -> {
    };

    private ComputerAccessHandler() {
    }

    public static void setResultSender(BiConsumer<ServerPlayer, ComputerAccessResultPayload> sender) {
        resultSender = sender;
    }

    public static void handle(ServerPlayer player, ComputerAccessPayload payload) {
        if (payload.value().length() > 65536 || player.level().isClientSide || !player.serverLevel().hasChunkAt(payload.pos()) ||
                player.distanceToSqr(payload.pos().getX() + 0.5D, payload.pos().getY() + 0.5D, payload.pos().getZ() + 0.5D) > 64.0D) {
            return;
        }
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(payload.pos());
        if (!(blockEntity instanceof ComputerBlockEntity computer)) {
            send(player, payload, ComputerAccessResultPayload.INVALID);
            return;
        }
        switch (payload.action()) {
            case ComputerAccessPayload.OPEN -> open(player, computer, payload);
            case ComputerAccessPayload.SETUP -> setup(player, computer, payload);
            case ComputerAccessPayload.LOGIN -> login(player, computer, payload);
            case ComputerAccessPayload.LOGOUT -> {
                computer.logout();
                send(player, payload, ComputerAccessResultPayload.READY);
            }
            case ComputerAccessPayload.CLOSE -> {
                computer.releaseUser(player);
                send(player, payload, computer.isAuthenticated() ? ComputerAccessResultPayload.SUCCESS : ComputerAccessResultPayload.READY);
            }
            case ComputerAccessPayload.CHANGE_PASSWORD -> {
                boolean changed = computer.setPassword(player, payload.value());
                send(player, payload, changed ? ComputerAccessResultPayload.SUCCESS : ComputerAccessResultPayload.INVALID_PASSWORD);
            }
            case ComputerAccessPayload.EJECT -> eject(player, computer, payload);
            case ComputerAccessPayload.FILE_LIST -> fileList(player, computer, payload);
            case ComputerAccessPayload.FILE_OPEN -> fileOpen(player, computer, payload);
            case ComputerAccessPayload.FILE_CREATE -> fileCreate(player, computer, payload);
            case ComputerAccessPayload.FILE_SAVE -> fileSave(player, computer, payload);
            case ComputerAccessPayload.FILE_DELETE -> fileDelete(player, computer, payload);
            case ComputerAccessPayload.FILE_MOVE -> fileMove(player, computer, payload);
            case ComputerAccessPayload.TERMINAL_COMMAND -> terminalCommand(player, computer, payload);
            case ComputerAccessPayload.DESKTOP_STATE -> desktopState(player, computer, payload);
            case ComputerAccessPayload.DESKTOP_WALLPAPER -> desktopWallpaper(player, computer, payload);
            case ComputerAccessPayload.BASILISK_STATE -> basiliskState(player, computer, payload);
            case ComputerAccessPayload.ANTMAN_STATE -> antmanState(player, computer, payload);
            case ComputerAccessPayload.BLOCKLE_STATE -> blockleState(player, computer, payload, false);
            case ComputerAccessPayload.BLOCKLE_GUESS -> blockleState(player, computer, payload, true);
            default -> send(player, payload, ComputerAccessResultPayload.INVALID);
        }
    }

    private static void open(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (computer.hasActiveUser() && !computer.isAuthenticatedBy(player)) {
            send(player, payload, ComputerAccessResultPayload.BUSY);
            return;
        }
        if (!computer.hasPassword()) {
            computer.claimUser(player);
            send(player, payload, ComputerAccessResultPayload.SETUP_REQUIRED);
        } else if (computer.isAuthenticated()) {
            computer.claimUser(player);
            send(player, payload, ComputerAccessResultPayload.SUCCESS);
        } else {
            send(player, payload, ComputerAccessResultPayload.READY);
        }
    }

    private static void setup(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.hasPassword() && computer.initializePassword(player, payload.value())) {
            send(player, payload, ComputerAccessResultPayload.SUCCESS);
        } else {
            send(player, payload, computer.hasActiveUser() ? ComputerAccessResultPayload.BUSY : ComputerAccessResultPayload.INVALID_PASSWORD);
        }
    }

    private static void login(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (computer.hasActiveUser() && !computer.isAuthenticatedBy(player)) {
            send(player, payload, ComputerAccessResultPayload.BUSY);
        } else if (computer.authenticate(player, payload.value())) {
            com.craisinlord.antarchy.content.antmail.AntmailServerData data = com.craisinlord.antarchy.content.antmail.AntmailServerData.access(player.server);
            net.minecraft.resources.ResourceLocation dimension = player.serverLevel().dimension().location();
            com.craisinlord.antarchy.content.antmail.AntmailAddress address = data.addressAt(dimension, computer.getBlockPos());
            if (address != null) data.setLastUsedAddress(player, address);
            send(player, payload, ComputerAccessResultPayload.SUCCESS);
        } else {
            send(player, payload, ComputerAccessResultPayload.INVALID_PASSWORD);
        }
    }

    private static void eject(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        try {
            ResourceLocation diskId = ResourceLocation.parse(payload.value());
            boolean ejected = computer.ejectOne(player, diskId);
            send(player, payload, ejected ? ComputerAccessResultPayload.SUCCESS : ComputerAccessResultPayload.INVALID);
        } catch (Exception ignored) {
            send(player, payload, ComputerAccessResultPayload.INVALID);
        }
    }

    private static void fileList(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        StringBuilder data = new StringBuilder();
        for (ComputerFileSystem.ComputerFile file : computer.fileSystem().list()) {
            if (!data.isEmpty()) data.append('\n');
            data.append(file.type().name()).append('\t').append(file.path()).append('\t').append(file.contents().length());
        }
        sendFile(player, payload, true, data.toString(), "");
    }

    private static void fileOpen(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        ComputerFileSystem.ComputerFile file = computer.fileSystem().get(payload.value());
        if (file == null) {
            sendFile(player, payload, false, "", "not_found");
        } else if (file.type() != ComputerFileSystem.ComputerFile.Type.TEXT && file.type() != ComputerFileSystem.ComputerFile.Type.IMAGE) {
            sendFile(player, payload, false, "", "not_file");
        } else {
            sendFile(player, payload, true, file.path() + "\0" + file.contents(), "");
        }
    }

    private static void fileCreate(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        String[] request = splitFileRequest(payload.value());
        if (request == null) {
            sendFile(player, payload, false, "", "invalid_request");
            return;
        }
        ComputerFileSystem.Result result = computer.fileSystem().createTextFile(request[0], request[1]);
        if (result.successful()) computer.setChanged();
        sendFile(player, payload, result.successful(), request[0], result.error());
    }

    private static void fileSave(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        String[] request = splitFileRequest(payload.value());
        if (request == null) {
            sendFile(player, payload, false, "", "invalid_request");
            return;
        }
        ComputerFileSystem.Result result = computer.fileSystem().writeTextFile(request[0], request[1]);
        if (result.successful()) computer.setChanged();
        sendFile(player, payload, result.successful(), request[0], result.error());
    }

    private static void fileDelete(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        ComputerFileSystem.Result result = computer.fileSystem().delete(payload.value());
        if (result.successful()) computer.setChanged();
        sendFile(player, payload, result.successful(), payload.value(), result.error());
    }

    private static void fileMove(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        String[] request = splitFileRequest(payload.value());
        if (request == null) {
            sendFile(player, payload, false, "", "invalid_request");
            return;
        }
        ComputerFileSystem.Result result = computer.fileSystem().move(request[0], request[1]);
        if (result.successful()) computer.setChanged();
        sendFile(player, payload, result.successful(), request[1], result.error());
    }

    private static void terminalCommand(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            send(player, payload, ComputerAccessResultPayload.INVALID);
            return;
        }
        String[] request = splitFileRequest(payload.value());
        if (request == null) {
            sendTerminal(player, payload, TerminalResult.error("/", "ERROR: INVALID REQUEST"));
            return;
        }
        TerminalResult result = new TerminalCommandService().execute(new ComputerTerminalFileSystem(computer.fileSystem()), request[0], request[1]);
        if (result.status() != TerminalResult.Status.SUCCESS || !result.lines().isEmpty() || result.clearOutput() || result.openPath() != null) computer.setChanged();
        sendTerminal(player, payload, result);
    }

    private static void sendTerminal(ServerPlayer player, ComputerAccessPayload payload, TerminalResult result) {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("Directory", result.workingDirectory());
        tag.putBoolean("Clear", result.clearOutput());
        tag.putString("Open", result.openPath() == null ? "" : result.openPath());
        net.minecraft.nbt.ListTag lines = new net.minecraft.nbt.ListTag();
        for (String line : result.lines()) lines.add(net.minecraft.nbt.StringTag.valueOf(line));
        tag.put("Lines", lines);
        resultSender.accept(player, new ComputerAccessResultPayload(payload.pos(), result.status() == TerminalResult.Status.ERROR ? ComputerAccessResultPayload.INVALID : ComputerAccessResultPayload.SUCCESS,
                true, true, payload.action() + "\0" + AntmailWire.encodeTag(tag)));
    }

    private static final class ComputerTerminalFileSystem implements TerminalFileSystem {
        private final ComputerFileSystem fileSystem;

        private ComputerTerminalFileSystem(ComputerFileSystem fileSystem) { this.fileSystem = fileSystem; }

        @Override public java.util.Optional<Entry> find(String path, String workingDirectory) {
            ComputerFileSystem.ComputerFile file = fileSystem.get(resolve(path, workingDirectory));
            return file == null ? java.util.Optional.empty() : java.util.Optional.of(new Entry(file.path(), file.path(), file.type() == ComputerFileSystem.ComputerFile.Type.DIRECTORY ? EntryType.DIRECTORY : EntryType.FILE));
        }
        @Override public java.util.List<Entry> list(String path, String workingDirectory) {
            String directory = resolve(path, workingDirectory);
            return fileSystem.list().stream().filter(file -> {
                String parent = file.path().lastIndexOf('/') <= 0 ? "/" : file.path().substring(0, file.path().lastIndexOf('/'));
                return parent.equals(directory) && !file.path().equals(directory);
            }).map(file -> new Entry(file.path().substring(file.path().lastIndexOf('/') + 1), file.path(), file.type() == ComputerFileSystem.ComputerFile.Type.DIRECTORY ? EntryType.DIRECTORY : EntryType.FILE)).toList();
        }
        @Override public boolean createDirectory(String path, String workingDirectory) { return fileSystem.createDirectory(resolve(path, workingDirectory)).successful(); }
        @Override public boolean createFile(String path, String workingDirectory) { return fileSystem.createTextFile(resolve(path, workingDirectory), "").successful(); }
        @Override public java.util.Optional<String> readText(String path, String workingDirectory) { ComputerFileSystem.ComputerFile file = fileSystem.get(resolve(path, workingDirectory)); return file != null && file.type() == ComputerFileSystem.ComputerFile.Type.TEXT ? java.util.Optional.of(file.contents()) : java.util.Optional.empty(); }
        @Override public boolean writeText(String path, String workingDirectory, String contents) { return fileSystem.createOrWriteTextFile(resolve(path, workingDirectory), contents).successful(); }
        @Override public boolean deleteFile(String path, String workingDirectory) { return fileSystem.delete(resolve(path, workingDirectory)).successful(); }
        @Override public boolean deleteEmptyDirectory(String path, String workingDirectory) { return fileSystem.delete(resolve(path, workingDirectory)).successful(); }
        @Override public boolean move(String source, String destination, String workingDirectory) { return fileSystem.move(resolve(source, workingDirectory), resolve(destination, workingDirectory)).successful(); }
        @Override public String normalize(String path, String workingDirectory) { return resolve(path, workingDirectory); }

        private String resolve(String path, String workingDirectory) {
            if (path == null || path.isBlank()) return ComputerFileSystem.normalize(workingDirectory);
            return ComputerFileSystem.normalize(path.startsWith("/") ? path : ComputerFileSystem.normalize(workingDirectory) + "/" + path);
        }
    }

    private static void desktopState(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendDesktop(player, payload, false, "unauthorized");
            return;
        }
        sendDesktop(player, payload, true, AntmailWire.encodeTag(computer.desktopState().save()));
    }

    private static void desktopWallpaper(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendDesktop(player, payload, false, "unauthorized");
            return;
        }
        try {
            boolean changed = computer.selectWallpaper(ResourceLocation.parse(payload.value()));
            sendDesktop(player, payload, changed, changed ? "" : "wallpaper_locked");
        } catch (RuntimeException exception) {
            sendDesktop(player, payload, false, "invalid_wallpaper");
        }
    }

    private static void basiliskState(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        if (!payload.value().isBlank()) {
            try {
                computer.setBasiliskScore(Integer.parseInt(payload.value()));
            } catch (NumberFormatException ignored) {
                sendFile(player, payload, false, "", "invalid_score");
                return;
            }
        }
        sendFile(player, payload, true, computer.basiliskScore() + "\0" + computer.basiliskHighScore(), "");
    }

    private static void antmanState(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        if (!payload.value().isBlank()) {
            try {
                computer.setAntmanScore(Integer.parseInt(payload.value()));
            } catch (NumberFormatException ignored) {
                sendFile(player, payload, false, "", "invalid_score");
                return;
            }
        }
        sendFile(player, payload, true, computer.antmanScore() + "\0" + computer.antmanHighScore(), "");
    }

    private static void blockleState(ServerPlayer player, ComputerBlockEntity computer, ComputerAccessPayload payload, boolean submit) {
        if (!computer.canUseFileSystem(player)) {
            sendFile(player, payload, false, "", "unauthorized");
            return;
        }
        ResourceLocation disk = ResourceLocation.fromNamespaceAndPath("antarchy", "blockle_game");
        if (!computer.diskIds().contains(disk)) {
            sendFile(player, payload, false, "", "not_installed");
            return;
        }
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            sendFile(player, payload, false, "", "overworld_unavailable");
            return;
        }
        long day = overworld.getDayTime() / 24000L;
        computer.resetBlockle(day);
        BlockleAnswers.Answer answer = BlockleAnswers.answerForDay(day);
        if (submit) {
            String guess = payload.value() == null ? "" : payload.value().toLowerCase(java.util.Locale.ROOT);
            if (guess.length() != 5 || !guess.chars().allMatch(value -> value >= 'a' && value <= 'z')) {
                sendFile(player, payload, false, "", "invalid_word");
                return;
            }
            if (!BlockleDictionary.contains(guess)) {
                sendFile(player, payload, false, "", "invalid_word");
                return;
            }
            if (computer.blockleGuesses().size() >= 6 || computer.blockleGuesses().stream().anyMatch(value -> value.equals(answer.word()))) {
                sendFile(player, payload, false, "", "game_over");
                return;
            }
            computer.addBlockleGuess(guess);
        }
        sendFile(player, payload, true, encodeBlockle(day, answer, computer.blockleGuesses()), "");
    }

    private static String encodeBlockle(long day, BlockleAnswers.Answer answer, java.util.List<String> guesses) {
        StringBuilder result = new StringBuilder(Long.toString(day));
        boolean solved = false;
        for (String guess : guesses) {
            String colors = BlockleGame.evaluate(answer.word(), guess);
            if (guess.equals(answer.word())) solved = true;
            result.append('|').append(guess).append(',').append(colors);
        }
        if (solved || guesses.size() >= 6) {
            result.append('|').append(solved ? "SOLVED" : "FAILED").append('|').append(answer.word()).append('|').append(answer.itemId());
        } else {
            result.append('|').append("PLAYING");
        }
        return result.toString();
    }

    private static void sendDesktop(ServerPlayer player, ComputerAccessPayload payload, boolean success, String data) {
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(payload.pos());
        boolean hasPassword = blockEntity instanceof ComputerBlockEntity computer && computer.hasPassword();
        boolean authenticated = blockEntity instanceof ComputerBlockEntity computer && computer.isAuthenticated();
        resultSender.accept(player, new ComputerAccessResultPayload(payload.pos(), success ? ComputerAccessResultPayload.SUCCESS : ComputerAccessResultPayload.INVALID,
                hasPassword, authenticated, payload.action() + "\0\0" + data));
    }

    private static String[] splitFileRequest(String value) {
        if (value == null) return null;
        int separator = value.indexOf('\0');
        if (separator < 0) return null;
        return new String[]{value.substring(0, separator), value.substring(separator + 1)};
    }

    private static void send(ServerPlayer player, ComputerAccessPayload payload, int result) {
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(payload.pos());
        boolean hasPassword = blockEntity instanceof ComputerBlockEntity computer && computer.hasPassword();
        boolean authenticated = blockEntity instanceof ComputerBlockEntity computer && computer.isAuthenticated();
        resultSender.accept(player, new ComputerAccessResultPayload(payload.pos(), result, hasPassword, authenticated));
    }

    private static void sendFile(ServerPlayer player, ComputerAccessPayload payload, boolean success, String data, String error) {
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(payload.pos());
        boolean hasPassword = blockEntity instanceof ComputerBlockEntity computer && computer.hasPassword();
        boolean authenticated = blockEntity instanceof ComputerBlockEntity computer && computer.isAuthenticated();
        resultSender.accept(player, new ComputerAccessResultPayload(payload.pos(), success ? ComputerAccessResultPayload.SUCCESS : ComputerAccessResultPayload.INVALID,
                hasPassword, authenticated, payload.action() + "\0" + error + "\0" + data));
    }
}
