package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import com.craisinlord.antarchy.content.computer.ComputerFileSystem;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import net.minecraft.server.level.ServerPlayer;
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
            case ComputerAccessPayload.DESKTOP_STATE -> desktopState(player, computer, payload);
            case ComputerAccessPayload.DESKTOP_WALLPAPER -> desktopWallpaper(player, computer, payload);
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
        } else if (file.type() != ComputerFileSystem.ComputerFile.Type.TEXT) {
            sendFile(player, payload, false, "", "not_text");
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
