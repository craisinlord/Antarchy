package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import com.craisinlord.antarchy.content.client.ComputerAccessClientState;
import com.craisinlord.antarchy.content.client.ComputerFileSystemClientState;
import com.craisinlord.antarchy.content.client.AntmailClientState;
import com.craisinlord.antarchy.content.antmail.AntmailAddress;
import com.craisinlord.antarchy.content.antmail.AntmailMailbox;
import com.craisinlord.antarchy.content.antmail.AntmailMessage;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.guide.ComputerGuideData;
import com.craisinlord.antarchy.content.network.ComputerAccessResultPayload;
import com.craisinlord.antarchy.content.network.AntmailResultPayload;
import com.craisinlord.antarchy.content.network.ComputerNetworking;
import com.craisinlord.antarchy.content.network.AntmailNetworking;
import com.craisinlord.antarchy.content.computer.paint.AntPaintCanvas;
import com.craisinlord.antarchy.content.computer.paint.AntPaintHistory;
import com.craisinlord.antarchy.content.computer.paint.AntPaintTool;
import com.craisinlord.antarchy.content.computer.paint.AntPaintFile;
import com.craisinlord.antarchy.content.computer.paint.AntPaintFileCodec;
import com.craisinlord.antarchy.content.computer.ComputerFileSystem;
import com.craisinlord.antarchy.content.computer.ComputerDesktopState;
import com.craisinlord.antarchy.content.client.game.AntFarmProgram;
import com.craisinlord.antarchy.content.computer.terminal.TerminalCommandParser;
import com.craisinlord.antarchy.content.computer.terminal.TerminalResult;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.UUID;

public final class ComputerScreen extends Screen {
    private static final int WIDTH = 440;
    private static final int HEIGHT = 286;
    private static final int GREEN = 0xFF65FF65;
    private static final int PALE_GREEN = 0xFFB8FFB8;
    private static final int DARK_GREEN = 0xFF102010;
    private static final int BLACK = 0xFF030603;
    private static final String[] BOOT_MESSAGES = {"ANTS MARCHING...", "COMPUTER COMPUTING...", "HCFS BREWING...", "WAKING THE QUEEN..."};
    private static final long SESSION_TIMEOUT = 300_000L;
    private static final Map<String, Session> SESSIONS = new LinkedHashMap<>();
    private static final String[] ICONS = {"ARCHIVE", "FILES", "SETTINGS", "TERMINAL", "TEXT", "PAINT", "ANTMAIL", "GAMES", "TRASH"};
    private static final Map<String, String> TITLES = Map.of("ARCHIVE", "ANTARCHIVE", "FILES", "FILE EXPLORER", "SETTINGS", "SYSTEM SETTINGS", "TERMINAL", "ANTINTOSH TERMINAL", "TEXT", "ANTTEXT EDITOR", "PAINT", "ANTPAINT", "ANTMAIL", "ANTMAIL", "GAMES", "INSTALLED GAMES", "TRASH", "RECYCLE BIN");
    private final net.minecraft.core.BlockPos position;
    private final String sessionKey;
    private final Session session;
    private boolean loggedIn;
    private String password = "";
    private String confirmation = "";
    private boolean confirmingPassword;
    private String loginMessage = "";
    private int loginMessageTicks;
    private ComputerAccessResultPayload accessResult;
    private int observedResult = -1;
    private Window dragging;
    private String draggedDisk;
    private ResourceLocation selectedDisk;
    private int dragX;
    private int dragY;
    private Window activeWindow;
    private final AntFarmProgram antFarm = new AntFarmProgram(false);

    public ComputerScreen(net.minecraft.core.BlockPos position) {
        super(Component.translatable("screen.antarchy.computer"));
        this.position = position;
        String dimension = Minecraft.getInstance().level == null ? "" : Minecraft.getInstance().level.dimension().location().toString();
        this.sessionKey = dimension + ":" + position.asLong();
        this.session = SESSIONS.computeIfAbsent(sessionKey, ignored -> new Session());
        if (this.session.bootTicks < 0) this.session.bootTicks = 80;
        ComputerNetworking.open(position);
        AntmailNetworking.requestState(position);
        this.accessResult = ComputerAccessClientState.get(position);
        this.loggedIn = accessResult != null && accessResult.authenticated() && session.lastUse + SESSION_TIMEOUT > System.currentTimeMillis();
        if (!loggedIn) {
            session.authenticated = false;
            session.windows.clear();
        }
    }

    @Override
    public void tick() {
        if (session.bootTicks > 0) session.bootTicks--;
        if (loginMessageTicks > 0) loginMessageTicks--;
        ComputerAccessResultPayload currentResult = ComputerAccessClientState.get(position);
        if (currentResult != null) {
            accessResult = currentResult;
            if (currentResult.result() != observedResult) {
                observedResult = currentResult.result();
                if (currentResult.result() == ComputerAccessResultPayload.SUCCESS) {
                    loginMessage = "ACCESS GRANTED // WELCOME";
                    loginMessageTicks = 40;
                } else if (currentResult.result() == ComputerAccessResultPayload.INVALID_PASSWORD) {
                    loginMessage = "ACCESS DENIED // PASSWORD REJECTED";
                    loginMessageTicks = 100;
                } else if (currentResult.result() == ComputerAccessResultPayload.BUSY) {
                    loginMessage = "TERMINAL BUSY // USER PRESENT";
                    loginMessageTicks = 100;
                }
            }
        }
        if (!loggedIn && accessResult != null && accessResult.authenticated()) {
            loggedIn = true;
            session.authenticated = true;
            session.lastUse = System.currentTimeMillis();
        }
        if (loggedIn && (session.lastUse + SESSION_TIMEOUT <= System.currentTimeMillis() || accessResult == null || !accessResult.authenticated())) {
            loggedIn = false;
            session.authenticated = false;
            session.windows.clear();
        }
        if (loggedIn && !session.desktopRequested) {
            ComputerNetworking.requestDesktopState(position);
            session.desktopRequested = true;
        }
        if (currentResult != null && currentResult.data().startsWith("11\u0000")) {
            String[] desktop = currentResult.data().split("\u0000", 3);
            if (desktop.length == 3 && !desktop[2].isBlank()) {
                try {
                    var state = new ComputerDesktopState();
                    state.load(AntmailWire.decodeTag(desktop[2]));
                    session.wallpaperId = state.selectedWallpaper() == null ? ComputerDesktopState.DEFAULT_WALLPAPER.toString() : state.selectedWallpaper().toString();
                    session.wallpapers = state.unlockedWallpaperIds().stream().map(ResourceLocation::toString).toList();
                } catch (RuntimeException ignored) {
                }
            }
        }
        antFarm.setGameInstalled(physicalDisks().stream().anyMatch(id -> id.getPath().equals("ant_farm")));
        if (activeWindow != null && activeWindow.type.equals("GAMES") && activeWindow.gameOpen) antFarm.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        float scale = uiScale();
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2.0F, height / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        int left = -WIDTH / 2;
        int top = -HEIGHT / 2;
        int localMouseX = (int) ((mouseX - width / 2.0F) / scale);
        int localMouseY = (int) ((mouseY - height / 2.0F) / scale);
        graphics.fill(left - 5, top - 5, left + WIDTH + 5, top + HEIGHT + 5, GREEN);
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, BLACK);
        graphics.fill(left + 5, top + 5, left + WIDTH - 5, top + HEIGHT - 5, DARK_GREEN);
        if (session.bootTicks > 0) renderBoot(graphics, left, top); else if (loggedIn) renderDesktop(graphics, left, top, localMouseX, localMouseY); else renderLogin(graphics, left, top);
        graphics.pose().popPose();
    }

    private float uiScale() {
        return Math.min(1.0F, Math.min((width - 24.0F) / WIDTH, (height - 24.0F) / HEIGHT));
    }

    private void renderBoot(GuiGraphics g, int l, int t) {
        int progress = Math.min(100, (80 - session.bootTicks) * 100 / 80);
        int message = Math.min(BOOT_MESSAGES.length - 1, progress * BOOT_MESSAGES.length / 101);
        g.drawString(font, Component.literal("ANTINTOSH OS"), l + 28, t + 78, GREEN, false);
        g.drawString(font, Component.literal("INITIALIZING SYSTEM..."), l + 28, t + 105, PALE_GREEN, false);
        g.fill(l + 28, t + 132, l + WIDTH - 28, t + 148, BLACK);
        box(g, l + 28, t + 132, l + WIDTH - 28, t + 148, GREEN);
        g.fill(l + 32, t + 136, l + 32 + (WIDTH - 64) * progress / 100, t + 144, GREEN);
        g.drawString(font, Component.literal(progress + "%"), l + 28, t + 164, GREEN, false);
        g.drawString(font, Component.literal(BOOT_MESSAGES[message]), l + 28, t + 190, PALE_GREEN, false);
    }

    private void renderLogin(GuiGraphics g, int l, int t) {
        g.drawString(font, Component.literal("ANTINTOSH OS"), l + 28, t + 25, GREEN, false);
        g.drawString(font, Component.literal("BOOT SEQUENCE COMPLETE"), l + 28, t + 43, PALE_GREEN, false);
        g.fill(l + 28, t + 59, l + WIDTH - 28, t + 61, GREEN);
        ComputerBlockEntity computer = computer();
        boolean setup = (accessResult != null && !accessResult.hasPassword()) || (computer != null && !computer.hasPassword());
        int formX = setup ? l + 110 : l + 242;
        int formRight = setup ? l + 330 : l + WIDTH - 28;
        g.drawString(font, Component.literal(setup ? "INITIALIZE COMPUTER" : "LOGIN REQUIRED"), formX, t + 105, GREEN, false);
        g.drawString(font, Component.literal(setup ? (confirmingPassword ? "CONFIRM PASSWORD" : "CREATE PASSWORD") : "PASSWORD"), formX, t + 132, PALE_GREEN, false);
        g.fill(formX, t + 150, formRight, t + 173, BLACK);
        box(g, formX, t + 150, formRight, t + 173, GREEN);
        g.drawString(font, Component.literal("*".repeat((confirmingPassword ? confirmation : password).length())), formX + 6, t + 157, GREEN, false);
        g.drawString(font, Component.literal(setup ? (confirmingPassword ? "[ ENTER ] CONFIRM" : "[ ENTER ] CONTINUE") : "[ ENTER ] LOGIN"), formX, t + 187, GREEN, false);
        g.drawString(font, Component.literal("[ ESC ] POWER DOWN"), formX, t + 205, PALE_GREEN, false);
        if (loginMessageTicks > 0) wrap(g, loginMessage, formX, t + 232, setup ? 220 : 165, PALE_GREEN);
    }

    private void renderDesktop(GuiGraphics g, int l, int t, int mouseX, int mouseY) {
        session.lastUse = System.currentTimeMillis();
        if (session.wallpaperId.endsWith("grid_ant")) {
            for (int py = t + 36; py < t + HEIGHT - 8; py += 18) for (int px = l + 8; px < l + WIDTH - 8; px += 36) g.fill(px, py, px + 2, py + 2, 0xFF173817);
        }
        g.fill(l + 9, t + 9, l + WIDTH - 9, t + 30, BLACK);
        g.drawString(font, Component.literal("ANTINTOSH OS // v" + Antarchy.MOD_VERSION), l + 18, t + 15, GREEN, false);
        long dayTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getDayTime();
        long dayTicks = Math.floorMod(dayTime, 24000L);
        int hours = (int) ((dayTicks / 1000L + 6L) % 24L);
        int minutes = (int) (dayTicks % 1000L * 60L / 1000L);
        g.drawString(font, Component.literal(String.format("%02d:%02d", hours, minutes)), l + WIDTH - 48, t + 15, PALE_GREEN, false);
        g.fill(l + 9, t + 31, l + WIDTH - 9, t + 33, GREEN);
        for (int i = 0; i < ICONS.length; i++) {
            int x = l + 24 + i % 4 * 94;
            int y = t + 48 + i / 4 * 76;
            boolean hover = inside(x - 6, y - 6, 76, 57, mouseX, mouseY);
            if (hover) g.fill(x - 7, y - 7, x + 69, y + 51, 0xFF173817);
            icon(g, ICONS[i], x + 25, y + 2);
            g.drawString(font, Component.literal(ICONS[i]), x, y + 35, GREEN, false);
        }
        List<ResourceLocation> disks = physicalDisks();
        for (int i = 0; i < disks.size() && i < 3; i++) {
            int x = l + 24 + i % 4 * 94;
            int y = t + 200 + i / 4 * 54;
            icon(g, "DISK", x + 25, y + 2);
            String name = disks.get(i).getPath();
            g.drawString(font, Component.literal(name.length() > 12 ? name.substring(0, 12) : name), x, y + 35, GREEN, false);
        }
        for (Window window : session.windows) if (!window.minimized) renderWindow(g, window, l, t, mouseX, mouseY);
    }

    private void icon(GuiGraphics g, String type, int x, int y) {
        g.fill(x - 10, y, x + 10, y + 18, BLACK);
        g.fill(x - 8, y + 2, x + 8, y + 16, GREEN);
        if (type.equals("SETTINGS")) {
            g.fill(x - 12, y + 7, x + 12, y + 11, BLACK);
            g.fill(x - 2, y - 2, x + 2, y + 20, BLACK);
        } else if (type.equals("TRASH")) {
            g.fill(x - 12, y - 3, x + 12, y, GREEN);
            g.fill(x - 7, y + 4, x + 7, y + 18, BLACK);
        } else if (type.equals("TERMINAL")) {
            g.fill(x - 5, y + 7, x - 1, y + 11, BLACK);
            g.fill(x - 1, y + 11, x + 6, y + 14, BLACK);
        }
    }

    private void renderWindow(GuiGraphics g, Window window, int l, int t, int mouseX, int mouseY) {
        int x = l + window.x;
        int y = t + window.y;
        int w = window.maximized ? WIDTH - 28 : window.width;
        int h = window.maximized ? HEIGHT - 48 : window.height;
        window.renderWidth = w;
        window.renderHeight = h;
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, GREEN);
        g.fill(x, y, x + w, y + h, BLACK);
        g.fill(x, y, x + w, y + 19, 0xFF173817);
        g.drawString(font, Component.literal(window.title), x + 7, y + 6, GREEN, false);
        g.drawString(font, Component.literal("_"), x + w - 39, y + 5, PALE_GREEN, false);
        g.drawString(font, Component.literal(window.maximized ? "❐" : "□"), x + w - 27, y + 5, PALE_GREEN, false);
        g.drawString(font, Component.literal("X"), x + w - 13, y + 6, GREEN, false);
        int cx = x + 8;
        int cy = y + 28;
        if (window.type.equals("ARCHIVE")) renderArchive(g, cx, cy, w - 16, h - 34);
        else if (window.type.equals("FILES")) renderFileExplorer(g, cx, cy, w, h);
        else if (window.type.equals("SETTINGS")) renderSettings(g, cx, cy, h - 34);
        else if (window.type.equals("TERMINAL")) renderTerminal(g, cx, cy, h);
        else if (window.type.equals("TEXT")) renderTextEditor(g, cx, cy, w, h);
        else if (window.type.equals("PAINT")) renderPaint(g, cx, cy, w, h);
        else if (window.type.equals("ANTMAIL")) renderAntmail(g, cx, cy, h);
        else if (window.type.equals("GAMES")) renderGames(g, cx, cy);
        else renderTrash(g, cx, cy);
    }

    private void renderArchive(GuiGraphics g, int x, int y, int w, int h) {
        List<ComputerGuideData.Entry> entries = List.of();
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(position) instanceof ComputerBlockEntity computer) entries = ComputerGuideData.entriesFor(computer.diskIds());

        ComputerGuideData.Entry selected = null;
        if (session.archiveEntryId != null) {
            selected = entries.stream().filter(entry -> entry.id().equals(session.archiveEntryId)).findFirst().orElse(null);
            if (selected == null) session.archiveEntryId = null;
        }

        if (selected != null) {
            g.drawString(font, Component.literal("< BACK // RECOVERED FILES"), x, y, GREEN, false);
            g.fill(x, y + 16, x + w, y + 18, GREEN);
            g.drawString(font, Component.translatable(selected.titleKey()), x, y + 27, GREEN, false);
            if (!selected.subtitleKey().isBlank()) wrap(g, Component.translatable(selected.subtitleKey()), x, y + 43, w, PALE_GREEN);
            int line = y + (selected.subtitleKey().isBlank() ? 43 : 59);
            g.drawString(font, Component.literal(selected.type().toUpperCase() + " // " + selected.category().toUpperCase()), x, line, PALE_GREEN, false);
            line += 16;
            for (String descriptionKey : selected.descriptionKeys()) {
                line = wrap(g, Component.translatable(descriptionKey), x, line, w, GREEN) + 3;
                if (line > y + h - 36) break;
            }
            if (!selected.itemId().isBlank() && line < y + h - 24) {
                line = wrap(g, "ITEM // " + selected.itemId(), x, line, w, PALE_GREEN) + 2;
            }
            if (!selected.structureId().isBlank() && line < y + h - 24) {
                line = wrap(g, "STRUCTURE // " + selected.structureId(), x, line, w, PALE_GREEN) + 2;
            }
            if (!selected.dimensionId().isBlank() && line < y + h - 24) {
                wrap(g, "DIMENSION // " + selected.dimensionId(), x, line, w, PALE_GREEN);
            }
            g.drawString(font, Component.literal("[ ESC ] BACK"), x, y + h - 12, PALE_GREEN, false);
            return;
        }

        g.drawString(font, Component.literal("RECOVERED FILES // " + entries.size()), x, y, GREEN, false);
        if (entries.isEmpty()) {
            g.drawString(font, Component.literal("NO ARCHIVE DATA"), x, y + 28, PALE_GREEN, false);
            return;
        }
        int row = y + 18;
        int visible = Math.max(1, (h - 34) / 25);
        for (int i = 0; i < entries.size() && i < visible; i++) {
            ComputerGuideData.Entry entry = entries.get(i);
            int rowTop = row + i * 25;
            g.drawString(font, Component.literal(String.format("FILE_%02d", i + 1)), x + 5, rowTop, PALE_GREEN, false);
            String title = trimToWidth(Component.translatable(entry.titleKey()).getString(), w - 64);
            g.drawString(font, Component.literal(title), x + 57, rowTop, GREEN, false);
            g.drawString(font, Component.literal(entry.type().toUpperCase() + " // " + entry.category().toUpperCase()), x + 57, rowTop + 11, PALE_GREEN, false);
        }
        if (entries.size() > visible) g.drawString(font, Component.literal("SCROLLING NOT AVAILABLE // FIRST FILES SHOWN"), x, y + h - 12, PALE_GREEN, false);
        else g.drawString(font, Component.literal("[ CLICK FILE TO OPEN ]"), x, y + h - 12, PALE_GREEN, false);
    }

    private void renderFileExplorer(GuiGraphics g, int x, int y, int w, int h) {
        var files = ComputerFileSystemClientState.get(position).files();
        g.drawString(font, Component.literal("FILE EXPLORER // " + session.fileExplorerDirectory), x, y, GREEN, false);
        if (!session.fileExplorerDirectory.equals("/")) g.drawString(font, Component.literal("[ UP ]"), x + w - 40, y, GREEN, false);
        g.drawString(font, Component.literal("TYPE                 PATH             SIZE"), x, y + 18, PALE_GREEN, false);
        int line = y + 34;
        for (String file : files) {
            String[] fields = file.split("\\t", 3);
            if (fields.length < 3 || !isDirectChild(fields[1], session.fileExplorerDirectory) || line > y + h - 20) continue;
            g.drawString(font, Component.literal(trimToWidth(fields[0], 36)), x, line, PALE_GREEN, false);
            g.drawString(font, Component.literal(trimToWidth(fields[1], 125)), x + 42, line, GREEN, false);
            g.drawString(font, Component.literal(fields[2]), x + w - 28, line, PALE_GREEN, false);
            line += 14;
        }
        if (line == y + 34) g.drawString(font, Component.literal("NO FILES // OPEN TERMINAL TO CREATE ONE"), x, y + 38, PALE_GREEN, false);
    }

    private boolean isDirectChild(String path, String directory) {
        if (path.equals(directory)) return false;
        String prefix = directory.equals("/") ? "/" : directory + "/";
        if (!path.startsWith(prefix)) return false;
        return path.indexOf('/', prefix.length()) < 0;
    }

    private String parentDirectory(String path) {
        if (path.equals("/")) return "/";
        int slash = path.lastIndexOf('/');
        return slash <= 0 ? "/" : path.substring(0, slash);
    }

    private String trimToWidth(String value, int width) {
        if (font.width(value) <= width) return value;
        String ellipsis = "...";
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end) + ellipsis) > width) end--;
        return value.substring(0, end) + ellipsis;
    }

    private void renderSettings(GuiGraphics g, int x, int y, int h) {
        g.drawString(font, Component.literal("SYSTEM STATUS"), x, y, GREEN, false);
        g.drawString(font, Component.literal("CPU  " + (System.currentTimeMillis() / 100 % 87 + 12) + "%"), x, y + 18, PALE_GREEN, false);
        g.drawString(font, Component.literal("MEM  " + (System.currentTimeMillis() / 250 % 42 + 31) + "%"), x, y + 32, PALE_GREEN, false);
        g.drawString(font, Component.literal("WALLPAPER  " + session.wallpaperId.substring(session.wallpaperId.indexOf(':') + 1).toUpperCase()), x, y + 46, PALE_GREEN, false);
        g.drawString(font, Component.literal("[ CLICK TO CYCLE ]"), x, y + 60, GREEN, false);
        g.drawString(font, Component.literal("PASSWORD  CHANGE IN FULL BUILD"), x, y + 70, GREEN, false);
        g.drawString(font, Component.literal("PHYSICAL DISKS"), x, y + 91, GREEN, false);
        List<ResourceLocation> disks = physicalDisks();
        if (disks.isEmpty()) {
            g.drawString(font, Component.literal("NONE INSTALLED"), x, y + 108, PALE_GREEN, false);
        } else {
            for (int i = 0; i < disks.size() && i < 3; i++) {
                ResourceLocation disk = disks.get(i);
                boolean active = disk.equals(selectedDisk);
                if (active) g.fill(x - 3, y + 105 + i * 17, x + 205, y + 120 + i * 17, 0xFF173817);
                g.drawString(font, Component.literal(disk.toString()), x, y + 108 + i * 17, active ? GREEN : PALE_GREEN, false);
            }
            g.drawString(font, Component.literal("[ EJECT SELECTED ]"), x, y + 162, selectedDisk == null ? 0xFF4A754A : GREEN, false);
        }
        g.drawString(font, Component.literal("LOG OUT"), x, y + h - 17, GREEN, false);
    }

    private void renderTerminal(GuiGraphics g, int x, int y, int h) {
        var fileState = ComputerFileSystemClientState.get(position);
        g.drawString(font, Component.literal("ANTINTOSH TERMINAL [READY]"), x, y, GREEN, false);
        int line = y + 17;
        for (String output : session.terminalOutput) {
            line = wrap(g, output, x, line, 208, PALE_GREEN) + 1;
            if (line > y + h - 32) break;
        }
        if (!fileState.error().isEmpty()) g.drawString(font, Component.literal("RESPONSE: " + fileState.error()), x, y + 16, PALE_GREEN, false);
        g.drawString(font, Component.literal(session.terminalDirectory + "> " + session.terminalInput + "_"), x, y + h - 48, GREEN, false);
    }

    private void renderPaint(GuiGraphics g, int x, int y, int w, int h) {
        var fileState = ComputerFileSystemClientState.get(position);
        if (!session.paintDirty && fileState.openedPath().endsWith(".antpaint") && !fileState.openedContents().isEmpty()) {
            try {
                AntPaintFile file = AntPaintFileCodec.decode(Base64.getDecoder().decode(fileState.openedContents()));
                session.paintName = file.filename();
                session.paintCanvas = file.canvas().copy();
            } catch (IllegalArgumentException ignored) {
            }
        }
        g.drawString(font, Component.literal("ANTPAINT // " + session.paintName + (session.paintDirty ? " *" : "")), x, y, GREEN, false);
        g.drawString(font, Component.literal("NEW PENCIL ERASER FILL CLEAR UNDO REDO SAVE"), x, y + 14, PALE_GREEN, false);
        int size = Math.min((w - 16) / AntPaintCanvas.WIDTH, (h - 54) / AntPaintCanvas.HEIGHT);
        int cx = x + (w - size * AntPaintCanvas.WIDTH) / 2;
        int cy = y + 30;
        g.fill(cx - 1, cy - 1, cx + size * AntPaintCanvas.WIDTH + 1, cy + size * AntPaintCanvas.HEIGHT + 1, BLACK);
        g.fill(cx, cy, cx + size * AntPaintCanvas.WIDTH, cy + size * AntPaintCanvas.HEIGHT, GREEN);
        for (int py = 0; py < AntPaintCanvas.HEIGHT; py++) for (int px = 0; px < AntPaintCanvas.WIDTH; px++) if (!session.paintCanvas.get(px, py)) g.fill(cx + px * size, cy + py * size, cx + (px + 1) * size, cy + (py + 1) * size, BLACK);
        g.drawString(font, Component.literal("TOOL " + session.paintTool.name() + "  " + (session.paintDirty ? "MODIFIED" : "SAVED")), x, y + h - 16, PALE_GREEN, false);
    }

    private void renderTextEditor(GuiGraphics g, int x, int y, int w, int h) {
        var fileState = ComputerFileSystemClientState.get(position);
        if (!fileState.openedPath().isEmpty() && !session.textDirty) {
            session.textPath = fileState.openedPath();
            session.textContent = fileState.openedContents();
        }
        g.drawString(font, Component.literal("ANTTEXT // " + session.textPath + (session.textDirty ? " *" : "")), x, y, GREEN, false);
        g.drawString(font, Component.literal("NEW  OPEN  WRITE  SAVE"), x, y + 18, PALE_GREEN, false);
        g.fill(x + 4, y + 36, x + w - 4, y + h - 4, BLACK);
        if (session.textListing) {
            g.drawString(font, Component.literal("SELECT FILE"), x + 10, y + 46, GREEN, false);
            int line = y + 62;
            for (String file : fileState.files()) {
                String[] fields = file.split("\\t", 3);
                if (fields.length < 2 || line > y + h - 28) continue;
                g.drawString(font, Component.literal(fields[1]), x + 10, line, PALE_GREEN, false);
                line += 14;
            }
            if (fileState.files().isEmpty()) g.drawString(font, Component.literal("NO TEXT FILES"), x + 10, y + 62, PALE_GREEN, false);
        } else {
            wrap(g, session.textContent.isEmpty() ? "TYPE HERE..." : session.textContent, x + 10, y + 46, w - 28, GREEN);
        }
        g.drawString(font, Component.literal("[ CTRL+S ] SAVE   [ ESC ] CLOSE"), x, y + h - 50, PALE_GREEN, false);
    }

    private void renderAntmail(GuiGraphics g, int x, int y, int h) {
        var result = AntmailClientState.get(position);
        g.drawString(font, Component.literal("ANTMAIL // COMPUTER ADDRESS"), x, y, GREEN, false);
        boolean unconfigured = result == null || result.address().isBlank();
        if (unconfigured) {
            g.drawString(font, Component.literal("NO ADDRESS CONFIGURED"), x, y + 20, PALE_GREEN, false);
            g.drawString(font, Component.literal("USERNAME // 3-16 CHARACTERS"), x, y + 40, GREEN, false);
            g.fill(x, y + 56, x + 206, y + 79, BLACK);
            box(g, x, y + 56, x + 206, y + 79, GREEN);
            g.drawString(font, Component.literal(session.antmailUsername + "@ANTMAIL.COM"), x + 6, y + 63, GREEN, false);
            g.drawString(font, Component.literal("[ ENTER ] REGISTER ADDRESS"), x, y + 88, GREEN, false);
        } else {
            g.drawString(font, Component.literal(result.address()), x, y + 20, PALE_GREEN, false);
            g.drawString(font, Component.literal("[ INBOX ] [ SENT ] [ COMPOSE ]"), x, y + 40, GREEN, false);
            if (session.antmailMode.equals("compose")) {
                g.drawString(font, Component.literal("TO"), x, y + 62, GREEN, false);
                g.drawString(font, Component.literal(session.antmailRecipient), x + 28, y + 62, PALE_GREEN, false);
                g.drawString(font, Component.literal("SUBJECT"), x, y + 78, GREEN, false);
                g.drawString(font, Component.literal(session.antmailSubject), x + 52, y + 78, PALE_GREEN, false);
                g.drawString(font, Component.literal("BODY"), x, y + 94, GREEN, false);
                wrap(g, session.antmailBody.isEmpty() ? "TYPE MESSAGE..." : session.antmailBody, x, y + 110, 208, PALE_GREEN);
                g.drawString(font, Component.literal("[ " + (session.antmailAttachText ? "X" : " ") + " ] TEXT FILE   [ " + (session.antmailAttachPaint ? "X" : " ") + " ] PAINTING"), x, y + 142, PALE_GREEN, false);
                g.drawString(font, Component.literal("[ ENTER ] SEND MESSAGE"), x, y + h - 30, GREEN, false);
            } else if (session.antmailMode.equals("message")) {
                AntmailMessage message = selectedAntmailMessage(result);
                if (message != null) {
                    g.drawString(font, Component.literal("< BACK // INBOX"), x, y + 40, GREEN, false);
                    g.drawString(font, Component.literal(message.sender().fullAddress()), x, y + 62, PALE_GREEN, false);
                    g.drawString(font, Component.literal(message.subject()), x, y + 78, GREEN, false);
                    wrap(g, message.body(), x, y + 98, 208, PALE_GREEN);
                }
            } else {
                g.drawString(font, Component.literal("INBOX // " + mailboxMessages(result, false).size()), x, y + 62, GREEN, false);
                int line = y + 80;
                for (AntmailMessage message : mailboxMessages(result, false)) {
                    if (line > y + h - 38) break;
                    g.drawString(font, Component.literal((message.read() ? "  " : "* ") + trimToWidth(message.subject(), 28)), x, line, message.read() ? PALE_GREEN : GREEN, false);
                    line += 14;
                }
            }
        }
        if (!session.antmailStatus.isEmpty()) g.drawString(font, Component.literal(session.antmailStatus), x, y + h - 30, PALE_GREEN, false);
        g.drawString(font, Component.literal(result == null ? "WAITING FOR SERVER" : result.status() == 0 ? "DELIVERY READY" : "SERVER RESPONSE: " + result.detail()), x, y + h - 16, GREEN, false);
    }

    private AntmailMailbox mailbox(AntmailResultPayload result) {
        if (result == null || result.data().isBlank()) return null;
        try {
            return AntmailMailbox.fromTag(AntmailWire.decodeTag(result.data()));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private List<AntmailMessage> mailboxMessages(AntmailResultPayload result, boolean sent) {
        AntmailMailbox mailbox = mailbox(result);
        if (mailbox == null) return List.of();
        return sent ? mailbox.sent() : mailbox.inbox();
    }

    private AntmailMessage selectedAntmailMessage(AntmailResultPayload result) {
        List<AntmailMessage> messages = mailboxMessages(result, session.antmailSent);
        return session.antmailMessageIndex >= 0 && session.antmailMessageIndex < messages.size() ? messages.get(session.antmailMessageIndex) : null;
    }

    private void renderGames(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.literal("INSTALLED GAMES"), x, y, GREEN, false);
        if (activeWindow != null && activeWindow.gameOpen) { antFarm.render(g, font, x, y + 16, 214, 155); return; }
        g.drawString(font, Component.literal(antFarm.isGameInstalled() ? "ANT FARM  [ ENTER ]" : "ANT FARM [NOT INSTALLED]"), x, y + 22, PALE_GREEN, false);
        g.drawString(font, Component.literal("GLIMMER MATCH"), x, y + 38, PALE_GREEN, false);
        g.drawString(font, Component.literal("HFCS FACTORY"), x, y + 54, PALE_GREEN, false);
    }

    private void renderTrash(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.literal("DROP A DISK HERE TO EJECT"), x, y, GREEN, false);
        g.drawString(font, Component.literal("EJECTED FILES RETURN TO PLAYER"), x, y + 22, PALE_GREEN, false);
    }

    private int wrap(GuiGraphics g, String text, int x, int y, int width, int color) { return wrap(g, Component.literal(text), x, y, width, color); }

    private int wrap(GuiGraphics g, Component text, int x, int y, int width, int color) {
        for (var line : font.split(text, width)) { g.drawString(font, line, x, y, color, false); y += 11; }
        return y;
    }

    private void box(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 2, color);
        g.fill(x1, y2 - 2, x2, y2, color);
        g.fill(x1, y1, x1 + 2, y2, color);
        g.fill(x2 - 2, y1, x2, y2, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = uiScale();
        mouseX = (mouseX - width / 2.0F) / scale;
        mouseY = (mouseY - height / 2.0F) / scale;
        int l = -WIDTH / 2;
        int t = -HEIGHT / 2;
        if (!loggedIn) {
            ComputerBlockEntity computer = computer();
            boolean setup = (accessResult != null && !accessResult.hasPassword()) || (computer != null && !computer.hasPassword());
            int formX = setup ? l + 110 : l + 242;
            if (inside(formX, t + 181, setup ? 150 : 100, 24, mouseX, mouseY)) tryLogin();
            return true;
        }
        session.lastUse = System.currentTimeMillis();
        for (int i = session.windows.size() - 1; i >= 0; i--) {
            Window window = session.windows.get(i);
            if (window.minimized) continue;
            int x = l + window.x;
            int y = t + window.y;
            int w = window.maximized ? WIDTH - 28 : window.width;
            int h = window.maximized ? HEIGHT - 48 : window.height;
            if (inside(x, y + 20, w, h - 20, mouseX, mouseY)) {
                activeWindow = window;
                if (window.type.equals("ARCHIVE")) {
                    List<ComputerGuideData.Entry> entries = ComputerGuideData.entriesFor(computer() == null ? List.of() : computer().diskIds());
                    int archiveX = x + 8;
                    int archiveY = y + 28;
                    if (session.archiveEntryId != null) {
                        if (inside(archiveX, archiveY, w - 16, 20, mouseX, mouseY)) session.archiveEntryId = null;
                    } else {
                        int visible = Math.max(1, (h - 34) / 25);
                        int index = ((int) mouseY - (archiveY + 16)) / 25;
                        if (index >= 0 && index < entries.size() && index < visible && inside(archiveX, archiveY + 16 + index * 25, w - 16, 23, mouseX, mouseY)) session.archiveEntryId = entries.get(index).id();
                    }
                    return true;
                }
                if (window.type.equals("FILES")) {
                    var files = ComputerFileSystemClientState.get(position).files();
                    if (!session.fileExplorerDirectory.equals("/") && mouseY < y + 36 && mouseX > x + w - 48) {
                        session.fileExplorerDirectory = parentDirectory(session.fileExplorerDirectory);
                        return true;
                    }
                    int row = 0;
                    for (String file : files) {
                        String[] fields = file.split("\\t", 3);
                        if (fields.length < 3 || !isDirectChild(fields[1], session.fileExplorerDirectory)) continue;
                        if (row++ != ((int) mouseY - (y + 62)) / 14) continue;
                        if (fields[0].equals("DIRECTORY")) session.fileExplorerDirectory = fields[1];
                        else if (fields[0].equals("TEXT")) {
                            ComputerNetworking.openFile(position, fields[1]);
                            open(fields[1].endsWith(".antpaint") ? "PAINT" : "TEXT");
                        }
                        break;
                    }
                    return true;
                }
                if (window.type.equals("PAINT")) {
                    int contentX = x + 8;
                    int contentY = y + 48;
                    if (inside(contentX, contentY, w - 16, h - 54, mouseX, mouseY)) paintAt(mouseX, mouseY, contentX, contentY - 18, w - 16, h - 54, button == 1);
                    else if (mouseY < y + 66) {
                        if (mouseX > contentX + 190) savePaint();
                        else paintTool((int) mouseX - contentX);
                    }
                    return true;
                }
                if (window.type.equals("TEXT")) {
                    if (mouseY < y + 66) {
                        int action = ((int) mouseX - (x + 8)) / 42;
                        if (action == 0) newText(); else if (action == 1) { session.textListing = true; ComputerNetworking.listFiles(position); } else if (action == 2) writeText(); else if (action == 3) saveText();
                    } else if (session.textListing) {
                        var files = ComputerFileSystemClientState.get(position).files();
                        int index = ((int) mouseY - (y + 62)) / 14;
                        if (index >= 0 && index < files.size()) {
                            String[] fields = files.get(index).split("\\t", 3);
                            if (fields.length >= 2) {
                                session.textListing = false;
                                ComputerNetworking.openFile(position, fields[1]);
                            }
                        }
                    } else session.textFocused = true;
                    return true;
                }
                if (window.type.equals("ANTMAIL")) {
                    var result = AntmailClientState.get(position);
                    if (result == null || result.address().isBlank()) {
                        if (inside(x + 8, y + 84, 214, 25, mouseX, mouseY)) {
                            session.antmailFocused = true;
                            return true;
                        }
                        if (inside(x + 8, y + 112, 214, 24, mouseX, mouseY)) {
                            setupAntmail();
                            return true;
                        }
                    } else if (mouseY < y + 72) {
                        if (mouseX < x + 70) { session.antmailMode = "inbox"; session.antmailSent = false; session.antmailMessageIndex = -1; }
                        else if (mouseX < x + 125) { session.antmailMode = "sent"; session.antmailSent = true; session.antmailMessageIndex = -1; }
                        else { session.antmailMode = "compose"; session.antmailFocused = true; session.antmailField = 1; }
                    } else if (session.antmailMode.equals("inbox") || session.antmailMode.equals("sent")) {
                        int index = ((int) mouseY - (y + 108)) / 14;
                        List<AntmailMessage> messages = mailboxMessages(result, session.antmailSent);
                        if (index >= 0 && index < messages.size()) {
                            session.antmailMessageIndex = index;
                            session.antmailMode = "message";
                            AntmailNetworking.markRead(position, messages.get(index).id());
                        }
                    } else if (session.antmailMode.equals("compose")) {
                        if (mouseY >= y + 160 && mouseY < y + 184) {
                            session.antmailAttachText = mouseX < x + 112;
                            session.antmailAttachPaint = mouseX >= x + 112;
                            return true;
                        }
                        if (mouseY < y + 94) {
                            session.antmailField = mouseY < y + 78 ? 1 : 2;
                            session.antmailFocused = true;
                        } else if (mouseY < y + 150) {
                            session.antmailField = 3;
                            session.antmailFocused = true;
                        }
                    }
                    return true;
                }
                if (window.type.equals("GAMES") && mouseY > y + 35 && !window.gameOpen && antFarm.isGameInstalled()) { window.gameOpen = true; activeWindow = window; return true; }
                if (window.type.equals("TERMINAL")) { session.terminalFocused = true; return true; }
            }
            if (window.type.equals("SETTINGS") && inside(x + 8, y + 105, 205, 51, mouseX, mouseY)) {
                List<ResourceLocation> disks = physicalDisks();
                int index = ((int) mouseY - (y + 105)) / 17;
                if (index >= 0 && index < disks.size() && index < 3) {
                    selectedDisk = disks.get(index);
                    return true;
                }
            }
            if (window.type.equals("SETTINGS") && inside(x + 8, y + 70, 205, 25, mouseX, mouseY)) {
                if (session.wallpapers.size() > 1) {
                    int current = session.wallpapers.indexOf(session.wallpaperId);
                    String next = session.wallpapers.get((current + 1 + session.wallpapers.size()) % session.wallpapers.size());
                    session.wallpaperId = next;
                    ComputerNetworking.selectWallpaper(position, ResourceLocation.parse(next));
                }
                return true;
            }
            if (window.type.equals("SETTINGS") && inside(x + 8, y + 155, 205, 25, mouseX, mouseY)) {
                ejectSelected();
                return true;
            }
            if (inside(x, y + 20, w, h - 20, mouseX, mouseY)) return true;
            if (inside(x, y, w, 20, mouseX, mouseY)) {
                if (inside(x + w - 18, y, 18, 20, mouseX, mouseY)) session.windows.remove(i);
                else if (inside(x + w - 48, y, 16, 20, mouseX, mouseY)) window.maximized = !window.maximized;
                else if (inside(x + w - 63, y, 16, 20, mouseX, mouseY)) window.minimized = true;
                else { dragging = window; dragX = (int) mouseX - x; dragY = (int) mouseY - y; session.windows.remove(i); session.windows.add(window); }
                return true;
            }
        }
        for (int i = 0; i < ICONS.length; i++) {
            int x = l + 24 + i % 4 * 94;
            int y = t + 48 + i / 4 * 76;
            if (inside(x - 6, y - 6, 76, 57, mouseX, mouseY)) { open(ICONS[i]); return true; }
        }
        List<ResourceLocation> disks = physicalDisks();
        for (int i = 0; i < disks.size() && i < 3; i++) {
            int x = l + 24 + i % 4 * 94;
            int y = t + 200 + i / 4 * 54;
            if (inside(x - 6, y - 6, 76, 48, mouseX, mouseY)) {
                draggedDisk = disks.get(i).toString();
                selectedDisk = disks.get(i);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = uiScale();
        mouseX = (mouseX - width / 2.0F) / scale;
        mouseY = (mouseY - height / 2.0F) / scale;
        if (dragging != null) {
            int l = -WIDTH / 2;
            int t = -HEIGHT / 2;
            dragging.x = Math.max(12, Math.min(WIDTH - dragging.renderWidth - 12, (int) mouseX - l - this.dragX));
            dragging.y = Math.max(34, Math.min(HEIGHT - dragging.renderHeight - 12, (int) mouseY - t - this.dragY));
        }
        if (activeWindow != null && activeWindow.type.equals("PAINT") && button != 0) {
            int l = -WIDTH / 2;
            int t = -HEIGHT / 2;
            int x = l + activeWindow.x + 8;
            int y = t + activeWindow.y + 48;
            paintAt(mouseX, mouseY, x, y - 18, activeWindow.renderWidth - 16, activeWindow.renderHeight - 54, button == 1);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        float scale = uiScale();
        mouseX = (mouseX - width / 2.0F) / scale;
        mouseY = (mouseY - height / 2.0F) / scale;
        if (draggedDisk != null) {
            int l = -WIDTH / 2;
            int t = -HEIGHT / 2;
            int trashX = l + 24 + (6 % 4) * 94;
            int trashY = t + 48 + (6 / 4) * 76;
            if (inside(trashX - 6, trashY - 6, 76, 57, mouseX, mouseY)) ejectSelected();
            draggedDisk = null;
        }
        dragging = null;
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!loggedIn && codePoint >= 32) {
            if (confirmingPassword) {
                if (confirmation.length() < 32) confirmation += Character.toUpperCase(codePoint);
            } else if (password.length() < 32) {
                password += Character.toUpperCase(codePoint);
            }
        }
        if (loggedIn && activeWindow != null) {
            if (activeWindow.type.equals("TEXT") && session.textFocused && codePoint >= 32) { session.textContent += codePoint; session.textDirty = true; return true; }
            if (activeWindow.type.equals("TERMINAL") && session.terminalFocused && codePoint >= 32) { session.terminalInput += codePoint; return true; }
            if (activeWindow.type.equals("ANTMAIL") && session.antmailFocused && codePoint >= 32) {
                if (session.antmailMode.equals("compose")) {
                    if (session.antmailField == 1 && session.antmailRecipient.length() < 64) session.antmailRecipient += codePoint;
                    else if (session.antmailField == 2 && session.antmailSubject.length() < 64) session.antmailSubject += codePoint;
                    else if (session.antmailField == 3 && session.antmailBody.length() < 16384) session.antmailBody += codePoint;
                } else if (Character.isLetterOrDigit(codePoint) || codePoint == '_') session.antmailUsername += Character.toLowerCase(codePoint);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!loggedIn) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
            if (keyCode == 259) {
                if (confirmingPassword && !confirmation.isEmpty()) confirmation = confirmation.substring(0, confirmation.length() - 1);
                else if (!password.isEmpty()) password = password.substring(0, password.length() - 1);
            }
            else if (keyCode == 257 || keyCode == 335) tryLogin();
            return true;
        }
        if (activeWindow != null && activeWindow.type.equals("GAMES") && activeWindow.gameOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { activeWindow.gameOpen = false; return true; }
            if (antFarm.keyPressed(keyCode)) return true;
        }
        if (activeWindow != null && activeWindow.type.equals("TERMINAL") && session.terminalFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (!session.terminalInput.isEmpty()) session.terminalInput = session.terminalInput.substring(0, session.terminalInput.length() - 1); return true; }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { executeTerminal(); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.terminalFocused = false; return true; }
        }

        if (activeWindow != null && activeWindow.type.equals("ANTMAIL") && session.antmailFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (session.antmailMode.equals("compose")) {
                    if (session.antmailField == 1 && !session.antmailRecipient.isEmpty()) session.antmailRecipient = session.antmailRecipient.substring(0, session.antmailRecipient.length() - 1);
                    else if (session.antmailField == 2 && !session.antmailSubject.isEmpty()) session.antmailSubject = session.antmailSubject.substring(0, session.antmailSubject.length() - 1);
                    else if (session.antmailField == 3 && !session.antmailBody.isEmpty()) session.antmailBody = session.antmailBody.substring(0, session.antmailBody.length() - 1);
                } else if (!session.antmailUsername.isEmpty()) session.antmailUsername = session.antmailUsername.substring(0, session.antmailUsername.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (session.antmailMode.equals("compose")) sendAntmail(); else setupAntmail();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.antmailFocused = false; return true; }
        }
        if (activeWindow != null && activeWindow.type.equals("TEXT") && session.textFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (!session.textContent.isEmpty()) { session.textContent = session.textContent.substring(0, session.textContent.length() - 1); session.textDirty = true; } return true; }
            if (keyCode == GLFW.GLFW_KEY_ENTER) { session.textContent += "\n"; session.textDirty = true; return true; }
            if (keyCode == GLFW.GLFW_KEY_S && hasControl(modifiers)) { saveText(); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.textFocused = false; return true; }
        }
        if (activeWindow != null && activeWindow.type.equals("ARCHIVE") && session.archiveEntryId != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            session.archiveEntryId = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void tryLogin() {
        if (password.isBlank()) return;
        if (accessResult != null && !accessResult.hasPassword()) {
            if (!confirmingPassword) {
                confirmingPassword = true;
                confirmation = "";
                loginMessage = "RETYPE PASSWORD // CONFIRMATION REQUIRED";
                loginMessageTicks = 100;
                return;
            }
            if (!password.equals(confirmation)) {
                password = "";
                confirmation = "";
                confirmingPassword = false;
                loginMessage = "SETUP ERROR // PASSWORDS DO NOT MATCH";
                loginMessageTicks = 100;
                return;
            }
            ComputerNetworking.setup(position, password);
            loginMessage = "INITIALIZING ACCESS // WAITING";
        } else {
            ComputerNetworking.login(position, password);
            loginMessage = "AUTHENTICATING // WAITING";
        }
        loginMessageTicks = 40;
        password = "";
        confirmation = "";
        confirmingPassword = false;
    }

    private void executeTerminal() {
        String input = session.terminalInput.trim();
        if (input.isEmpty()) return;
        session.terminalOutput.add(session.terminalDirectory + "> " + input);
        TerminalCommandParser.ParseResult parsed = TerminalCommandParser.parse(input);
        if (!parsed.isSuccess()) session.terminalOutput.add(parsed.error());
        else {
            String command = parsed.command().name();
            List<String> args = parsed.command().arguments();
            if (command.equals("pwd")) session.terminalOutput.add(session.terminalDirectory);
            else if (command.equals("clear")) session.terminalOutput.clear();
            else if (command.equals("ls")) ComputerNetworking.listFiles(position);
            else if (command.equals("open") && args.size() == 1) ComputerNetworking.openFile(position, args.get(0));
            else if (command.equals("touch") && args.size() == 1) ComputerNetworking.createFile(position, args.get(0), "");
            else if (command.equals("write") && args.size() >= 1) ComputerNetworking.saveFile(position, args.get(0), args.size() > 1 ? args.get(1) : "");
            else if (command.equals("cd") && args.size() == 1) {
                String path = args.get(0).startsWith("/") ? args.get(0) : session.terminalDirectory + "/" + args.get(0);
                session.terminalDirectory = ComputerFileSystem.normalize(path);
            }
            else if (command.equals("help")) session.terminalOutput.add("HELP CLEAR PWD LS CD MKDIR TOUCH CAT WRITE RM RMDIR MV OPEN");
            else session.terminalOutput.add("COMMAND NOT RECOGNIZED");
        }
        while (session.terminalOutput.size() > 40) session.terminalOutput.remove(0);
        session.terminalInput = "";
    }

    private void setupAntmail() {
        if (!AntmailAddress.isValidUsername(session.antmailUsername)) {
            session.antmailStatus = "SETUP ERROR // USE 3-16 LOWERCASE LETTERS, NUMBERS, OR _";
            session.antmailFocused = true;
            return;
        }
        AntmailNetworking.setup(position, session.antmailUsername);
        session.antmailStatus = "REGISTERING ADDRESS // WAITING";
        session.antmailFocused = false;
    }

    private void sendAntmail() {
        if (!AntmailAddress.isValidAddress(session.antmailRecipient)) {
            session.antmailStatus = "SEND ERROR // INVALID RECIPIENT";
            return;
        }
        if (session.antmailSubject.isBlank() || session.antmailBody.isBlank()) {
            session.antmailStatus = "SEND ERROR // SUBJECT AND BODY REQUIRED";
            return;
        }
        List<AntmailAttachment> attachments = new ArrayList<>();
        if (session.antmailAttachText && !session.textContent.isEmpty()) attachments.add(new AntmailAttachment.TextFile(session.textPath.substring(session.textPath.lastIndexOf('/') + 1), session.textContent));
        if (session.antmailAttachPaint) {
            byte[] pixels = new byte[AntPaintCanvas.WIDTH * AntPaintCanvas.HEIGHT];
            for (int py = 0; py < AntPaintCanvas.HEIGHT; py++) for (int px = 0; px < AntPaintCanvas.WIDTH; px++) pixels[py * AntPaintCanvas.WIDTH + px] = (byte) (session.paintCanvas.get(px, py) ? 1 : 0);
            attachments.add(new AntmailAttachment.PaintImage(session.paintName, AntPaintCanvas.WIDTH, AntPaintCanvas.HEIGHT, pixels));
        }
        AntmailNetworking.send(position, session.antmailRecipient, session.antmailSubject, session.antmailBody, attachments);
        session.antmailStatus = "TRANSMITTING MESSAGE // WAITING";
        session.antmailMode = "inbox";
        session.antmailFocused = false;
        session.antmailRecipient = "";
        session.antmailSubject = "";
        session.antmailBody = "";
        session.antmailAttachText = false;
        session.antmailAttachPaint = false;
    }

    private void newText() {
        session.textPath = "/documents/untitled.txt";
        session.textContent = "";
        session.textDirty = true;
        session.textListing = false;
        session.textFocused = true;
    }

    private void writeText() {
        ComputerNetworking.createFile(position, session.textPath, session.textContent);
        session.textDirty = false;
    }

    private void saveText() {
        ComputerNetworking.saveFile(position, session.textPath, session.textContent);
        session.textDirty = false;
    }

    private void savePaint() {
        String path = "/pictures/" + session.paintName;
        if (!path.endsWith(".antpaint")) path += ".antpaint";
        session.paintName = path.substring(path.lastIndexOf('/') + 1);
        AntPaintFile file = new AntPaintFile(UUID.randomUUID().toString(), session.paintName, session.paintCanvas.copy(), 0L, 0L);
        String encoded = Base64.getEncoder().encodeToString(AntPaintFileCodec.encode(file));
        String savedPath = path;
        boolean exists = ComputerFileSystemClientState.get(position).files().stream().anyMatch(entry -> entry.contains("\t" + savedPath + "\t"));
        if (exists) ComputerNetworking.saveFile(position, savedPath, encoded); else ComputerNetworking.createFile(position, savedPath, encoded);
        session.paintDirty = false;
    }

    private void paintTool(int offset) {
        session.paintTool = offset < 52 ? AntPaintTool.PENCIL : offset < 108 ? AntPaintTool.ERASER : AntPaintTool.FILL;
    }

    private void paintAt(double mouseX, double mouseY, int x, int y, int w, int h, boolean draw) {
        int size = Math.min(w / AntPaintCanvas.WIDTH, h / AntPaintCanvas.HEIGHT);
        if (size <= 0) return;
        int canvasX = x + (w - size * AntPaintCanvas.WIDTH) / 2;
        int px = (int) ((mouseX - canvasX) / size);
        int py = (int) ((mouseY - y) / size);
        if (px < 0 || py < 0 || px >= AntPaintCanvas.WIDTH || py >= AntPaintCanvas.HEIGHT) return;
        session.paintHistory.record(session.paintCanvas);
        if (session.paintTool == AntPaintTool.PENCIL) session.paintCanvas.erase(px, py);
        else if (session.paintTool == AntPaintTool.ERASER) session.paintCanvas.draw(px, py);
        else session.paintCanvas.floodFill(px, py, session.paintTool == AntPaintTool.ERASER);
        session.paintDirty = true;
    }

    private boolean hasControl(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    private ComputerBlockEntity computer() {
        if (Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.getBlockEntity(position) instanceof ComputerBlockEntity computer ? computer : null;
    }

    private List<ResourceLocation> physicalDisks() {
        ComputerBlockEntity computer = computer();
        if (computer == null) return List.of();
        return computer.diskIds().stream()
                .filter(id -> !id.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "introduction")))
                .toList();
    }

    private void ejectSelected() {
        if (selectedDisk == null) return;
        ComputerNetworking.eject(position, selectedDisk);
        loginMessage = "EJECTING DISK // WAITING";
        loginMessageTicks = 100;
        selectedDisk = null;
    }

    private void open(String type) {
        for (Window window : session.windows) if (window.type.equals(type)) { window.minimized = false; session.windows.remove(window); session.windows.add(window); return; }
        session.windows.add(new Window(type, TITLES.get(type), 112 + session.windows.size() * 12, 52 + session.windows.size() * 10));
        if (type.equals("ANTMAIL")) AntmailNetworking.requestState(position);
    }

    private static boolean inside(int x, int y, int w, int h, double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        ComputerNetworking.close(position);
        super.onClose();
    }

    private static final class Session {
        private boolean authenticated;
        private long lastUse;
        private final List<Window> windows = new ArrayList<>();
        private final List<String> terminalOutput = new ArrayList<>(List.of("ANTINTOSH TERMINAL [READY]", "TYPE HELP FOR COMMANDS"));
        private String terminalInput = "";
        private String terminalDirectory = "/";
        private boolean terminalFocused;
        private String textPath = "/documents/untitled.txt";
        private String textContent = "";
        private boolean textDirty;
        private boolean textFocused;
        private boolean textListing;
        private ResourceLocation archiveEntryId;
        private AntPaintCanvas paintCanvas = new AntPaintCanvas();
        private final AntPaintHistory paintHistory = new AntPaintHistory();
        private AntPaintTool paintTool = AntPaintTool.PENCIL;
        private String paintName = "UNTITLED.ANTPAINT";
        private boolean paintDirty;
        private String antmailUsername = "";
        private String antmailStatus = "";
        private boolean antmailFocused;
        private String antmailMode = "inbox";
        private String antmailRecipient = "";
        private String antmailSubject = "";
        private String antmailBody = "";
        private int antmailField;
        private boolean antmailSent;
        private int antmailMessageIndex = -1;
        private boolean antmailAttachText;
        private boolean antmailAttachPaint;
        private String fileExplorerDirectory = "/";
        private String wallpaperId = ComputerDesktopState.DEFAULT_WALLPAPER.toString();
        private List<String> wallpapers = List.of(ComputerDesktopState.DEFAULT_WALLPAPER.toString());
        private boolean desktopRequested;
        private int bootTicks = -1;
    }

    private static final class Window {
        private final String type;
        private final String title;
        private final int width = 230;
        private final int height = 210;
        private int x;
        private int y;
        private int renderWidth = width;
        private int renderHeight = height;
        private boolean minimized;
        private boolean maximized;
        private boolean gameOpen;

        private Window(String type, String title, int x, int y) { this.type = type; this.title = title; this.x = x; this.y = y; }
    }
}
