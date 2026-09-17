package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import com.craisinlord.antarchy.content.client.ComputerAccessClientState;
import com.craisinlord.antarchy.content.client.BlockleClientState;
import com.craisinlord.antarchy.content.client.ComputerFileSystemClientState;
import com.craisinlord.antarchy.content.client.AntmailClientState;
import com.craisinlord.antarchy.content.antmail.AntmailAddress;
import com.craisinlord.antarchy.content.antmail.AntmailMailbox;
import com.craisinlord.antarchy.content.antmail.AntmailMessage;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.antmail.AntmailAttachmentFiles;
import com.craisinlord.antarchy.content.antmail.AntmailDraft;
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
import com.craisinlord.antarchy.content.client.game.BasiliskProgram;
import com.craisinlord.antarchy.content.client.game.AntmanProgram;
import com.craisinlord.antarchy.content.client.game.BlockleProgram;
import com.craisinlord.antarchy.content.computer.terminal.TerminalCommandParser;
import com.craisinlord.antarchy.content.computer.terminal.TerminalResult;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Base64;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public final class ComputerScreen extends Screen {
    private static final int WIDTH = 440;
    private static final int HEIGHT = 286;
    private static final int GREEN = 0xFF65FF65;
    private static final int PALE_GREEN = 0xFFB8FFB8;
    private static final int DARK_GREEN = 0xFF102010;
    private static final int BLACK = 0xFF030603;
    private static final int TITLE_BAR_HEIGHT = 20;
    private static final int WINDOW_CONTROL_WIDTH = 18;
    private static final int WINDOW_CONTROL_COUNT = 3;
    private static final int TRASH_ICON_INDEX = 8;
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
    private boolean paintStrokeActive;
    private int paintStrokeButton = -1;
    private int lastPaintPixelX = -1;
    private int lastPaintPixelY = -1;
    private String draggedDisk;
    private ResourceLocation selectedDisk;
    private int dragX;
    private int dragY;
    private Window activeWindow;
    private final BasiliskProgram basilisk;
    private final AntmanProgram antman;
    private final BlockleProgram blockle;
    private final Set<String> archiveRenderLog = new HashSet<>();
    private final Map<String, LivingEntity> archiveEntityPreviews = new HashMap<>();
    private final Set<String> unavailableArchiveEntities = new HashSet<>();

    public ComputerScreen(net.minecraft.core.BlockPos position) {
        super(Component.translatable("screen.antarchy.computer"));
        this.position = position;
        this.basilisk = new BasiliskProgram(false, 0, 0, score -> ComputerNetworking.saveBasiliskScore(this.position, score));
        this.antman = new AntmanProgram(false, 0, 0, score -> ComputerNetworking.saveAntmanScore(this.position, score));
        this.blockle = new BlockleProgram(this.position);
        String dimension = Minecraft.getInstance().level == null ? "" : Minecraft.getInstance().level.dimension().location().toString();
        this.sessionKey = dimension + ":" + position.asLong();
        this.session = SESSIONS.computeIfAbsent(sessionKey, ignored -> new Session());
        this.session.windows.removeIf(window -> window.type.equals("GAMES") && window.gameOpen);
        if (this.session.bootTicks < 0) this.session.bootTicks = 80;
        this.session.antmanStateRequested = false;
        this.session.blockleStateRequested = false;
        this.session.antmanResponse = "";
        ComputerAccessClientState.clear(position);
        BlockleClientState.clear(position);
        ComputerNetworking.open(position);
        this.accessResult = ComputerAccessClientState.get(position);
        this.loggedIn = accessResult != null && accessResult.authenticated() && session.lastUse + SESSION_TIMEOUT > System.currentTimeMillis();
        if (loggedIn) {
            requestAntmailState();
            session.antmailRefreshTicks = 40;
        }
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
        AntmailResultPayload mailResult = AntmailClientState.get(position);
        syncAntmailPaintAttachment();
        if (loggedIn && --session.antmailRefreshTicks <= 0) {
            requestAntmailState();
            session.antmailRefreshTicks = 40;
        }
        if (loggedIn && AntmailClientState.getMailbox(position) == null) session.antmailStateWaitTicks++;
        else session.antmailStateWaitTicks = 0;
        if (session.antmailRegistrationPending && mailResult != null) {
            String expectedAddress = session.antmailUsername + "@antmail.com";
            if (mailResult.address().equalsIgnoreCase(expectedAddress)) {
                session.antmailStatus = "ADDRESS REGISTERED";
                session.antmailRegistrationPending = false;
                requestAntmailState();
            } else if (mailResult.detail().startsWith("registration_failed:")) {
                String reason = mailResult.detail().substring("registration_failed:".length()).trim();
                session.antmailStatus = "ADDRESS REGISTRATION FAILED // " + (reason.isBlank() ? "SERVER REJECTED" : reason.toUpperCase());
                session.antmailRegistrationPending = false;
            }
        }
        if (!session.antmailRetryMessageId.isBlank() && mailResult != null
                && mailResult != session.antmailRetryBaseline
                && session.antmailRetryMessageId.equals(mailResult.messageId())
                && !"message_detail".equals(mailResult.detail())) {
            session.antmailStatus = antmailStatus(mailResult);
            session.antmailRetryMessageId = "";
            session.antmailRetryBaseline = null;
            session.antmailRetryTicks = 0;
            requestAntmailState(true);
        } else if (!session.antmailRetryMessageId.isBlank() && --session.antmailRetryTicks <= 0) {
            session.antmailStatus = "RETRY TIMED OUT // TRY AGAIN";
            session.antmailRetryMessageId = "";
            session.antmailRetryBaseline = null;
            requestAntmailState(true);
        }
        if (!loggedIn && accessResult != null && accessResult.authenticated()) {
            loggedIn = true;
            session.authenticated = true;
            session.lastUse = System.currentTimeMillis();
            session.antmailRefreshTicks = 40;
            requestAntmailState();
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
        if (loggedIn && !session.basiliskStateRequested) {
            ComputerNetworking.requestBasiliskState(position);
            session.basiliskStateRequested = true;
        }
        if (loggedIn && !session.antmanStateRequested) {
            ComputerNetworking.requestAntmanState(position);
            session.antmanStateRequested = true;
        }
        if (loggedIn && !session.blockleStateRequested) {
            ComputerNetworking.requestBlockleState(position);
            session.blockleStateRequested = true;
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
        if (currentResult != null && currentResult.data().startsWith("15\u0000") && !currentResult.data().equals(session.terminalResponse)) {
            session.terminalResponse = currentResult.data();
            try {
                var terminal = AntmailWire.decodeTag(currentResult.data().substring(3));
                session.terminalDirectory = terminal.getString("Directory");
                if (terminal.getBoolean("Clear")) session.terminalOutput.clear();
                var lines = terminal.getList("Lines", 8);
                for (int index = 0; index < lines.size(); index++) session.terminalOutput.add(lines.getString(index));
                String openPath = terminal.getString("Open");
                if (!openPath.isBlank()) {
                    ComputerNetworking.openFile(position, openPath);
                    open(openPath.endsWith(".antpaint") ? "PAINT" : "TEXT");
                }
                while (session.terminalOutput.size() > 40) session.terminalOutput.remove(0);
            } catch (RuntimeException ignored) {
                session.terminalOutput.add("ERROR: CORRUPTED SERVER RESPONSE");
            }
        }
        boolean unlockAllGames = AntarchySettings.unlockAllArchives();
        basilisk.setInstalled(unlockAllGames || physicalDisks().stream().anyMatch(id -> id.getPath().equals("basilisk_game")));
        antman.setInstalled(unlockAllGames || physicalDisks().stream().anyMatch(id -> id.getPath().equals("antman_game")));
        boolean blockleWasInstalled = blockle.installed();
        blockle.setInstalled(unlockAllGames || physicalDisks().stream().anyMatch(id -> id.getPath().equals("blockle_game")));
        if (loggedIn && blockle.installed() && !blockleWasInstalled) {
            ComputerNetworking.requestBlockleState(position);
            session.blockleStateRequested = true;
        }
        blockle.tick();
        if (currentResult != null && currentResult.data().startsWith("16\u0000") && !currentResult.data().equals(session.basiliskResponse)) {
            session.basiliskResponse = currentResult.data();
            String[] scores = currentResult.data().split("\u0000", 3);
            if (scores.length == 3) {
                String[] values = scores[2].split("\u0000", 2);
                if (values.length == 2) try { basilisk.setStoredScores(Integer.parseInt(values[0]), Integer.parseInt(values[1])); } catch (NumberFormatException ignored) { }
            }
        }
        if (currentResult != null && currentResult.data().startsWith("17\u0000") && !currentResult.data().equals(session.antmanResponse)) {
            session.antmanResponse = currentResult.data();
            String[] scores = currentResult.data().split("\u0000", 3);
            if (scores.length == 3) {
                String[] values = scores[2].split("\u0000", 2);
                if (values.length == 2) try { antman.setStoredScores(Integer.parseInt(values[0]), Integer.parseInt(values[1])); } catch (NumberFormatException ignored) { }
            }
        }
        if (activeWindow != null && activeWindow.type.equals("GAMES") && activeWindow.gameOpen) {
            if (activeWindow.gameId.equals("antman")) antman.tick();
            else if (activeWindow.gameId.equals("basilisk")) basilisk.tick();
        }
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
        g.drawString(font, Component.literal("ANTINTOSH OS // v" + Antarchy.MOD_VERSION), l + 28, t + 78, GREEN, false);
        g.drawString(font, Component.literal("INITIALIZING SYSTEM..."), l + 28, t + 105, PALE_GREEN, false);
        g.fill(l + 28, t + 132, l + WIDTH - 28, t + 148, BLACK);
        box(g, l + 28, t + 132, l + WIDTH - 28, t + 148, GREEN);
        g.fill(l + 32, t + 136, l + 32 + (WIDTH - 64) * progress / 100, t + 144, GREEN);
        g.drawString(font, Component.literal(progress + "%"), l + 28, t + 164, GREEN, false);
        g.drawString(font, Component.literal(BOOT_MESSAGES[message]), l + 28, t + 190, PALE_GREEN, false);
    }

    private void renderLogin(GuiGraphics g, int l, int t) {
        g.drawString(font, Component.literal("ANTINTOSH OS // v" + Antarchy.MOD_VERSION), l + 28, t + 25, GREEN, false);
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
        if (loginMessageTicks > 0) wrapLimited(g, loginMessage, formX, t + 232, setup ? 220 : 165, t + HEIGHT - 8, PALE_GREEN);
    }

    private void renderDesktop(GuiGraphics g, int l, int t, int mouseX, int mouseY) {
        session.lastUse = System.currentTimeMillis();
        session.mouseX = mouseX;
        session.mouseY = mouseY;
        session.archiveMouseX = mouseX;
        session.archiveMouseY = mouseY;
        renderWallpaper(g, l, t);
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
            if (ICONS[i].equals("ANTMAIL") && antmailHasUnreadMessages()) drawNotificationBadge(g, x + 35, y + 6);
            g.drawString(font, Component.literal(ICONS[i]), x, y + 35, GREEN, false);
        }
        List<ResourceLocation> disks = physicalDisks().stream().filter(id -> !id.getPath().equals("blockle_game")).toList();
        for (int i = 0; i < disks.size() && i < 3; i++) {
            int x = l + 24 + (i + 1) * 94;
            int y = t + 200 + i / 4 * 54;
            icon(g, "DISK", x + 25, y + 2);
            String name = disks.get(i).getPath();
            g.drawString(font, Component.literal(name.length() > 12 ? name.substring(0, 12) : name), x, y + 35, GREEN, false);
        }
        // Commit the desktop before compositing the independent window layers.
        g.flush();
        if (activeWindow != null && !activeWindow.minimized && session.windows.remove(activeWindow)) {
            session.windows.add(activeWindow);
        }
        int layer = 1;
        for (Window window : session.windows) {
            if (!window.minimized) renderWindow(g, window, l, t, mouseX, mouseY, layer++);
        }
    }

    private boolean antmailHasUnreadMessages() {
        AntmailResultPayload result = AntmailClientState.getMailbox(position);
        AntmailMailbox mailbox = mailbox(result);
        return mailbox != null && mailbox.unreadCount() > 0;
    }

    private void drawNotificationBadge(GuiGraphics g, int x, int y) {
        String[] circle = {
                "....#####....",
                "..##+++++##..",
                ".#+++++++++#.",
                "#+++++++++++#",
                "#+++++++++++#",
                "#+++++++++++#",
                "#+++++++++++#",
                "#+++++++++++#",
                "#+++++++++++#",
                "#+++++++++++#",
                ".#+++++++++#.",
                "..##+++++##..",
                "....#####...."
        };
        int badgeFill = GREEN;
        for (int row = 0; row < circle.length; row++) {
            for (int column = 0; column < circle[row].length(); column++) {
                char pixel = circle[row].charAt(column);
                if (pixel == '#') g.fill(x - 6 + column, y - 6 + row, x - 5 + column, y - 5 + row, BLACK);
                else if (pixel == '+') g.fill(x - 6 + column, y - 6 + row, x - 5 + column, y - 5 + row, badgeFill);
            }
        }
        // Two-pixel stem and a centered two-pixel dot keep the exclamation bold and symmetrical.
        g.fill(x - 1, y - 4, x + 1, y + 2, BLACK);
        g.fill(x - 1, y + 3, x + 1, y + 5, BLACK);
    }

    private void renderWallpaper(GuiGraphics g, int l, int t) {
        int left = l + 9;
        int top = t + 34;
        int right = l + WIDTH - 9;
        int bottom = t + HEIGHT - 8;
        String wallpaperId = ComputerDesktopState.DEFAULT_WALLPAPER.toString();
        int hash = wallpaperId.hashCode();
        int green = 24 + Math.floorMod(hash, 40);
        int blue = 12 + Math.floorMod(hash >>> 8, 24);
        int base = 0xFF000000 | (green / 2 << 16) | (green << 8) | blue;
        g.fill(left, top, right, bottom, base);
        if (wallpaperId.endsWith("grid_ant")) {
            for (int py = top + 2; py < bottom; py += 18) {
                for (int px = left + 2; px < right; px += 36) {
                    g.fill(px, py, px + 2, py + 2, 0xFF173817);
                }
            }
            return;
        }
        int accent = 0xFF000000 | (Math.min(120, green + 34) << 8) | Math.min(80, blue + 34);
        int spacing = 18 + Math.floorMod(hash, 13);
        int offset = Math.floorMod(hash >>> 16, spacing);
        for (int y = top - spacing; y < bottom + spacing; y += spacing) {
            int x = left - spacing + offset;
            while (x < right) {
                int size = 3 + Math.floorMod(x + y + hash, 7);
                g.fill(x, y, Math.min(right, x + size), Math.min(bottom, y + 2), accent);
                x += spacing * 2;
            }
            offset = spacing - offset;
        }
    }

    private String wallpaperName() {
        int separator = session.wallpaperId.indexOf(':');
        String name = separator >= 0 ? session.wallpaperId.substring(separator + 1) : session.wallpaperId;
        return name.toUpperCase();
    }

    private void icon(GuiGraphics g, String type, int x, int y) {
        if (type.equals("ARCHIVE") || type.equals("DISK")) {
            // Chunky drive/archive with an inset label window and lower status slot.
            g.fill(x - 11, y - 2, x + 11, y + 20, BLACK);
            g.fill(x - 8, y + 1, x + 8, y + 16, GREEN);
            g.fill(x - 5, y + 3, x + 5, y + 8, BLACK);
            g.fill(x - 4, y + 4, x + 4, y + 7, PALE_GREEN);
            g.fill(x - 5, y + 11, x + 5, y + 13, BLACK);
            g.fill(x + 5, y + 14, x + 7, y + 16, BLACK);
            return;
        }
        if (type.equals("FILES")) {
            // Folder silhouette with a clearly outlined tab and a second document peeking out.
            g.fill(x - 8, y - 2, x + 9, y + 14, BLACK);
            g.fill(x - 5, y + 1, x + 6, y + 11, PALE_GREEN);
            g.fill(x - 10, y + 4, x + 11, y + 20, BLACK);
            g.fill(x - 7, y + 7, x + 8, y + 17, GREEN);
            g.fill(x - 8, y + 4, x - 2, y + 7, GREEN);
            g.fill(x - 4, y + 10, x + 5, y + 12, BLACK);
            g.fill(x - 4, y + 14, x + 3, y + 16, BLACK);
            return;
        }
        if (type.equals("SETTINGS")) {
            // One continuous cog silhouette, with broad square teeth and an open center.
            g.fill(x - 4, y - 2, x + 4, y + 22, BLACK);
            g.fill(x - 12, y + 6, x + 12, y + 14, BLACK);
            g.fill(x - 10, y + 1, x - 5, y + 5, BLACK);
            g.fill(x + 5, y + 1, x + 10, y + 5, BLACK);
            g.fill(x - 10, y + 15, x - 5, y + 19, BLACK);
            g.fill(x + 5, y + 15, x + 10, y + 19, BLACK);
            g.fill(x - 2, y, x + 2, y + 20, GREEN);
            g.fill(x - 10, y + 8, x + 10, y + 12, GREEN);
            g.fill(x - 7, y + 3, x + 7, y + 17, GREEN);
            g.fill(x - 3, y + 7, x + 3, y + 13, BLACK);
            g.fill(x - 1, y + 9, x + 1, y + 11, PALE_GREEN);
            return;
        }
        if (type.equals("TERMINAL")) {
            g.fill(x - 12, y - 1, x + 12, y + 19, BLACK);
            g.fill(x - 9, y + 2, x + 9, y + 14, GREEN);
            g.fill(x - 6, y + 5, x - 3, y + 8, BLACK);
            g.fill(x - 3, y + 8, x + 1, y + 11, BLACK);
            g.fill(x - 6, y + 11, x - 3, y + 14, BLACK);
            g.fill(x + 3, y + 11, x + 7, y + 13, BLACK);
            g.fill(x - 5, y + 16, x + 5, y + 18, BLACK);
            return;
        }
        if (type.equals("TEXT")) {
            g.fill(x - 9, y - 2, x + 10, y + 20, BLACK);
            g.fill(x - 6, y + 1, x + 7, y + 17, GREEN);
            g.fill(x + 4, y + 1, x + 7, y + 4, BLACK);
            g.fill(x - 3, y + 5, x + 4, y + 7, BLACK);
            g.fill(x - 3, y + 9, x + 4, y + 11, BLACK);
            g.fill(x - 3, y + 13, x + 2, y + 15, BLACK);
            return;
        }
        if (type.equals("PAINT")) {
            // Squared palette with a true thumb notch and a distinct, outlined brush.
            g.fill(x - 11, y + 2, x + 8, y + 20, BLACK);
            g.fill(x - 8, y + 5, x + 5, y + 17, GREEN);
            g.fill(x - 11, y + 5, x - 8, y + 7, GREEN);
            g.fill(x - 6, y + 8, x - 3, y + 11, BLACK);
            g.fill(x, y + 7, x + 3, y + 10, BLACK);
            g.fill(x - 1, y + 13, x + 2, y + 16, BLACK);
            g.fill(x + 6, y - 3, x + 11, y + 1, BLACK);
            g.fill(x + 4, y, x + 9, y + 6, BLACK);
            g.fill(x + 2, y + 5, x + 7, y + 11, BLACK);
            g.fill(x, y + 10, x + 5, y + 16, BLACK);
            g.fill(x + 7, y - 1, x + 9, y + 2, PALE_GREEN);
            g.fill(x + 5, y + 3, x + 7, y + 7, PALE_GREEN);
            g.fill(x + 3, y + 8, x + 5, y + 11, PALE_GREEN);
            return;
        }
        if (type.equals("ANTMAIL")) {
            g.fill(x - 12, y + 1, x + 12, y + 18, BLACK);
            g.fill(x - 9, y + 4, x + 9, y + 15, GREEN);
            // Envelope flap and two symmetric folded corners remain legible at icon scale.
            g.fill(x - 9, y + 4, x + 9, y + 6, BLACK);
            g.fill(x - 7, y + 6, x - 4, y + 8, BLACK);
            g.fill(x + 4, y + 6, x + 7, y + 8, BLACK);
            g.fill(x - 4, y + 8, x + 4, y + 10, BLACK);
            g.fill(x - 9, y + 11, x - 6, y + 13, BLACK);
            g.fill(x + 6, y + 11, x + 9, y + 13, BLACK);
            g.fill(x - 6, y + 13, x - 3, y + 15, BLACK);
            g.fill(x + 3, y + 13, x + 6, y + 15, BLACK);
            return;
        }
        if (type.equals("GAMES")) {
            // Pixel controller with tapered grips, a bold D-pad and paired buttons.
            g.fill(x - 7, y + 2, x + 7, y + 5, BLACK);
            g.fill(x - 11, y + 5, x + 11, y + 15, BLACK);
            g.fill(x - 9, y + 15, x - 5, y + 19, BLACK);
            g.fill(x + 5, y + 15, x + 9, y + 19, BLACK);
            g.fill(x - 8, y + 5, x + 8, y + 13, GREEN);
            g.fill(x - 7, y + 8, x - 1, y + 10, BLACK);
            g.fill(x - 5, y + 6, x - 3, y + 12, BLACK);
            g.fill(x + 3, y + 7, x + 5, y + 9, BLACK);
            g.fill(x + 6, y + 10, x + 8, y + 12, BLACK);
            return;
        }
        if (type.equals("TRASH")) {
            g.fill(x - 8, y + 3, x + 8, y + 20, BLACK);
            g.fill(x - 11, y + 1, x + 11, y + 4, BLACK);
            g.fill(x - 5, y - 1, x + 5, y + 1, BLACK);
            g.fill(x - 6, y + 4, x + 6, y + 18, GREEN);
            g.fill(x - 3, y + 7, x - 1, y + 16, BLACK);
            g.fill(x + 2, y + 7, x + 4, y + 16, BLACK);
            return;
        }
        g.fill(x - 10, y, x + 10, y + 18, BLACK);
        g.fill(x - 8, y + 2, x + 8, y + 16, GREEN);
    }

    private void renderWindow(GuiGraphics g, Window window, int l, int t, int mouseX, int mouseY, int layer) {
        g.flush();
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, layer * 10.0F);
        try {
            renderWindowLayer(g, window, l, t, mouseX, mouseY);
        } finally {
            g.flush();
            g.pose().popPose();
        }
    }

    private void renderWindowLayer(GuiGraphics g, Window window, int l, int t, int mouseX, int mouseY) {
        if (!window.maximized) {
            window.x = Math.max(12, Math.min(WIDTH - window.width - 12, window.x));
            window.y = Math.max(34, Math.min(HEIGHT - window.height - 12, window.y));
        }
        int x = l + windowLocalX(window);
        int y = t + windowLocalY(window);
        int w = windowWidth(window);
        int h = windowHeight(window);
        window.renderWidth = w;
        window.renderHeight = h;
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, GREEN);
        g.fill(x, y, x + w, y + h, BLACK);
        g.fill(x, y, x + w, y + TITLE_BAR_HEIGHT, 0xFF173817);
        int controlsLeft = x + w - WINDOW_CONTROL_WIDTH * WINDOW_CONTROL_COUNT;
        g.drawString(font, Component.literal(trimToWidth(window.title, Math.max(1, controlsLeft - x - 12))), x + 7, y + 6, GREEN, false);
        g.fill(controlsLeft, y, x + w, y + TITLE_BAR_HEIGHT, 0xFF173817);
        g.fill(controlsLeft, y, controlsLeft + 1, y + TITLE_BAR_HEIGHT, GREEN);
        g.fill(controlsLeft + WINDOW_CONTROL_WIDTH, y, controlsLeft + WINDOW_CONTROL_WIDTH + 1, y + TITLE_BAR_HEIGHT, GREEN);
        g.fill(controlsLeft + WINDOW_CONTROL_WIDTH * 2, y, controlsLeft + WINDOW_CONTROL_WIDTH * 2 + 1, y + TITLE_BAR_HEIGHT, GREEN);
        g.drawCenteredString(font, Component.literal("_"), controlsLeft + WINDOW_CONTROL_WIDTH / 2, y + 5, PALE_GREEN);
        g.drawCenteredString(font, Component.literal(window.maximized ? "❐" : "□"), controlsLeft + WINDOW_CONTROL_WIDTH + WINDOW_CONTROL_WIDTH / 2, y + 5, PALE_GREEN);
        g.drawCenteredString(font, Component.literal("X"), controlsLeft + WINDOW_CONTROL_WIDTH * 2 + WINDOW_CONTROL_WIDTH / 2, y + 6, GREEN);
        int cx = x + 8;
        int cy = y + 28;
        enableComputerScissor(g, x + 1, y + TITLE_BAR_HEIGHT, x + w - 1, y + h - 1);
        try {
            if (window.type.equals("ARCHIVE")) renderArchive(g, cx, cy, w - 16, h - 34, window.maximized);
            else if (window.type.equals("FILES")) renderFileExplorer(g, cx, cy, w - 16, h - 34);
            else if (window.type.equals("SETTINGS")) renderSettings(g, cx, cy, h - 34);
            else if (window.type.equals("TERMINAL")) renderTerminal(g, cx, cy, h - 34);
            else if (window.type.equals("TEXT")) renderTextEditor(g, cx, cy, w - 16, h - 34);
            else if (window.type.equals("PAINT")) renderPaint(g, cx, cy, w - 16, h - 34);
            else if (window.type.equals("ANTMAIL")) renderAntmail(g, cx, cy, h - 34);
            else if (window.type.equals("GAMES")) renderGames(g, window, cx, cy, mouseX, mouseY);
            else renderTrash(g, cx, cy);
        } finally {
            g.disableScissor();
        }
    }

    private void enableComputerScissor(GuiGraphics g, int left, int top, int right, int bottom) {
        float scale = uiScale();
        int screenLeft = (int) Math.floor(width / 2.0F + left * scale);
        int screenTop = (int) Math.floor(height / 2.0F + top * scale);
        int screenRight = (int) Math.ceil(width / 2.0F + right * scale);
        int screenBottom = (int) Math.ceil(height / 2.0F + bottom * scale);
        g.enableScissor(screenLeft, screenTop, screenRight, screenBottom);
    }

    private static int windowLocalX(Window window) { return window.maximized ? 14 : window.x; }
    private static int windowLocalY(Window window) { return window.maximized ? 34 : window.y; }
    private static int windowWidth(Window window) { return window.maximized ? WIDTH - 28 : window.width; }
    private static int windowHeight(Window window) { return window.maximized ? HEIGHT - 48 : window.height; }

    private static void toggleMaximized(Window window) {
        if (window.maximized) {
            window.maximized = false;
            window.x = window.restoreX;
            window.y = window.restoreY;
        } else {
            window.restoreX = window.x;
            window.restoreY = window.y;
            window.maximized = true;
        }
    }

    private void renderArchive(GuiGraphics g, int x, int y, int w, int h, boolean maximized) {
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
            g.drawString(font, Component.literal(trimToWidth(Component.translatable(selected.titleKey()).getString(), w)), x, y + 27, GREEN, false);
            int contentBottom = y + h - 20;
            enableComputerScissor(g, x, y + 36, x + w, contentBottom);
            int line = y + 43 - session.archiveDetailScroll * 11;
            line = wrap(g, selected.type().toUpperCase() + " // " + selected.category().toUpperCase(), x, line, w, PALE_GREEN) + 5;
            if (!selected.entityId().isBlank()) line = renderArchivePreview(g, "", selected.entityId(), "", x, line, w, selected.rotation(), selected.renderScale()) + 2;
            for (String descriptionPart : selected.descriptionKeys()) {
                line = renderArchiveDescriptionPart(g, descriptionPart, x, line, w);
            }
            if (!selected.itemId().isBlank()) line = renderArchivePreview(g, selected.itemId(), "", "", x, line, w) + 2;
            if (!selected.enchantmentId().isBlank()) line = renderArchivePreview(g, "", "", selected.enchantmentId(), x, line, w) + 2;
            if (!selected.recipeId().isBlank()) line = renderArchiveRecipe(g, selected.recipeId(), x, line, w) + 2;
            if (!selected.structureId().isBlank()) {
                line = wrap(g, "STRUCTURE // " + selected.structureId(), x, line, w, PALE_GREEN) + 2;
                line = wrap(g, com.craisinlord.antarchy.content.client.ComputerStructureLocatorClientState.get(position), x, line, w, GREEN) + 2;
            }
            if (!selected.dimensionId().isBlank()) line = wrap(g, "DIMENSION // " + selected.dimensionId(), x, line, w, PALE_GREEN);
            g.disableScissor();
            session.archiveDetailMaxScroll = Math.max(0, (line + session.archiveDetailScroll * 11 - contentBottom + 10) / 11);
            session.archiveDetailScroll = Math.min(session.archiveDetailScroll, session.archiveDetailMaxScroll);
            if (session.archiveDetailMaxScroll > 0) g.drawString(font, Component.literal("SCROLL " + session.archiveDetailScroll + "/" + session.archiveDetailMaxScroll), x + w - 82, y + h - 12, PALE_GREEN, false);
            g.drawString(font, Component.literal("[ ESC ] BACK"), x, y + h - 12, PALE_GREEN, false);
            return;
        }

        List<ComputerGuideData.Entry> filteredEntries = filterArchiveEntries(entries);
        g.drawString(font, Component.literal("RECOVERED FILES // " + filteredEntries.size() + "/" + entries.size()), x, y, GREEN, false);
        g.fill(x, y + 14, x + w - 8, y + 35, BLACK);
        box(g, x, y + 14, x + w - 8, y + 35, session.archiveSearchFocused ? GREEN : PALE_GREEN);
        String searchText = session.archiveSearch.isBlank() && !session.archiveSearchFocused ? "SEARCH TITLE" : session.archiveSearch;
        g.drawString(font, Component.literal(trimToWidth(searchText + (session.archiveSearchFocused ? "_" : ""), w - 16)), x + 6, y + 20,
                session.archiveSearch.isBlank() ? PALE_GREEN : GREEN, false);
        if (entries.isEmpty()) {
            g.drawString(font, Component.literal("NO ARCHIVE DATA"), x, y + 28, PALE_GREEN, false);
            return;
        }
        if (filteredEntries.isEmpty()) {
            g.drawString(font, Component.literal("NO MATCHING ENTRIES"), x, y + 52, PALE_GREEN, false);
            return;
        }
        int columns = maximized ? 3 : 1;
        int cellHeight = maximized ? 72 : 38;
        int thumbnailSize = maximized ? 44 : 28;
        int listTop = y + 40;
        int visibleRows = Math.max(1, (h - 58) / cellHeight);
        int totalRows = (filteredEntries.size() + columns - 1) / columns;
        int startRow = Math.max(0, Math.min(session.archiveScroll, Math.max(0, totalRows - visibleRows)));
        for (int rowIndex = startRow; rowIndex < totalRows && rowIndex < startRow + visibleRows; rowIndex++) {
            int rowTop = listTop + (rowIndex - startRow) * cellHeight;
            for (int column = 0; column < columns; column++) {
                int index = rowIndex * columns + column;
                if (index >= filteredEntries.size()) break;
                ComputerGuideData.Entry entry = filteredEntries.get(index);
                int cellWidth = (w - 8) / columns;
                int cellX = x + column * cellWidth;
                int thumbX = maximized ? cellX + (cellWidth - thumbnailSize) / 2 : cellX + 3;
                int thumbY = maximized ? rowTop + 2 : rowTop + 4;
                int textX = maximized ? cellX + 3 : cellX + thumbnailSize + 10;
                int textY = maximized ? rowTop + thumbnailSize + 5 : rowTop + 7;
                boolean hover = inside(cellX, rowTop, cellWidth - 2, cellHeight - 2, session.archiveMouseX, session.archiveMouseY);
                if (hover) g.fill(cellX, rowTop, cellX + cellWidth - 2, rowTop + cellHeight - 2, 0xFF173817);
                g.fill(thumbX, thumbY, thumbX + thumbnailSize, thumbY + thumbnailSize, BLACK);
                renderThumbnail(g, entry, thumbX + thumbnailSize / 2, thumbY + thumbnailSize / 2, hover, thumbnailSize);
                int textWidth = maximized ? cellWidth - 8 : cellWidth - thumbnailSize - 12;
                g.drawString(font, Component.literal(trimToWidth(Component.translatable(entry.titleKey()).getString(), textWidth)), textX, textY, GREEN, false);
                g.drawString(font, Component.literal(trimToWidth(entry.category().toUpperCase(), textWidth)), textX, textY + 12, PALE_GREEN, false);
            }
        }
        int shownStart = startRow * columns;
        int shownEnd = Math.min(filteredEntries.size(), (startRow + visibleRows) * columns);
        if (totalRows > visibleRows) g.drawString(font, Component.literal("SCROLL " + (shownStart + 1) + "-" + shownEnd + " / " + filteredEntries.size()), x, y + h - 12, PALE_GREEN, false);
        else g.drawString(font, Component.literal("[ CLICK ENTRY TO OPEN ]"), x, y + h - 12, PALE_GREEN, false);
    }

    private List<ComputerGuideData.Entry> filterArchiveEntries(List<ComputerGuideData.Entry> entries) {
        String query = session.archiveSearch.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return entries;
        return entries.stream().filter(entry -> Component.translatable(entry.titleKey()).getString().toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private void renderThumbnail(GuiGraphics g, ComputerGuideData.Entry entry, int centerX, int centerY, boolean hover, int size) {
        int background = hover ? 0xFF173817 : 0xFF102010;
        g.fill(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, background);
        if (!entry.recipeId().isBlank() && renderArchiveRecipe(g, entry.recipeId(), centerX - size / 2, centerY - size / 2 + size / 5, size, Math.max(6, size / 4))) return;
        String itemId = entry.coverItemId().isBlank() ? entry.itemId() : entry.coverItemId();
        String entityId = entry.coverEntityId().isBlank() ? entry.entityId() : entry.coverEntityId();
        if (!renderArchiveAsset(g, itemId, entityId, entry.enchantmentId(), entry.coverPotionId(), centerX, centerY, size, entry.rotation(), entry.renderScale())) {
            String fallback = !itemId.isBlank() ? itemId : !entry.enchantmentId().isBlank() ? entry.enchantmentId() : entityId;
            g.drawString(font, Component.literal(trimToWidth(fallback.isBlank() ? "NO RENDER" : fallback, size - 4)), centerX - size / 2 + 2, centerY - 4, hover ? GREEN : PALE_GREEN, false);
        }
    }

    private int renderArchiveDescriptionPart(GuiGraphics g, String part, int x, int y, int w) {
        if (part.startsWith("@item:")) return renderArchivePreview(g, part.substring(6), "", "", x, y, w) + 2;
        if (part.startsWith("@entity:")) return renderArchivePreview(g, "", part.substring(8), "", x, y, w) + 2;
        if (part.startsWith("@enchantment:")) return renderArchivePreview(g, "", "", part.substring(13), x, y, w) + 2;
        if (part.startsWith("@recipe:")) return renderArchiveRecipe(g, part.substring(8), x, y, w) + 2;
        return wrap(g, Component.translatable(part), x, y, w, GREEN) + 3;
    }

    private int renderArchiveRecipe(GuiGraphics g, String recipeId, int x, int y, int w) {
        int slot = 16;
        int recipeWidth = slot * 3 + 24;
        if (!renderArchiveRecipe(g, recipeId, x, y, Math.min(w, recipeWidth), slot)) {
            return wrap(g, "RECIPE // " + recipeId, x, y, w, PALE_GREEN);
        }
        return y + slot * 3;
    }

    private boolean renderArchiveRecipe(GuiGraphics g, String recipeId, int x, int y, int width, int slot) {
        if (Minecraft.getInstance().level == null) return false;
        try {
            var recipeManager = Minecraft.getInstance().level.getRecipeManager();
            ResourceLocation requestedId = ResourceLocation.parse(recipeId);
            RecipeHolder<?> holder = recipeManager.byKey(requestedId).orElse(null);
            if (holder == null) {
                var requestedItem = BuiltInRegistries.ITEM.getOptional(requestedId).orElse(null);
                if (requestedItem != null && requestedItem != Items.AIR) {
                    holder = recipeManager.getRecipes().stream()
                            .filter(candidate -> candidate.value().getResultItem(Minecraft.getInstance().level.registryAccess()).is(requestedItem))
                            .findFirst().orElse(null);
                }
            }
            if (holder == null) return false;
            var recipe = holder.value();
            List<Ingredient> ingredients = recipe.getIngredients();
            int actualSlot = Math.min(slot, Math.max(4, (width - 8) / 4));
            int gridWidth = actualSlot * 3;
            int outputX = x + gridWidth + 8;
            int outputY = y + actualSlot;
            int shapedWidth = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
            int shapedHeight = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : Math.min(3, (ingredients.size() + 2) / 3);
            for (int index = 0; index < 9; index++) {
                int column = index % 3;
                int row = index / 3;
                int cellX = x + column * actualSlot;
                int cellY = y + row * actualSlot;
                g.fill(cellX, cellY, cellX + actualSlot - 1, cellY + actualSlot - 1, 0xFF173817);
                int ingredientIndex = recipe instanceof ShapedRecipe
                        ? (column < shapedWidth && row < shapedHeight ? row * shapedWidth + column : -1)
                        : index;
                if (ingredientIndex >= 0 && ingredientIndex < ingredients.size()) {
                    ItemStack[] choices = ingredients.get(ingredientIndex).getItems();
                    if (choices.length > 0) renderArchiveItem(g, choices[0], cellX, cellY, actualSlot);
                }
            }
            g.fill(x + gridWidth + 2, y + actualSlot + actualSlot / 2 - 1, x + gridWidth + 6, y + actualSlot + actualSlot / 2 + 1, PALE_GREEN);
            g.fill(x + gridWidth + 5, y + actualSlot + actualSlot / 2 - 3, x + gridWidth + 7, y + actualSlot + actualSlot / 2 + 3, PALE_GREEN);
            ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if (!result.isEmpty()) {
                g.fill(outputX, outputY, outputX + actualSlot - 1, outputY + actualSlot - 1, 0xFF173817);
                renderArchiveItem(g, result, outputX, outputY, actualSlot);
            }
            return true;
        } catch (RuntimeException exception) {
            logArchivePreviewOnce("recipe.error." + recipeId, "Archive recipe preview failed id={} error={}", recipeId, exception.toString());
            return false;
        }
    }

    private int renderArchivePreview(GuiGraphics g, String itemId, String entityId, String enchantmentId, int x, int y, int w) {
        return renderArchivePreview(g, itemId, entityId, enchantmentId, x, y, w, 0.0F, 1.0F);
    }

    private int renderArchivePreview(GuiGraphics g, String itemId, String entityId, String enchantmentId, int x, int y, int w, float rotation) {
        return renderArchivePreview(g, itemId, entityId, enchantmentId, x, y, w, rotation, 1.0F);
    }

    private int renderArchivePreview(GuiGraphics g, String itemId, String entityId, String enchantmentId, int x, int y, int w, float rotation, float renderScale) {
        int previewSize = entityId.isBlank() ? 48 : 96;
        int boxWidth = Math.min(previewSize, w);
        g.fill(x, y, x + boxWidth, y + previewSize, 0xFF102010);
        int centerX = x + boxWidth / 2;
        int centerY = y + previewSize / 2;
        if (!renderArchiveAsset(g, itemId, entityId, enchantmentId, "", centerX, centerY, boxWidth, rotation, renderScale)) {
            String fallback = !itemId.isBlank() ? itemId : !enchantmentId.isBlank() ? enchantmentId : entityId;
            g.drawString(font, Component.literal(trimToWidth(fallback, Math.max(1, w - boxWidth - 6))), x + boxWidth + 6, y + previewSize / 2 - 4, PALE_GREEN, false);
        } else if (!itemId.isBlank() || !entityId.isBlank() || !enchantmentId.isBlank()) {
            String label = !itemId.isBlank() ? "ITEM // " + itemId
                    : !enchantmentId.isBlank() ? "ENCHANTMENT // " + enchantmentId : "MOB // " + archiveMobName(entityId);
            g.drawString(font, Component.literal(trimToWidth(label, Math.max(1, w - boxWidth - 6))), x + boxWidth + 6, y + previewSize / 2 - 4, PALE_GREEN, false);
        }
        return y + previewSize;
    }

    private String archiveMobName(String entityId) {
        int separator = entityId.indexOf(':');
        return (separator >= 0 ? entityId.substring(separator + 1) : entityId).replace('_', ' ').toUpperCase(Locale.ROOT);
    }

    private boolean renderArchiveAsset(GuiGraphics g, String itemId, String entityId, String enchantmentId, String potionId, int centerX, int centerY, int size, float rotation, float renderScale) {
        if (!itemId.isBlank()) {
            try {
                ResourceLocation resourceId = ResourceLocation.parse(itemId);
                var item = BuiltInRegistries.ITEM.getOptional(resourceId).orElse(null);
                logArchivePreviewOnce("item.lookup." + itemId, "Archive item preview lookup id={} found={} registryName={}", itemId, item != null, item == null ? "<missing>" : BuiltInRegistries.ITEM.getKey(item));
                if (item != null && !item.equals(net.minecraft.world.item.Items.AIR)) {
                    ItemStack stack = new ItemStack(item);
                    int iconSize = Math.min(32, Math.max(16, size - 4));
                    renderArchiveItem(g, stack, centerX - iconSize / 2, centerY - iconSize / 2, iconSize);
                    logArchivePreviewOnce("item.render." + itemId, "Archive item preview rendered id={} descriptionId={} center=({}, {})", itemId, stack.getDescriptionId(), centerX, centerY);
                    return true;
                }
                logArchivePreviewOnce("item.unrenderable." + itemId, "Archive item preview unavailable id={} (missing or AIR)", itemId);
            } catch (RuntimeException exception) {
                logArchivePreviewOnce("item.error." + itemId, "Archive item preview failed id={} error={}", itemId, exception.toString());
            }
        }
        if (!enchantmentId.isBlank()) {
            ItemStack stack = createArchiveEnchantedBook(enchantmentId);
            if (!stack.isEmpty()) {
                int iconSize = Math.min(32, Math.max(16, size - 4));
                renderArchiveItem(g, stack, centerX - iconSize / 2, centerY - iconSize / 2, iconSize);
                logArchivePreviewOnce("enchantment.render." + enchantmentId, "Archive enchantment preview rendered id={}", enchantmentId);
                return true;
            }
            logArchivePreviewOnce("enchantment.unrenderable." + enchantmentId, "Archive enchantment preview unavailable id={}", enchantmentId);
        }
        if (!potionId.isBlank() && Minecraft.getInstance().level != null) {
            try {
                ResourceLocation potionLocation = ResourceLocation.parse(potionId);
                ResourceKey<Potion> potionKey = ResourceKey.create(Registries.POTION, potionLocation);
                var potion = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.POTION).get(potionKey).orElse(null);
                if (potion != null) {
                    int iconSize = Math.min(32, Math.max(16, size - 4));
                    renderArchiveItem(g, PotionContents.createItemStack(Items.POTION, potion), centerX - iconSize / 2, centerY - iconSize / 2, iconSize);
                    return true;
                }
            } catch (RuntimeException exception) {
                logArchivePreviewOnce("potion.error." + potionId, "Archive potion preview failed id={} error={}", potionId, exception.toString());
            }
        }
        if (!entityId.isBlank() && Minecraft.getInstance().level == null) {
            logArchivePreviewOnce("entity.no_level." + entityId, "Archive mob preview skipped id={} because the client level is null", entityId);
        }
        if (!entityId.isBlank() && Minecraft.getInstance().level != null) {
            try {
                ResourceLocation resourceId = ResourceLocation.parse(entityId);
                var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(resourceId).orElse(null);
                logArchivePreviewOnce("entity.lookup." + entityId, "Archive mob preview lookup id={} found={} registryName={}", entityId, entityType != null, entityType == null ? "<missing>" : BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
                LivingEntity living = archiveEntityPreviews.get(entityId);
                if (living == null && entityType != null && !unavailableArchiveEntities.contains(entityId)) {
                    var entity = entityType.create(Minecraft.getInstance().level);
                    if (entity instanceof LivingEntity created && !created.isRemoved()) {
                        living = created;
                        archiveEntityPreviews.put(entityId, created);
                    } else {
                        unavailableArchiveEntities.add(entityId);
                        logArchivePreviewOnce("entity.unrenderable." + entityId, "Archive mob preview unavailable id={} createdClass={} isLiving={}", entityId, entity == null ? "<null>" : entity.getClass().getName(), entity instanceof LivingEntity);
                    }
                }
                if (living != null && !living.isRemoved()) {
                    if (living instanceof NightmareEntity nightmare) nightmare.setArchivePreviewFlying();
                    living.setPos(0.0D, 0.0D, 0.0D);
                    float yaw = 180.0F + rotation;
                    living.setYRot(yaw);
                    living.yRotO = yaw;
                    living.yBodyRot = yaw;
                    living.yBodyRotO = yaw;
                    living.yHeadRot = yaw;
                    living.yHeadRotO = yaw;
                    living.tickCount = (int) (Minecraft.getInstance().level.getGameTime() & Integer.MAX_VALUE);
                    float largestDimension = Math.max(0.45F, Math.max(living.getBbWidth(), living.getBbHeight()));
                    // Leave room for wide silhouettes and configured yaw rotations (notably the flying squirrel).
                    int entityRenderScale = Math.max(4, Math.min(size, Math.round(size * 0.52F * renderScale / largestDimension)));
                    float entityScale = Math.max(0.01F, living.getScale());
                    Vector3f translation = new Vector3f(0.0F, living.getBbHeight() / 2.0F + 0.0625F * entityScale, 0.0F);
                    Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
                    Quaternionf camera = new Quaternionf();
                    g.flush();
                    enableComputerScissor(g, centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2);
                    g.setColor(0.0F, 1.0F, 0.0F, 1.0F);
                    try {
                        InventoryScreen.renderEntityInInventory(g, centerX, centerY, entityRenderScale / entityScale,
                                translation, pose, camera, living);
                        g.flush();
                    } finally {
                        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        g.disableScissor();
                    }
                    logArchivePreviewOnce("entity.render." + entityId, "Archive mob preview rendered id={} class={} size={} center=({}, {})", entityId, living.getClass().getName(), size, centerX, centerY);
                    return true;
                }
            } catch (RuntimeException exception) {
                logArchivePreviewOnce("entity.error." + entityId, "Archive mob preview failed id={} error={}", entityId, exception.toString());
            }
        }
        return false;
    }

    private void renderArchiveItem(GuiGraphics g, ItemStack stack, int x, int y) {
        renderArchiveItem(g, stack, x, y, 16);
    }

    private void renderArchiveItem(GuiGraphics g, ItemStack stack, int x, int y, int size) {
        g.flush();
        // Keep only the green channel, matching the archive mob treatment and preventing source hues from bleeding through.
        g.setColor(0.0F, 1.0F, 0.0F, 1.0F);
        g.pose().pushPose();
        try {
            g.pose().translate(x, y, 0.0F);
            float scale = size / 16.0F;
            g.pose().scale(scale, scale, 1.0F);
            g.renderItem(stack, 0, 0);
            g.flush();
        } finally {
            g.pose().popPose();
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private ItemStack createArchiveEnchantedBook(String enchantmentId) {
        if (Minecraft.getInstance().level == null) return ItemStack.EMPTY;
        try {
            ResourceLocation id = ResourceLocation.parse(enchantmentId);
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            var holder = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
            if (holder == null) return ItemStack.EMPTY;
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(holder, 1);
            ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
            stack.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable().withTooltip(true));
            return stack;
        } catch (RuntimeException exception) {
            logArchivePreviewOnce("enchantment.error." + enchantmentId, "Archive enchantment preview failed id={} error={}", enchantmentId, exception.toString());
            return ItemStack.EMPTY;
        }
    }

    private void logArchivePreviewOnce(String key, String message, Object... arguments) {
        if (archiveRenderLog.add(key)) Antarchy.LOGGER.info(message, arguments);
    }

    private void renderFileExplorer(GuiGraphics g, int x, int y, int w, int h) {
        var files = ComputerFileSystemClientState.get(position).files();
        g.drawString(font, Component.literal("FILE EXPLORER // " + trimToWidth(session.fileExplorerDirectory, w - 4)), x, y, GREEN, false);
        if (!session.fileExplorerDirectory.equals("/")) g.drawString(font, Component.literal("[ UP ]"), x + w - 40, y, GREEN, false);
        g.drawString(font, Component.literal("TYPE                 PATH             SIZE"), x, y + 18, PALE_GREEN, false);
        int line = y + 34;
        int fileIndex = 0;
        for (String file : files) {
            String[] fields = file.split("\\t", 3);
            if (fields.length < 3 || !isDirectChild(fields[1], session.fileExplorerDirectory)) continue;
            if (fileIndex++ < session.fileExplorerScroll || line > y + h - 32) continue;
            boolean selected = fields[1].equals(session.fileExplorerSelected);
            if (selected) g.fill(x - 3, line - 2, x + w - 3, line + 11, 0xFF173817);
            g.drawString(font, Component.literal(fileIcon(fields[0]) + " " + trimToWidth(fields[0], 30)), x, line, PALE_GREEN, false);
            g.drawString(font, Component.literal(trimToWidth(fields[1], 112)), x + 42, line, GREEN, false);
            g.drawString(font, Component.literal(trimToWidth(fields[2], 28)), x + w - 28, line, PALE_GREEN, false);
            line += 14;
        }
        if (line == y + 34) g.drawString(font, Component.literal("NO FILES // OPEN TERMINAL TO CREATE ONE"), x, y + 38, PALE_GREEN, false);
        else {
            var state = ComputerFileSystemClientState.get(position);
            String footer = session.fileExplorerCreatingDirectory ? "NEW FOLDER: " + session.fileExplorerRename + "_"
                    : session.fileExplorerMoving ? "MOVE TO PATH: " + session.fileExplorerRename + "_"
                    : session.fileExplorerRenaming ? "RENAME: " + session.fileExplorerRename + "_"
                    : session.fileExplorerDeleteConfirm ? "DELETE SELECTED FILE? [ ENTER / ESC ]" : "[ CTRL+N NEW ] [ CTRL+M MOVE ] [ DEL ] [ CTRL+R RENAME ]";
            if (state.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_DELETE
                    || state.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_MOVE) {
                footer += state.lastMutationSuccess() ? " // OK" : " // ERR: " + state.lastMutationError();
            }
            g.drawString(font, Component.literal(trimToWidth(footer, w - 4)), x, y + h - 16, PALE_GREEN, false);
        }
    }

    private String fileIcon(String type) {
        return type.equals("DIRECTORY") ? "[D]" : type.equals("TEXT") ? "[T]" : "[I]";
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
        g.drawString(font, Component.literal("WALLPAPER  DEFAULT // LOCKED"), x, y + 46, PALE_GREEN, false);
        g.drawString(font, Component.literal("PASSWORD  CHANGE IN FULL BUILD"), x, y + 60, GREEN, false);
        g.drawString(font, Component.literal("PHYSICAL DISKS"), x, y + 91, GREEN, false);
        List<ResourceLocation> disks = physicalDisks();
        if (disks.isEmpty()) {
            g.drawString(font, Component.literal("NONE INSTALLED"), x, y + 108, PALE_GREEN, false);
        } else {
            for (int i = 0; i < disks.size() && i < 3; i++) {
                ResourceLocation disk = disks.get(i);
                boolean active = disk.equals(selectedDisk);
                if (active) g.fill(x - 3, y + 105 + i * 17, x + 205, y + 120 + i * 17, 0xFF173817);
                g.drawString(font, Component.literal(trimToWidth(disk.toString(), 202)), x, y + 108 + i * 17, active ? GREEN : PALE_GREEN, false);
            }
            g.drawString(font, Component.literal("[ EJECT SELECTED ]"), x, y + 162, selectedDisk == null ? 0xFF4A754A : GREEN, false);
        }
        g.drawString(font, Component.literal("LOG OUT"), x, y + 74, GREEN, false);
    }

    private void drawWallpaperPreview(GuiGraphics g, int x, int y, int w, int h, String id) {
        int hash = id.hashCode();
        int green = 24 + Math.floorMod(hash, 40);
        int blue = 12 + Math.floorMod(hash >>> 8, 24);
        int base = 0xFF000000 | (green / 2 << 16) | (green << 8) | blue;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, GREEN);
        g.fill(x, y, x + w, y + h, base);
        int accent = 0xFF000000 | (Math.min(120, green + 34) << 8) | Math.min(80, blue + 34);
        for (int py = y + 4; py < y + h; py += 9) {
            for (int px = x + 4; px < x + w; px += 12) {
                if (Math.floorMod(px + py + hash, 3) == 0) g.fill(px, py, Math.min(x + w, px + 3), Math.min(y + h, py + 2), accent);
            }
        }
    }

    private void renderTerminal(GuiGraphics g, int x, int y, int h) {
        g.drawString(font, Component.literal("ANTINTOSH TERMINAL [READY]"), x, y, GREEN, false);
        int line = y + 17;
        for (String output : session.terminalOutput) {
            line = wrapLimited(g, output, x, line, 208, y + h - 48, PALE_GREEN) + 1;
            if (line >= y + h - 48) break;
        }
        g.drawString(font, Component.literal(trimToWidth(session.terminalDirectory + "> " + session.terminalInput + "_", 208)), x, y + h - 26, GREEN, false);
    }

    private void renderPaint(GuiGraphics g, int x, int y, int w, int h) {
        var fileState = ComputerFileSystemClientState.get(position);
        if (!session.paintDirty && fileState.openedPath().endsWith(".antpaint") && !fileState.openedContents().isEmpty()) {
            try {
                AntPaintFile file = AntPaintFileCodec.decode(Base64.getDecoder().decode(fileState.openedContents()));
                session.paintName = file.filename();
                session.antmailPaintPath = fileState.openedPath();
                session.paintCanvas = file.canvas().copy();
            } catch (IllegalArgumentException ignored) {
            }
        }
        g.drawString(font, Component.literal(trimToWidth("ANTPAINT // " + session.paintName + (session.paintDirty ? " *" : ""), w)), x, y, GREEN, false);
        renderPaintToolbar(g, x, y);
        // w and h are already the window's content dimensions; keep rendering in
        // lockstep with paintAt(), which receives these same adjusted dimensions.
        int canvasHeight = Math.max(1, h - 32);
        int size = Math.max(1, Math.min(w / AntPaintCanvas.WIDTH, canvasHeight / AntPaintCanvas.HEIGHT));
        int cx = x + (w - size * AntPaintCanvas.WIDTH) / 2;
        int cy = y + 30 + Math.max(0, (canvasHeight - size * AntPaintCanvas.HEIGHT) / 2);
        g.fill(cx - 1, cy - 1, cx + size * AntPaintCanvas.WIDTH + 1, cy + size * AntPaintCanvas.HEIGHT + 1, BLACK);
        g.fill(cx, cy, cx + size * AntPaintCanvas.WIDTH, cy + size * AntPaintCanvas.HEIGHT, BLACK);
        for (int py = 0; py < AntPaintCanvas.HEIGHT; py++) for (int px = 0; px < AntPaintCanvas.WIDTH; px++) if (session.paintCanvas.get(px, py)) g.fill(cx + px * size, cy + py * size, cx + (px + 1) * size, cy + (py + 1) * size, GREEN);
        int hoverX = (session.mouseX - cx) / size;
        int hoverY = (session.mouseY - cy) / size;
        if (session.mouseX >= cx && session.mouseY >= cy && hoverX >= 0 && hoverX < AntPaintCanvas.WIDTH && hoverY >= 0 && hoverY < AntPaintCanvas.HEIGHT) {
            int pixelX = cx + hoverX * size;
            int pixelY = cy + hoverY * size;
            g.fill(pixelX, pixelY, pixelX + size, pixelY + 1, PALE_GREEN);
            g.fill(pixelX, pixelY + size - 1, pixelX + size, pixelY + size, PALE_GREEN);
            g.fill(pixelX, pixelY, pixelX + 1, pixelY + size, PALE_GREEN);
            g.fill(pixelX + size - 1, pixelY, pixelX + size, pixelY + size, PALE_GREEN);
        }
    }

    private void renderPaintToolbar(GuiGraphics g, int x, int y) {
        for (int index = 0; index < 7; index++) {
            int buttonX = x + index * 28;
            if (index < 3 && ((index == 0 && session.paintTool == AntPaintTool.PENCIL)
                    || (index == 1 && session.paintTool == AntPaintTool.ERASER)
                    || (index == 2 && session.paintTool == AntPaintTool.FILL))) {
                box(g, buttonX, y + 11, buttonX + 22, y + 28, GREEN);
            }
            paintToolbarIcon(g, index, buttonX + 11, y + 19);
        }
    }

    private void paintToolbarIcon(GuiGraphics g, int index, int x, int y) {
        String externalIcon = switch (index) {
            case 0 -> "pencil";
            case 1 -> "eraser";
            case 3 -> "trash";
            case 4 -> "undo";
            case 5 -> "redo";
            default -> null;
        };
        if (externalIcon != null) {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                    "antarchy", "textures/gui/antpaint/" + externalIcon + ".png");
            g.blit(texture, x - 9, y - 9, 0, 0, 18, 18, 24, 24);
            return;
        }
        int color = PALE_GREEN;
        String[][] sprites = {
                // Pencil: graphite point at lower-left, long barrel, and capped eraser at upper-right.
                {
                        ".............##.",
                        "............####",
                        "...........##++#",
                        "..........##+++##",
                        ".........##+++##.",
                        "........##+++##..",
                        ".......##+++##...",
                        "......##+++##....",
                        ".....##+++##.....",
                        "....##+++##......",
                        "...##+++##.......",
                        "..##+++##........",
                        ".##++##..........",
                        ".####............",
                        "..##.............",
                        "...t............."
                },
                // Eraser: angled block with a contrasting paper edge and colored rubber.
                {
                        "................",
                        "...........####.",
                        ".........###++##",
                        ".......###+++##.",
                        ".....###+++##...",
                        "...###+++##.....",
                        ".###+++##.......",
                        "##+++###........",
                        "#++###...........",
                        "####.............",
                        "................",
                        "................",
                        "................",
                        "................",
                        "................",
                        "................"
                },
                // Bucket: pale arched handle, wide rim, tapered body, and visible paint.
                {
                        "....######......",
                        "...##....##.....",
                        "..##......##....",
                        "..#........#....",
                        ".#############..",
                        ".#+++++++++++##.",
                        ".#++gggggg+++##.",
                        ".#+++++++++++##.",
                        "..#+++++++++##..",
                        "..##++++++++##..",
                        "...##++++++##...",
                        "...##++++++##...",
                        "....########....",
                        "................",
                        "................",
                        "................"
                },
                // Clear: trash bin with lid and vertical slats.
                {
                        ".....######.....",
                        "...##########...",
                        "....##.##.##....",
                        "..############..",
                        "..##++++++++##..",
                        "..##++##++##++##.",
                        "..##++##++##++##.",
                        "..##++##++##++##.",
                        "...##++++++++##..",
                        "...############..",
                        "....##......##...",
                        "................",
                        "................",
                        "................",
                        "................",
                        "................"
                },
                // Undo: left-facing hooked arrow.
                {
                        "......####......",
                        "................",
                        "................",
                        "....##..........",
                        "...####.........",
                        "..##++##........",
                        ".##++++##.......",
                        "##++++++++++##..",
                        ".##++++##.......",
                        "..##++##........",
                        "...####.........",
                        "....##..........",
                        "................",
                        "................",
                        "................",
                        "................"
                },
                // Redo: right-facing hooked arrow, mirrored at render time.
                {
                        "......####......",
                        "................",
                        "................",
                        "....##..........",
                        "...####.........",
                        "..##++##........",
                        ".##++++##.......",
                        "##++++++++++##..",
                        ".##++++##.......",
                        "..##++##........",
                        "...####.........",
                        "....##..........",
                        "................",
                        "................",
                        "................",
                        "................"
                },
                // Save icon intentionally retained.
                {
                        ".#############.",
                        "#+++++++++++++#",
                        "#++#####++++++#",
                        "#++#...#++++++#",
                        "#++#...#++++++#",
                        "#++#####++++++#",
                        "#+++++++++++++#",
                        "#++#########++#",
                        "#++#+++++++#++#",
                        "#++#++gg+++#++#",
                        "#++#+++++++#++#",
                        "#++#########++#",
                        "#+++++++++++++#",
                        ".#############.",
                        "..............."
                }
        };
        String[] sprite = sprites[Math.max(0, Math.min(index, sprites.length - 1))];
        if (index == 5) {
            sprite = sprite.clone();
            for (int row = 0; row < sprite.length; row++) {
                sprite[row] = new StringBuilder(sprite[row].substring(0, Math.min(16, sprite[row].length()))).reverse().toString();
            }
        }
        for (int row = 0; row < 16; row++) {
            String line = row < sprite.length ? sprite[row] : "";
            if (line.length() > 16) line = line.substring(0, 16);
            for (int column = 0; column < 16; column++) {
                char pixel = column < line.length() ? line.charAt(column) : '.';
                if (pixel != '#' && pixel != '+' && pixel != 'g' && pixel != 't') continue;
                for (int[] offset : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) {
                    int dx = offset[0];
                    int dy = offset[1];
                    int nx = column + dx;
                    int ny = row + dy;
                    if (nx < 0 || nx >= 16 || ny < 0 || ny >= 16) continue;
                    String neighborLine = ny < sprite.length ? sprite[ny] : "";
                    char neighbor = nx < neighborLine.length() ? neighborLine.charAt(nx) : '.';
                    if (neighbor == '.' || neighbor == ' ') g.fill(x - 8 + nx, y - 8 + ny, x - 7 + nx, y - 7 + ny, 0xFF78A878);
                }
            }
        }
        for (int row = 0; row < 16; row++) {
            String line = row < sprite.length ? sprite[row] : "";
            for (int column = 0; column < 16; column++) {
                char pixel = column < line.length() ? line.charAt(column) : '.';
                if (pixel == '#') g.fill(x - 8 + column, y - 8 + row, x - 7 + column, y - 7 + row, BLACK);
                else if (pixel == '+') g.fill(x - 8 + column, y - 8 + row, x - 7 + column, y - 7 + row, color);
                else if (pixel == 'g') g.fill(x - 8 + column, y - 8 + row, x - 7 + column, y - 7 + row, GREEN);
                else if (pixel == 't') g.fill(x - 8 + column, y - 8 + row, x - 7 + column, y - 7 + row, 0xFF657465);
            }
        }
    }

    private void paintToolbarIconLegacy(GuiGraphics g, int index, int x, int y) {
        int color = PALE_GREEN;
        if (index == 0) {
            // Pencil: a full diagonal silhouette with graphite, wood, painted barrel, ferrule, and eraser.
            g.fill(x - 9, y + 4, x - 5, y + 8, BLACK);
            g.fill(x - 7, y + 2, x - 3, y + 7, BLACK);
            g.fill(x - 5, y, x + 3, y + 6, BLACK);
            g.fill(x - 2, y - 3, x + 6, y + 3, BLACK);
            g.fill(x + 4, y - 6, x + 8, y - 1, BLACK);
            g.fill(x - 8, y + 5, x - 6, y + 7, color);
            g.fill(x - 6, y + 3, x - 4, y + 5, color);
            g.fill(x - 4, y + 1, x - 2, y + 3, color);
            g.fill(x - 2, y - 1, x + 1, y + 2, color);
            g.fill(x + 1, y - 3, x + 4, y, color);
            g.fill(x + 4, y - 5, x + 6, y - 2, color);
            g.fill(x - 4, y + 3, x - 2, y + 5, GREEN);
            g.fill(x - 2, y + 1, x + 1, y + 3, GREEN);
            g.fill(x + 1, y - 1, x + 4, y + 1, GREEN);
            g.fill(x + 3, y - 4, x + 6, y - 2, BLACK);
            g.fill(x + 5, y - 6, x + 8, y - 4, BLACK);
            g.fill(x + 5, y - 4, x + 7, y - 2, color);
        } else if (index == 1) {
            // Eraser: large rotated block with a paper edge, center band, shadow, and shine.
            g.fill(x - 9, y - 3, x + 5, y + 7, BLACK);
            g.fill(x - 6, y - 6, x + 8, y + 4, BLACK);
            g.fill(x - 7, y - 2, x - 2, y + 3, color);
            g.fill(x - 4, y - 4, x + 2, y + 1, color);
            g.fill(x - 1, y - 5, x + 5, y, color);
            g.fill(x - 9, y + 4, x + 3, y + 8, BLACK);
            g.fill(x - 7, y + 4, x - 1, y + 6, color);
            g.fill(x - 6, y - 1, x - 3, y + 2, PALE_GREEN);
            g.fill(x - 3, y - 3, x, y, PALE_GREEN);
            g.fill(x + 1, y - 4, x + 4, y - 1, PALE_GREEN);
            g.fill(x + 3, y, x + 6, y + 3, BLACK);
            g.fill(x + 5, y - 3, x + 8, y + 1, BLACK);
            g.fill(x + 5, y - 2, x + 6, y, color);
        } else if (index == 2) {
            // Fill bucket: arched handle, thick rim, tapered metal body, and visible paint inside.
            g.fill(x - 6, y - 7, x + 6, y - 4, BLACK);
            g.fill(x - 8, y - 5, x - 5, y + 1, BLACK);
            g.fill(x + 5, y - 5, x + 8, y + 1, BLACK);
            g.fill(x - 6, y - 5, x + 6, y - 3, color);
            g.fill(x - 7, y - 2, x + 7, y + 2, BLACK);
            g.fill(x - 5, y - 1, x + 5, y + 1, color);
            g.fill(x - 5, y + 1, x + 5, y + 7, BLACK);
            g.fill(x - 4, y + 3, x + 4, y + 6, color);
            g.fill(x - 3, y + 2, x + 4, y + 4, PALE_GREEN);
            g.fill(x - 4, y + 6, x + 4, y + 8, BLACK);
            g.fill(x - 2, y + 5, x + 3, y + 6, GREEN);
            g.fill(x - 5, y - 6, x - 3, y - 4, GREEN);
            g.fill(x + 3, y - 6, x + 5, y - 4, GREEN);
        } else if (index == 3) {
            // Clear canvas: trash can with lid, slats, and a strong diagonal clear mark.
            g.fill(x - 6, y - 7, x + 6, y - 4, BLACK);
            g.fill(x - 3, y - 9, x + 3, y - 6, BLACK);
            g.fill(x - 5, y - 3, x + 5, y + 8, BLACK);
            g.fill(x - 3, y - 1, x + 3, y + 6, color);
            g.fill(x - 4, y - 2, x + 4, y, PALE_GREEN);
            g.fill(x - 3, y + 1, x - 1, y + 6, GREEN);
            g.fill(x + 1, y + 1, x + 3, y + 6, GREEN);
            g.fill(x - 8, y - 8, x + 8, y - 5, BLACK);
            g.fill(x - 7, y - 4, x + 7, y - 2, BLACK);
            g.fill(x - 8, y - 8, x - 5, y + 8, BLACK);
            g.fill(x + 5, y - 8, x + 8, y + 8, BLACK);
            g.fill(x - 9, y - 7, x - 7, y + 8, color);
            g.fill(x + 7, y - 7, x + 9, y + 8, color);
            g.fill(x - 8, y - 1, x + 8, y + 1, BLACK);
            g.fill(x - 8, y + 5, x + 8, y + 7, BLACK);
            g.fill(x - 8, y - 8, x + 8, y - 6, color);
            g.fill(x - 7, y - 7, x + 7, y - 5, BLACK);
        } else if (index == 4) {
            // Undo: broad pixel arrow, stepped hook, and hollow center.
            g.fill(x - 9, y - 4, x - 5, y + 2, color);
            g.fill(x - 7, y - 6, x - 3, y - 2, color);
            g.fill(x - 7, y, x - 3, y + 4, color);
            g.fill(x - 5, y + 3, x + 3, y + 7, color);
            g.fill(x + 1, y + 1, x + 6, y + 5, color);
            g.fill(x + 4, y - 3, x + 8, y + 3, color);
            g.fill(x + 2, y - 6, x + 6, y - 2, color);
            g.fill(x - 5, y - 1, x + 3, y + 2, BLACK);
            g.fill(x - 3, y + 2, x + 3, y + 4, BLACK);
            g.fill(x - 8, y - 5, x - 5, y - 3, PALE_GREEN);
            g.fill(x + 5, y - 2, x + 7, y, PALE_GREEN);
        } else if (index == 5) {
            // Redo: mirrored broad pixel arrow, stepped hook, and hollow center.
            g.fill(x + 5, y - 4, x + 9, y + 2, color);
            g.fill(x + 3, y - 6, x + 7, y - 2, color);
            g.fill(x + 3, y, x + 7, y + 4, color);
            g.fill(x - 3, y + 3, x + 5, y + 7, color);
            g.fill(x - 6, y + 1, x - 1, y + 5, color);
            g.fill(x - 8, y - 3, x - 4, y + 3, color);
            g.fill(x - 6, y - 6, x - 2, y - 2, color);
            g.fill(x - 3, y - 1, x + 5, y + 2, BLACK);
            g.fill(x - 3, y + 2, x + 3, y + 4, BLACK);
            g.fill(x + 5, y - 5, x + 8, y - 3, PALE_GREEN);
            g.fill(x - 7, y - 2, x - 5, y, PALE_GREEN);
        } else {
            // Save: floppy disk with corner notch, shutter, label, hub, and highlight.
            g.fill(x - 8, y - 8, x + 8, y + 8, BLACK);
            g.fill(x - 5, y - 6, x + 5, y + 6, color);
            g.fill(x - 5, y - 6, x + 2, y - 2, GREEN);
            g.fill(x + 2, y - 6, x + 5, y - 1, BLACK);
            g.fill(x + 4, y - 6, x + 5, y - 3, PALE_GREEN);
            g.fill(x - 5, y, x + 5, y + 6, BLACK);
            g.fill(x - 3, y + 1, x + 3, y + 5, color);
            g.fill(x - 2, y + 2, x + 2, y + 4, PALE_GREEN);
            g.fill(x - 7, y - 7, x - 5, y + 5, PALE_GREEN);
            g.fill(x - 7, y + 6, x + 7, y + 8, BLACK);
            g.fill(x - 4, y + 6, x + 4, y + 7, GREEN);
        }
    }

    private void renderTextEditor(GuiGraphics g, int x, int y, int w, int h) {
        var fileState = ComputerFileSystemClientState.get(position);
        if (session.textDirty
                && fileState.lastMutationSuccess()
                && (fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_CREATE
                || fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_SAVE)
                && fileState.lastMutationPath().equals(session.textPath)) {
            session.textDirty = false;
        }
        if (!session.textSaveAsPendingPath.isBlank()
                && fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_CREATE
                && fileState.lastMutationPath().equals(session.textSaveAsPendingPath)) {
            if (fileState.lastMutationSuccess()) {
                session.textPath = session.textSaveAsPendingPath;
                session.textDirty = false;
                session.textJustSavedAs = true;
            }
            session.textSaveAsPendingPath = "";
        }
        if (!session.textMovePendingPath.isBlank()
                && fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_MOVE
                && fileState.lastMutationPath().equals(session.textMovePendingPath)) {
            if (fileState.lastMutationSuccess()) {
                session.textPath = session.textMovePendingPath;
                session.textJustSavedAs = true;
            }
            session.textMovePendingPath = "";
        }
        if (!fileState.openedPath().isEmpty() && !session.textDirty && !session.textJustSavedAs) {
            session.textPath = fileState.openedPath();
            session.textContent = fileState.openedContents();
            session.textCursor = session.textContent.length();
        }
        session.textJustSavedAs = false;
        String editorTitle = session.textRenaming ? "RENAME // " + session.textRename + "_" : "ANTTEXT // " + session.textPath + (session.textDirty ? " *" : "");
        g.drawString(font, Component.literal(trimToWidth(editorTitle, Math.max(1, w - 104))), x, y, GREEN, false);
        g.drawString(font, Component.literal(trimToWidth("NEW   OPEN   WRITE   SAVE   AS", w)), x, y + 18, PALE_GREEN, false);
        if (fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_CREATE
                || fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_SAVE
                || fileState.lastMutationAction() == com.craisinlord.antarchy.content.network.ComputerAccessPayload.FILE_MOVE) {
            String status = fileState.lastMutationSuccess() ? "OPERATION OK" : "OPERATION ERROR // " + fileState.lastMutationError();
            String displayStatus = trimToWidth(status, Math.min(100, w));
            g.drawString(font, Component.literal(displayStatus), x + w - font.width(displayStatus), y, fileState.lastMutationSuccess() ? PALE_GREEN : GREEN, false);
        }
        g.fill(x + 4, y + 36, x + w - 4, y + h - 4, BLACK);
        if (session.textListing) {
            g.drawString(font, Component.literal("SELECT FILE"), x + 10, y + 46, GREEN, false);
            int line = y + 62;
            int visibleFiles = 0;
            int textIndex = 0;
            for (String file : fileState.files()) {
                String[] fields = file.split("\\t", 3);
                if (fields.length < 3 || !fields[0].equals("TEXT")) continue;
                if (textIndex++ < session.textPickerScroll || line > y + h - 50) continue;
                g.drawString(font, Component.literal(fields[1]), x + 10, line, PALE_GREEN, false);
                line += 14;
                visibleFiles++;
            }
            if (visibleFiles == 0) g.drawString(font, Component.literal("NO TEXT FILES"), x + 10, y + 62, PALE_GREEN, false);
        } else {
            String content = session.textContent.isEmpty() ? "TYPE HERE..." : session.textContent;
            if (session.textFocused) content = content.substring(0, Math.min(session.textCursor, content.length())) + "|"
                    + content.substring(Math.min(session.textCursor, content.length()));
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(content), w - 28);
            int maxLines = Math.max(1, (h - 88) / 11);
            session.textScroll = Math.max(0, Math.min(session.textScroll, Math.max(0, lines.size() - maxLines)));
            for (int i = session.textScroll; i < lines.size() && i < session.textScroll + maxLines; i++) {
                g.drawString(font, lines.get(i), x + 10, y + 46 + (i - session.textScroll) * 11, GREEN, false);
            }
        }
        if (session.textDeleteConfirm) {
            g.drawString(font, Component.literal("DELETE CURRENT FILE? [ ENTER / ESC ]"), x, y + h - 28, PALE_GREEN, false);
        } else {
            g.drawString(font, Component.literal(trimToWidth("[CTRL+S] SAVE  [CTRL+SHIFT+S] SAVE AS", w)), x, y + h - 39, PALE_GREEN, false);
            g.drawString(font, Component.literal(trimToWidth("[CTRL+R] RENAME  [CTRL+DEL] DELETE  [ESC] CLOSE", w)), x, y + h - 26, PALE_GREEN, false);
        }
    }

    private void renderAntmail(GuiGraphics g, int x, int y, int h) {
        var result = AntmailClientState.getMailbox(position);
        AntmailResultPayload lastResponse = AntmailClientState.get(position);
        boolean unconfigured = (result != null && result.address().isBlank())
                || (result == null && lastResponse != null && ("unconfigured".equals(lastResponse.detail())
                || "address_not_found".equals(lastResponse.detail()) || lastResponse.detail().startsWith("registration_failed:")));
        g.drawString(font, Component.literal("ANTMAIL // COMPUTER ADDRESS"), x, y, GREEN, false);
        session.antmailAttachmentStartLine = -1;
        session.antmailVisibleAttachmentCount = 0;
        if (result == null && !unconfigured) {
            g.drawString(font, Component.literal("CONTACTING ANTMAIL NETWORK"), x, y + 20, GREEN, false);
            String waitStatus = lastResponse != null && !lastResponse.detail().isBlank()
                    ? "SERVER // " + lastResponse.detail().toUpperCase()
                    : session.antmailStateWaitTicks >= 100 ? "NO RESPONSE // CHECK CLIENT + SERVER LOGS" : "PLEASE WAIT // RETRYING STATE SYNC";
            g.drawString(font, Component.literal(trimToWidth(waitStatus, 208)), x, y + 40, PALE_GREEN, false);
            box(g, x, y + 56, x + 150, y + 79, GREEN);
            g.drawString(font, Component.literal("[ RETRY STATE LINK ]"), x + 6, y + 63, GREEN, false);
        } else if (unconfigured) {
            String registrationStatus = lastResponse != null && lastResponse.detail().startsWith("registration_failed:")
                    ? "REGISTRATION FAILED // " + lastResponse.detail().substring("registration_failed:".length()).toUpperCase()
                    : "NO ADDRESS CONFIGURED";
            g.drawString(font, Component.literal(trimToWidth(registrationStatus, 208)), x, y + 20, PALE_GREEN, false);
            g.drawString(font, Component.literal("USERNAME // 3-16 CHARACTERS"), x, y + 40, GREEN, false);
            g.fill(x, y + 56, x + 206, y + 79, BLACK);
            box(g, x, y + 56, x + 206, y + 79, GREEN);
            g.drawString(font, Component.literal(trimToWidth(session.antmailUsername + "@antmail.com", 194)), x + 6, y + 63, GREEN, false);
            g.drawString(font, Component.literal("[ ENTER ] REGISTER ADDRESS"), x, y + 88, GREEN, false);
        } else {
            g.drawString(font, Component.literal(trimToWidth(result.address(), 208)), x, y + 20, PALE_GREEN, false);
            if (session.antmailMode.equals("inbox")) box(g, x, y + 30, x + 54, y + 53, PALE_GREEN);
            if (session.antmailMode.equals("sent")) box(g, x + 54, y + 30, x + 100, y + 53, PALE_GREEN);
            if (session.antmailMode.equals("drafts")) box(g, x + 100, y + 30, x + 154, y + 53, PALE_GREEN);
            g.drawString(font, Component.literal("INBOX"), x + 6, y + 38, session.antmailMode.equals("inbox") ? PALE_GREEN : GREEN, false);
            g.drawString(font, Component.literal("SENT"), x + 60, y + 38, session.antmailMode.equals("sent") ? PALE_GREEN : GREEN, false);
            g.drawString(font, Component.literal("DRAFTS"), x + 106, y + 38, session.antmailMode.equals("drafts") ? PALE_GREEN : GREEN, false);
            box(g, x + 158, y + 30, x + 214, y + 53, session.antmailMode.equals("compose") ? PALE_GREEN : GREEN);
            g.drawString(font, Component.literal("COMPOSE"), x + 163, y + 38, session.antmailMode.equals("compose") ? PALE_GREEN : GREEN, false);
            if (session.antmailMode.equals("compose")) {
                g.drawString(font, Component.literal(trimToWidth(session.antmailRecipient, 180)), x + 28, y + 52, PALE_GREEN, false);
                g.drawString(font, Component.literal(trimToWidth(session.antmailSubject, 156)), x + 52, y + 66, PALE_GREEN, false);
                g.drawString(font, Component.literal("TO"), x, y + 52, GREEN, false);
                g.drawString(font, Component.literal("SUBJECT"), x, y + 66, GREEN, false);
                g.drawString(font, Component.literal("BODY"), x, y + 80, GREEN, false);
                wrapLimited(g, session.antmailBody.isEmpty() ? "TYPE MESSAGE..." : session.antmailBody, x, y + 94, 208, y + 124, PALE_GREEN);
                g.drawString(font, Component.literal(trimToWidth("[ " + (session.antmailAttachText ? "X" : " ") + " ] TEXT FILE   [ " + (session.antmailAttachPaint ? "X" : " ") + " ] PAINTING", 208)), x, y + 128, PALE_GREEN, false);
                if (session.antmailPickingAttachment) {
                    g.fill(x + 4, y + 42, x + 210, y + h - 40, BLACK);
                    box(g, x + 4, y + 42, x + 210, y + h - 40, GREEN);
                    g.drawString(font, Component.literal("SELECT FILE TO ATTACH"), x + 10, y + 48, GREEN, false);
                    int line = y + 64;
                    for (String file : ComputerFileSystemClientState.get(position).files()) {
                        String[] fields = file.split("\\t", 3);
                        if (fields.length < 2 || (!fields[0].equals("TEXT") && !fields[0].equals("IMAGE")) || line > y + h - 52) continue;
                        g.drawString(font, Component.literal(trimToWidth(fields[1], 190)), x + 10, line, PALE_GREEN, false);
                        line += 14;
                    }
                }
                g.drawString(font, Component.literal("[ ENTER ] SEND"), x, y + h - 28, GREEN, false);
            } else if (session.antmailMode.equals("message")) {
                AntmailMessage message = selectedAntmailMessage(result);
                if (message != null) {
                    boolean showRetry = session.antmailSent && !message.deliveryStatus().equals("DELIVERED");
                    int attachmentBottom = y + h - (showRetry ? 54 : 36);
                    g.drawString(font, Component.literal("< BACK // " + (session.antmailSent ? "SENT" : "INBOX")), x, y + 40, GREEN, false);
                    g.drawString(font, Component.literal(trimToWidth(session.antmailSent ? message.recipient().fullAddress() : message.sender().fullAddress(), 208)), x, y + 62, PALE_GREEN, false);
                    g.drawString(font, Component.literal(trimToWidth(message.subject(), 208)), x, y + 78, GREEN, false);
                    int messageLine = wrapLimited(g, message.body(), x, y + 98, 208, attachmentBottom, PALE_GREEN) + 4;
                    if (!message.attachments().isEmpty() && messageLine + 9 <= attachmentBottom) {
                        session.antmailAttachmentStartLine = messageLine;
                        for (AntmailAttachment attachment : message.attachments()) {
                            if (messageLine + 9 > attachmentBottom) break;
                            g.drawString(font, Component.literal(trimToWidth("[ SAVE ATTACHMENT ] " + attachment.fileName(), 208)), x, messageLine, GREEN, false);
                            session.antmailVisibleAttachmentCount++;
                            messageLine += 14;
                        }
                    }
                    if (showRetry) {
                        String retryLabel = session.antmailRetryMessageId.equals(message.id().toString()) ? "[ RETRYING... ]" : "[ RETRY DELIVERY ]";
                        g.drawString(font, Component.literal(retryLabel), x, y + h - 46, GREEN, false);
                    }
                    g.drawString(font, Component.literal("[ DELETE MESSAGE ]"), x, y + h - 28, GREEN, false);
                }
            } else if (session.antmailMode.equals("drafts")) {
                List<AntmailDraft> drafts = mailbox(result) == null ? List.of() : mailbox(result).drafts();
                g.drawString(font, Component.literal("DRAFTS // " + drafts.size()), x, y + 62, GREEN, false);
                int line = y + 80;
                for (AntmailDraft draft : drafts) {
                    if (line > y + h - 38) break;
                    g.drawString(font, Component.literal(trimToWidth("* " + (draft.subject().isBlank() ? "UNTITLED DRAFT" : draft.subject()), 30)), x, line, PALE_GREEN, false);
                    line += 14;
                }
                if (drafts.isEmpty()) g.drawString(font, Component.literal("NO SAVED DRAFTS"), x, y + 80, PALE_GREEN, false);
            } else {
                List<AntmailMessage> messages = mailboxMessages(result, session.antmailSent);
                int total = antmailTotal(result, session.antmailSent);
                g.drawString(font, Component.literal((session.antmailSent ? "SENT // " : "INBOX // ") + total), x, y + 62, GREEN, false);
                int line = y + 80;
                for (AntmailMessage message : messages) {
                    if (line > y + h - 38) break;
                    String status = session.antmailSent ? " // " + message.deliveryStatus() : "";
                    g.drawString(font, Component.literal((message.read() ? "  " : "* ") + trimToWidth(message.subject() + status, 28)), x, line, message.read() ? PALE_GREEN : GREEN, false);
                    line += 14;
                }
                g.drawString(font, Component.literal("[ < ] PAGE " + (session.antmailPage + 1) + " [ > ]"), x, y + h - 42, GREEN, false);
            }
        }
        AntmailResultPayload operation = AntmailClientState.get(position);
        if (session.antmailSendPending && operation != null && operation != session.antmailSendBaseline && !operation.messageId().isBlank()) {
            session.antmailSendPending = false;
            session.antmailSendBaseline = null;
            if (operation.status() == 0 || operation.status() == 1) {
                if (session.antmailDraftId != null) AntmailNetworking.deleteDraft(position, session.antmailDraftId);
                session.antmailDraftId = null;
                session.antmailDraftLoaded = false;
                session.antmailStatus = antmailStatus(operation);
                session.antmailMode = "inbox";
                session.antmailFocused = false;
                session.antmailRecipient = "";
                session.antmailSubject = "";
                session.antmailBody = "";
                session.antmailAttachText = false;
                session.antmailAttachPaint = false;
                session.antmailPaintAttachment = null;
                session.antmailPaintLoadPendingPath = "";
                session.antmailTextPath = "";
                session.antmailPaintPath = "";
                session.antmailPage = 0;
                requestAntmailState(true);
            } else {
                session.antmailFocused = true;
                session.antmailStatus = antmailStatus(operation);
            }
        }
        String displayStatus = session.antmailStatus;
        if (!displayStatus.isEmpty()) drawBottomWrapped(g, displayStatus, x, y, h, 208, PALE_GREEN);
    }

    private String antmailStatus(AntmailResultPayload result) {
        if (result.status() == 0) return "DELIVERED";
        if (result.status() == 1) return "QUEUED";
        return "DELIVERY FAILED // " + (result.detail().isBlank() ? "SERVER REJECTED" : result.detail().toUpperCase());
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

    private int antmailTotal(AntmailResultPayload result, boolean sent) {
        if (result == null || result.data().isBlank()) return 0;
        try {
            var tag = AntmailWire.decodeTag(result.data());
            return tag.getInt(sent ? "TotalSent" : "TotalInbox");
        } catch (RuntimeException ignored) {
            return mailboxMessages(result, sent).size();
        }
    }

    private AntmailMessage selectedAntmailMessage(AntmailResultPayload result) {
        List<AntmailMessage> messages = mailboxMessages(result, session.antmailSent);
        if (session.antmailMessageIndex < 0 || session.antmailMessageIndex >= messages.size()) return null;
        AntmailMessage summary = messages.get(session.antmailMessageIndex);
        AntmailMessage detail = AntmailClientState.getMessage(position, summary.id());
        return detail == null ? summary : detail;
    }

    private void renderGames(GuiGraphics g, Window window, int x, int y, int mouseX, int mouseY) {
        g.drawString(font, Component.literal("INSTALLED GAMES"), x, y, GREEN, false);
        if (window.gameOpen) {
            if (window.gameId.equals("antman")) antman.render(g, font, x, y + 16, 214, 155);
            else if (window.gameId.equals("blockle")) blockle.render(g, font, x, y + 16, 214, 155);
            else basilisk.render(g, font, x, y + 16, 214, 155);
            return;
        }
        List<String> games = installedGameIds();
        for (int index = 0; index < games.size(); index++) {
            int rowTop = y + 18 + index * 16;
            if (inside(x - 4, rowTop, 218, 16, mouseX, mouseY)) box(g, x - 4, rowTop, x + 214, rowTop + 16, GREEN);
            g.drawString(font, Component.literal(gameLabel(games.get(index))), x, y + 22 + index * 16, PALE_GREEN, false);
        }
        if (games.isEmpty()) {
            g.drawString(font, Component.literal("NO GAMES INSTALLED"), x, y + 22, PALE_GREEN, false);
            g.drawString(font, Component.literal(trimToWidth("FIND GAME DISKS TO INSTALL PROGRAMS", 214)), x, y + 40, PALE_GREEN, false);
        }
    }

    private List<String> installedGameIds() {
        List<String> games = new ArrayList<>();
        if (basilisk.installed()) games.add("basilisk");
        if (antman.installed()) games.add("antman");
        if (blockle.installed()) games.add("blockle");
        return games;
    }

    private String gameLabel(String gameId) {
        return switch (gameId) {
            case "basilisk" -> "BASILISK  [ ENTER ]";
            case "antman" -> "ANTMAN.EXE  [ ENTER ]";
            default -> "BLOCKLE  [ ENTER ]";
        };
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

    private int wrapLimited(GuiGraphics g, String text, int x, int y, int width, int bottom, int color) {
        for (var line : font.split(Component.literal(text), width)) {
            if (y + 11 > bottom) break;
            g.drawString(font, line, x, y, color, false);
            y += 11;
        }
        return y;
    }

    private void drawBottomWrapped(GuiGraphics g, String text, int x, int y, int h, int width, int color) {
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(text), width);
        int lineY = y + h - 16 - Math.max(0, lines.size() - 1) * 11;
        for (var line : lines) {
            g.drawString(font, line, x, lineY, color, false);
            lineY += 11;
        }
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
            int x = l + windowLocalX(window);
            int y = t + windowLocalY(window);
            int w = windowWidth(window);
            int h = windowHeight(window);
            int contentX = x + 8;
            int contentY = y + 28;
            int contentW = w - 16;
            int contentH = h - 34;
            if (inside(x, y + TITLE_BAR_HEIGHT, w, h - TITLE_BAR_HEIGHT, mouseX, mouseY)) {
                activeWindow = window;
                session.windows.remove(i);
                session.windows.add(window);
                if (window.type.equals("SETTINGS") && inside(contentX, contentY + 105, Math.min(205, contentW), 51, mouseX, mouseY)) {
                    List<ResourceLocation> disks = physicalDisks();
                    int index = ((int) mouseY - (contentY + 105)) / 17;
                    if (index >= 0 && index < disks.size() && index < 3) selectedDisk = disks.get(index);
                    return true;
                }
                if (window.type.equals("ARCHIVE")) {
                    List<ComputerGuideData.Entry> entries = filterArchiveEntries(ComputerGuideData.entriesFor(computer() == null ? List.of() : computer().diskIds()));
                    int archiveX = x + 8;
                    int archiveY = y + 28;
                    if (session.archiveEntryId != null) {
                        if (inside(archiveX, archiveY, w - 16, 20, mouseX, mouseY)) {
                            session.archiveEntryId = null;
                            session.archiveDetailScroll = 0;
                        }
                    } else {
                        if (inside(archiveX, archiveY + 14, w - 16, 21, mouseX, mouseY)) {
                            session.archiveSearchFocused = true;
                            return true;
                        }
                        session.archiveSearchFocused = false;
                        int columns = window.maximized ? 3 : 1;
                        int cellHeight = window.maximized ? 72 : 38;
                        int visibleRows = Math.max(1, (contentH - 58) / cellHeight);
                        int totalRows = (entries.size() + columns - 1) / columns;
                        int startRow = Math.max(0, Math.min(session.archiveScroll, Math.max(0, totalRows - visibleRows)));
                        int gridTop = archiveY + 40;
                        int relativeRow = ((int) mouseY - gridTop) / cellHeight;
                        int relativeColumn = Math.min(columns - 1, Math.max(0, ((int) mouseX - archiveX) / Math.max(1, (contentW - 8) / columns)));
                        int index = (startRow + relativeRow) * columns + relativeColumn;
                        int cellWidth = (contentW - 8) / columns;
                        int cellX = archiveX + relativeColumn * cellWidth;
                        int rowTop = gridTop + relativeRow * cellHeight;
                        if (relativeRow >= 0 && relativeRow < visibleRows && index < entries.size()
                                && inside(cellX, rowTop, cellWidth - 2, cellHeight - 2, mouseX, mouseY)) {
                            ComputerGuideData.Entry openedEntry = entries.get(index);
                            session.archiveEntryId = openedEntry.id();
                            session.archiveDetailScroll = 0;
                            if (!openedEntry.structureId().isBlank()) {
                                com.craisinlord.antarchy.content.client.ComputerStructureLocatorClientState.searching(position, openedEntry.dimensionId());
                                ComputerNetworking.locateStructure(position, openedEntry.id());
                            }
                        }
                    }
                    return true;
                }
                if (window.type.equals("FILES")) {
                    var files = ComputerFileSystemClientState.get(position).files();
                    if (!session.fileExplorerDirectory.equals("/") && mouseY >= contentY && mouseY < contentY + 12 && mouseX > x + w - 48) {
                        session.fileExplorerDirectory = parentDirectory(session.fileExplorerDirectory);
                        session.fileExplorerScroll = 0;
                        return true;
                    }
                    int row = 0;
                    for (String file : files) {
                        String[] fields = file.split("\\t", 3);
                        if (fields.length < 3 || !isDirectChild(fields[1], session.fileExplorerDirectory)) continue;
                        int rowIndex = row++;
                        if (rowIndex < session.fileExplorerScroll) continue;
                        int rowTop = contentY + 34 + (rowIndex - session.fileExplorerScroll) * 14;
                        if (rowTop > contentY + contentH - 32) break;
                        if (!inside(contentX, rowTop - 2, contentW, 14, mouseX, mouseY)) continue;
                        session.fileExplorerSelected = fields[1];
                        if (fields[0].equals("DIRECTORY")) {
                            session.fileExplorerDirectory = fields[1];
                            session.fileExplorerScroll = 0;
                        }
                        else if (fields[0].equals("TEXT") || fields[0].equals("IMAGE")) {
                            ComputerNetworking.openFile(position, fields[1]);
                            open(fields[1].endsWith(".antpaint") ? "PAINT" : "TEXT");
                        }
                        break;
                    }
                    return true;
                }
                if (window.type.equals("PAINT")) {
                    int canvasX = contentX;
                    int canvasY = contentY + 30;
                    int canvasHeight = Math.max(1, contentH - 32);
                    if (inside(canvasX, canvasY, contentW, canvasHeight, mouseX, mouseY)) {
                        if (button == 0 || button == 1) {
                            paintStrokeActive = session.paintTool == AntPaintTool.PENCIL || session.paintTool == AntPaintTool.ERASER;
                            paintStrokeButton = button;
                            lastPaintPixelX = -1;
                            lastPaintPixelY = -1;
                            if (paintStrokeActive) session.paintHistory.record(session.paintCanvas);
                        }
                        if (button == 0 || button == 1) paintAt(mouseX, mouseY, canvasX, canvasY, contentW, canvasHeight);
                    }
                    else if (mouseY >= contentY + 11 && mouseY < contentY + 30 && mouseX >= contentX && mouseX < contentX + Math.min(196, contentW)) {
                        paintStrokeActive = false;
                        paintToolbarAction(((int) mouseX - contentX) / 28);
                    }
                    return true;
                }
                if (window.type.equals("TEXT")) {
                    if (inside(contentX, contentY + 12, contentW, 26, mouseX, mouseY)) {
                        int toolbarX = (int) mouseX - contentX;
                        if (toolbarX < 30) newText();
                        else if (toolbarX < 60) { session.textListing = true; session.textPickerScroll = 0; ComputerNetworking.listFiles(position); }
                        else if (toolbarX < 102) writeText();
                        else if (toolbarX < 138) saveText();
                        else if (toolbarX < 210) beginSaveAs();
                    } else if (session.textListing) {
                        var files = ComputerFileSystemClientState.get(position).files();
                        int index = session.textPickerScroll + ((int) mouseY - (contentY + 62)) / 14;
                        int textIndex = 0;
                        for (String file : files) {
                            String[] fields = file.split("\\t", 3);
                            if (fields.length < 3 || !fields[0].equals("TEXT")) continue;
                            if (textIndex++ == index) {
                                session.textListing = false;
                                ComputerNetworking.openFile(position, fields[1]);
                                break;
                            }
                        }
                    } else {
                        session.textFocused = true;
                        setTextCursorFromMouse(mouseX - (contentX + 10), mouseY - (contentY + 46), contentW - 12);
                    }
                    return true;
                }
                if (window.type.equals("ANTMAIL")) {
                    var result = AntmailClientState.getMailbox(position);
                    if (result == null && !antmailUnconfigured()) {
                        if (inside(contentX, contentY + 56, Math.min(150, contentW), 23, mouseX, mouseY)) {
                            session.antmailStateWaitTicks = 0;
                            requestAntmailState(true);
                        }
                        return true;
                    } else if (result == null || result.address().isBlank()) {
                        if (inside(contentX, contentY + 56, Math.min(206, contentW), 23, mouseX, mouseY)) {
                            session.antmailFocused = true;
                            return true;
                        }
                        if (inside(contentX, contentY + 84, contentW, 24, mouseX, mouseY)) {
                            setupAntmail();
                            return true;
                        }
                    } else if (inside(contentX, contentY + 30, 54, 24, mouseX, mouseY)) {
                        session.antmailMode = "inbox";
                        session.antmailSent = false;
                        session.antmailPage = 0;
                        session.antmailMessageIndex = -1;
                        requestAntmailState(true);
                    } else if (inside(contentX + 54, contentY + 30, 46, 24, mouseX, mouseY)) {
                        session.antmailMode = "sent";
                        session.antmailSent = true;
                        session.antmailPage = 0;
                        session.antmailMessageIndex = -1;
                        requestAntmailState(true);
                    } else if (inside(contentX + 100, contentY + 30, 54, 24, mouseX, mouseY)) {
                        session.antmailMode = "drafts";
                        session.antmailSent = false;
                        session.antmailPage = 0;
                        session.antmailMessageIndex = -1;
                        requestAntmailState(true);
                    } else if (inside(contentX + 158, contentY + 30, Math.min(56, Math.max(0, contentW - 158)), 24, mouseX, mouseY)) {
                        session.antmailMode = "compose";
                        session.antmailFocused = true;
                        session.antmailField = 1;
                        restoreLatestDraft(result);
                    } else if (session.antmailMode.equals("inbox") || session.antmailMode.equals("sent")) {
                        if (mouseY >= contentY + contentH - 48 && mouseY < contentY + contentH - 28) {
                            if (mouseX < contentX + 52 && session.antmailPage > 0) {
                                session.antmailPage--;
                                requestAntmailState(true);
                            } else if (mouseX >= contentX + 142 && (session.antmailPage + 1) * AntmailMailbox.PAGE_SIZE < antmailTotal(result, session.antmailSent)) {
                                session.antmailPage++;
                                requestAntmailState(true);
                            }
                            return true;
                        }
                        int index = ((int) mouseY - (contentY + 80)) / 14;
                        List<AntmailMessage> messages = mailboxMessages(result, session.antmailSent);
                        if (index >= 0 && index < messages.size()) {
                            session.antmailMessageIndex = index;
                            session.antmailMode = "message";
                            AntmailNetworking.requestMessage(position, messages.get(index).id());
                            if (!session.antmailSent) AntmailNetworking.markRead(position, messages.get(index).id());
                        }
                    } else if (session.antmailMode.equals("drafts")) {
                        int index = ((int) mouseY - (contentY + 80)) / 14;
                        AntmailMailbox mailbox = mailbox(result);
                        if (mailbox != null && index >= 0 && index < mailbox.drafts().size()) {
                            restoreDraft(mailbox.drafts().get(index));
                            session.antmailMode = "compose";
                            session.antmailFocused = true;
                            session.antmailField = 1;
                        }
                    } else if (session.antmailMode.equals("message")) {
                        AntmailMessage message = selectedAntmailMessage(result);
                        if (session.antmailSent && message != null && !message.deliveryStatus().equals("DELIVERED")
                                && session.antmailRetryMessageId.isBlank()
                                && mouseY >= contentY + contentH - 50 && mouseY < contentY + contentH - 32) {
                            session.antmailRetryBaseline = AntmailClientState.get(position);
                            session.antmailRetryMessageId = message.id().toString();
                            session.antmailRetryTicks = 100;
                            AntmailNetworking.retry(position, message.id());
                            session.antmailStatus = "RETRYING DELIVERY";
                            return true;
                        }
                        if (mouseY >= contentY + contentH - 32 && mouseY < contentY + contentH - 8) {
                            if (message != null) AntmailNetworking.delete(position, message.id(), session.antmailSent);
                            session.antmailMode = session.antmailSent ? "sent" : "inbox";
                            session.antmailMessageIndex = -1;
                            return true;
                        }
                        if (message != null && session.antmailAttachmentStartLine >= 0
                                && mouseY >= session.antmailAttachmentStartLine
                                && mouseY < session.antmailAttachmentStartLine + session.antmailVisibleAttachmentCount * 14
                                && !message.attachments().isEmpty()) {
                            int attachmentIndex = ((int) mouseY - session.antmailAttachmentStartLine) / 14;
                            saveAntmailAttachment(message.attachments().get(attachmentIndex));
                            return true;
                        }
                        if (mouseY < contentY + 72) {
                            session.antmailMode = session.antmailSent ? "sent" : "inbox";
                            session.antmailMessageIndex = -1;
                        }
                    } else if (session.antmailMode.equals("compose")) {
                        if (session.antmailPickingAttachment) {
                            int row = 0;
                            for (String file : ComputerFileSystemClientState.get(position).files()) {
                                String[] fields = file.split("\\t", 3);
                                if (fields.length < 2 || (!fields[0].equals("TEXT") && !fields[0].equals("IMAGE"))) continue;
                                int rowTop = contentY + 64 + row++ * 14;
                                if (rowTop > contentY + contentH - 52) break;
                                if (mouseY >= rowTop - 2 && mouseY < rowTop + 12) {
                                    session.fileExplorerSelected = fields[1];
                                    session.antmailAttachText = fields[0].equals("TEXT");
                                    session.antmailAttachPaint = fields[0].equals("IMAGE");
                                    session.antmailPickingAttachment = false;
                                    if (session.antmailAttachText) {
                                        session.textPath = fields[1];
                                        session.antmailTextPath = fields[1];
                                    } else {
                                        session.antmailPaintPath = fields[1];
                                        session.paintName = fields[1].substring(fields[1].lastIndexOf('/') + 1);
                                        session.antmailPaintAttachment = null;
                                        session.antmailPaintLoadPendingPath = fields[1];
                                    }
                                    ComputerNetworking.openFile(position, fields[1]);
                                    return true;
                                }
                            }
                            return true;
                        }
                        if (mouseY >= contentY + 116 && mouseY < contentY + 141) {
                            session.antmailPickingAttachment = true;
                            return true;
                        }
                        if (mouseY >= contentY + 42 && mouseY < contentY + 59) {
                            session.antmailField = 1;
                            session.antmailFocused = true;
                        } else if (mouseY >= contentY + 59 && mouseY < contentY + 75) {
                            session.antmailField = 2;
                            session.antmailFocused = true;
                        } else if (mouseY >= contentY + 75 && mouseY < contentY + 116) {
                            session.antmailField = 3;
                            session.antmailFocused = true;
                        } else if (mouseY >= contentY + contentH - 38 && mouseY < contentY + contentH - 8) {
                            sendAntmail();
                        }
                    }
                    return true;
                }
                if (window.type.equals("GAMES") && !window.gameOpen) {
                    List<String> games = installedGameIds();
                    int index = ((int) mouseY - (contentY + 18)) / 16;
                    if (index >= 0 && index < games.size() && mouseY < contentY + 18 + games.size() * 16) {
                        window.gameId = games.get(index);
                        window.gameOpen = true;
                        activeWindow = window;
                        return true;
                    }
                }
                if (window.type.equals("TERMINAL")) { session.terminalFocused = true; return true; }
            }
            if (window.type.equals("SETTINGS") && inside(contentX, contentY + 156, Math.min(205, contentW), 20, mouseX, mouseY)) {
                ejectSelected();
                return true;
            }
            if (window.type.equals("SETTINGS") && inside(contentX, contentY + 70, 90, 18, mouseX, mouseY)) {
                ComputerNetworking.logout(position);
                loggedIn = false;
                session.authenticated = false;
                session.windows.clear();
                activeWindow = null;
                return true;
            }
            if (inside(x, y + TITLE_BAR_HEIGHT, w, h - TITLE_BAR_HEIGHT, mouseX, mouseY)) return true;
            if (inside(x, y, w, TITLE_BAR_HEIGHT, mouseX, mouseY)) {
                int controlsLeft = x + w - WINDOW_CONTROL_WIDTH * WINDOW_CONTROL_COUNT;
                if (inside(controlsLeft + WINDOW_CONTROL_WIDTH * 2, y, WINDOW_CONTROL_WIDTH, TITLE_BAR_HEIGHT, mouseX, mouseY)) {
                    if (window.type.equals("ANTMAIL") && session.antmailMode.equals("compose")) saveAntmailDraft();
                    session.windows.remove(i);
                    if (activeWindow == window) activeWindow = null;
                }
                else if (inside(controlsLeft + WINDOW_CONTROL_WIDTH, y, WINDOW_CONTROL_WIDTH, TITLE_BAR_HEIGHT, mouseX, mouseY)) toggleMaximized(window);
                else if (inside(controlsLeft, y, WINDOW_CONTROL_WIDTH, TITLE_BAR_HEIGHT, mouseX, mouseY)) {
                    window.minimized = true;
                    if (activeWindow == window) activeWindow = null;
                }
                else if (!window.maximized) { dragging = window; dragX = (int) mouseX - x; dragY = (int) mouseY - y; session.windows.remove(i); session.windows.add(window); }
                return true;
            }
        }
        for (int i = 0; i < ICONS.length; i++) {
            int x = l + 24 + i % 4 * 94;
            int y = t + 48 + i / 4 * 76;
            if (inside(x - 6, y - 6, 76, 57, mouseX, mouseY)) { open(ICONS[i]); return true; }
        }
        List<ResourceLocation> disks = physicalDisks().stream().filter(id -> !id.getPath().equals("blockle_game")).toList();
        for (int i = 0; i < disks.size() && i < 3; i++) {
            int x = l + 24 + (i + 1) * 94;
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
        if (paintStrokeActive && button == paintStrokeButton && activeWindow != null && activeWindow.type.equals("PAINT")) {
            int l = -WIDTH / 2;
            int t = -HEIGHT / 2;
            int x = l + windowLocalX(activeWindow) + 8;
            int y = t + windowLocalY(activeWindow) + 58;
            paintAt(mouseX, mouseY, x, y, activeWindow.renderWidth - 16, Math.max(1, activeWindow.renderHeight - 66));
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
            int trashX = l + 24 + (TRASH_ICON_INDEX % 4) * 94;
            int trashY = t + 48 + (TRASH_ICON_INDEX / 4) * 76;
            if (inside(trashX - 6, trashY - 6, 76, 57, mouseX, mouseY)) ejectSelected();
            draggedDisk = null;
        }
        dragging = null;
        if (button == paintStrokeButton) {
            paintStrokeActive = false;
            paintStrokeButton = -1;
            lastPaintPixelX = -1;
            lastPaintPixelY = -1;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Window hoveredWindow = windowAt(mouseX, mouseY);
        if (!loggedIn || hoveredWindow == null) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        activeWindow = hoveredWindow;
        if (activeWindow.type.equals("FILES")) {
            session.fileExplorerScroll = Math.max(0, session.fileExplorerScroll - (int) Math.signum(scrollY));
            return true;
        }
        if (activeWindow.type.equals("TEXT") && session.textListing) {
            session.textPickerScroll = Math.max(0, session.textPickerScroll - (int) Math.signum(scrollY));
            return true;
        }
        if (activeWindow.type.equals("TEXT")) {
            session.textScroll = Math.max(0, session.textScroll - (int) Math.signum(scrollY));
            return true;
        }
        if (activeWindow.type.equals("ARCHIVE")) {
            if (session.archiveEntryId == null) session.archiveScroll = Math.max(0, session.archiveScroll - (int) Math.signum(scrollY));
            else session.archiveDetailScroll = Math.max(0, session.archiveDetailScroll - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
        if (loggedIn && activeWindow != null && activeWindow.type.equals("FILES") && session.fileExplorerRenaming && codePoint >= 32 && codePoint != '/' && codePoint != '\\') {
            if (session.fileExplorerRename.length() < 48) session.fileExplorerRename += codePoint;
            return true;
        }
        if (loggedIn && activeWindow != null && activeWindow.type.equals("ARCHIVE") && session.archiveSearchFocused && codePoint >= 32 && session.archiveSearch.length() < 48) {
            session.archiveSearch += codePoint;
            session.archiveScroll = 0;
            return true;
        }
        if (loggedIn && activeWindow != null && activeWindow.type.equals("FILES") && session.fileExplorerCreatingDirectory && codePoint >= 32 && codePoint != '/' && codePoint != '\\') {
            if (session.fileExplorerRename.length() < 48) session.fileExplorerRename += codePoint;
            return true;
        }
        if (loggedIn && activeWindow != null && activeWindow.type.equals("FILES") && session.fileExplorerMoving && codePoint >= 32 && codePoint != '\\') {
            if (session.fileExplorerRename.length() < 64) session.fileExplorerRename += codePoint;
            return true;
        }
        if (loggedIn && activeWindow != null && activeWindow.type.equals("TEXT") && session.textRenaming && codePoint >= 32 && codePoint != '/' && codePoint != '\\') {
            if (session.textRename.length() < 48) session.textRename += codePoint;
            return true;
        }
        if (loggedIn && activeWindow != null) {
            if (activeWindow.type.equals("GAMES") && activeWindow.gameOpen && activeWindow.gameId.equals("blockle") && blockle.charTyped(codePoint)) return true;
            if (activeWindow.type.equals("TEXT") && session.textFocused && codePoint >= 32) {
                session.textContent = session.textContent.substring(0, session.textCursor) + codePoint + session.textContent.substring(session.textCursor);
                session.textCursor++;
                session.textDirty = true;
                return true;
            }
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
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                activeWindow.gameOpen = false;
                return true;
            }
            if (activeWindow.gameId.equals("antman") ? antman.keyPressed(keyCode) : activeWindow.gameId.equals("blockle") ? blockle.keyPressed(keyCode) : basilisk.keyPressed(keyCode)) return true;
        }
        if (activeWindow != null && activeWindow.type.equals("FILES")) {
            if (session.fileExplorerDeleteConfirm) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.fileExplorerDeleteConfirm = false; return true; }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { session.fileExplorerDeleteConfirm = false; deleteSelectedFile(); return true; }
            }
            if (session.fileExplorerCreatingDirectory) {
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (!session.fileExplorerRename.isEmpty()) session.fileExplorerRename = session.fileExplorerRename.substring(0, session.fileExplorerRename.length() - 1); return true; }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.fileExplorerCreatingDirectory = false; session.fileExplorerRename = ""; return true; }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    if (!session.fileExplorerRename.isBlank()) {
                        String folder = session.fileExplorerRename.replace("\\", "\\\\").replace("\"", "\\\"");
                        ComputerNetworking.terminalCommand(position, session.fileExplorerDirectory, "mkdir \"" + folder + "\"");
                    }
                    session.fileExplorerCreatingDirectory = false;
                    session.fileExplorerRename = "";
                    ComputerNetworking.listFiles(position);
                    return true;
                }
            }
            if (session.fileExplorerMoving) {
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (!session.fileExplorerRename.isEmpty()) session.fileExplorerRename = session.fileExplorerRename.substring(0, session.fileExplorerRename.length() - 1); return true; }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.fileExplorerMoving = false; session.fileExplorerRename = ""; return true; }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    if (!session.fileExplorerRename.isBlank() && !session.fileExplorerSelected.isBlank()) {
                        String destination = session.fileExplorerRename.startsWith("/")
                                ? ComputerFileSystem.normalize(session.fileExplorerRename)
                                : ComputerFileSystem.normalize(session.fileExplorerDirectory + "/" + session.fileExplorerRename);
                        ComputerNetworking.moveFile(position, session.fileExplorerSelected, destination);
                    }
                    session.fileExplorerMoving = false;
                    session.fileExplorerRename = "";
                    ComputerNetworking.listFiles(position);
                    return true;
                }
            }
            if (session.fileExplorerRenaming) {
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!session.fileExplorerRename.isEmpty()) session.fileExplorerRename = session.fileExplorerRename.substring(0, session.fileExplorerRename.length() - 1);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    session.fileExplorerRenaming = false;
                    session.fileExplorerRename = "";
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    renameSelectedFile();
                    return true;
                }
            } else if (keyCode == GLFW.GLFW_KEY_R && hasControl(modifiers)) {
                beginRenameSelectedFile();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                session.fileExplorerDeleteConfirm = true;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_N && hasControl(modifiers)) {
                session.fileExplorerCreatingDirectory = true;
                session.fileExplorerRename = "";
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_M && hasControl(modifiers) && !session.fileExplorerSelected.isBlank()) {
                session.fileExplorerMoving = true;
                session.fileExplorerRename = "";
                return true;
            }
        }
        if (activeWindow != null && activeWindow.type.equals("TERMINAL") && session.terminalFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (!session.terminalInput.isEmpty()) session.terminalInput = session.terminalInput.substring(0, session.terminalInput.length() - 1); return true; }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { executeTerminal(); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.terminalFocused = false; return true; }
        }

        if (activeWindow != null && activeWindow.type.equals("ANTMAIL") && session.antmailFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && session.antmailPickingAttachment) {
                session.antmailPickingAttachment = false;
                return true;
            }
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
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (session.textCursor > 0) { session.textContent = session.textContent.substring(0, session.textCursor - 1) + session.textContent.substring(session.textCursor); session.textCursor--; session.textDirty = true; } return true; }
            if (keyCode == GLFW.GLFW_KEY_DELETE && !hasControl(modifiers)) { if (session.textCursor < session.textContent.length()) { session.textContent = session.textContent.substring(0, session.textCursor) + session.textContent.substring(session.textCursor + 1); session.textDirty = true; } return true; }
            if (keyCode == GLFW.GLFW_KEY_LEFT) { session.textCursor = Math.max(0, session.textCursor - 1); return true; }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) { session.textCursor = Math.min(session.textContent.length(), session.textCursor + 1); return true; }
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) { moveTextCursorVertically(keyCode == GLFW.GLFW_KEY_UP); return true; }
            if (keyCode == GLFW.GLFW_KEY_HOME) { session.textCursor = session.textContent.lastIndexOf('\n', Math.max(0, session.textCursor - 1)) + 1; return true; }
            if (keyCode == GLFW.GLFW_KEY_END) { int next = session.textContent.indexOf('\n', session.textCursor); session.textCursor = next < 0 ? session.textContent.length() : next; return true; }
            if (keyCode == GLFW.GLFW_KEY_ENTER) { session.textContent = session.textContent.substring(0, session.textCursor) + "\n" + session.textContent.substring(session.textCursor); session.textCursor++; session.textDirty = true; return true; }
            if (keyCode == GLFW.GLFW_KEY_S && hasControl(modifiers) && (modifiers & GLFW.GLFW_MOD_SHIFT) == 0) { saveText(); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.textFocused = false; return true; }
        }
        if (activeWindow != null && activeWindow.type.equals("TEXT")) {
            if (session.textDeleteConfirm) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) { session.textDeleteConfirm = false; return true; }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { session.textDeleteConfirm = false; deleteTextFile(); return true; }
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE && hasControl(modifiers)) { session.textDeleteConfirm = true; session.textFocused = false; return true; }
            if (keyCode == GLFW.GLFW_KEY_S && hasControl(modifiers) && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                beginSaveAs();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_R && hasControl(modifiers) && !session.textPath.isBlank()) {
                int slash = session.textPath.lastIndexOf('/');
                session.textRename = session.textPath.substring(slash + 1);
                session.textRenaming = true;
                return true;
            }
            if (session.textRenaming) {
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!session.textRename.isEmpty()) session.textRename = session.textRename.substring(0, session.textRename.length() - 1);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    session.textRenaming = false;
                    session.textRename = "";
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    renameTextFile();
                    return true;
                }
            }
        }
        if (activeWindow != null && activeWindow.type.equals("ARCHIVE") && session.archiveEntryId != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            session.archiveEntryId = null;
            session.archiveDetailScroll = 0;
            return true;
        }
        if (activeWindow != null && activeWindow.type.equals("ARCHIVE") && session.archiveSearchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!session.archiveSearch.isEmpty()) session.archiveSearch = session.archiveSearch.substring(0, session.archiveSearch.length() - 1);
                session.archiveScroll = 0;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                session.archiveSearchFocused = false;
                return true;
            }
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
        ComputerNetworking.terminalCommand(position, session.terminalDirectory, input);
        session.terminalInput = "";
    }

    private void requestAntmailState() {
        requestAntmailState(false);
    }

    private boolean antmailUnconfigured() {
        AntmailResultPayload mailbox = AntmailClientState.getMailbox(position);
        if (mailbox != null) return mailbox.address().isBlank();
        AntmailResultPayload response = AntmailClientState.get(position);
        return response != null && ("unconfigured".equals(response.detail())
                || "address_not_found".equals(response.detail())
                || response.detail().startsWith("registration_failed:"));
    }

    private void requestAntmailState(boolean force) {
        int folder = session.antmailMode.equals("sent") ? com.craisinlord.antarchy.content.network.AntmailStateRequestPayload.SENT
                : session.antmailMode.equals("drafts") ? com.craisinlord.antarchy.content.network.AntmailStateRequestPayload.DRAFTS
                : com.craisinlord.antarchy.content.network.AntmailStateRequestPayload.INBOX;
        AntmailNetworking.requestState(position, folder, session.antmailPage, force ? 0L : AntmailClientState.getVersion(position));
        if (force) session.antmailStateWaitTicks = 0;
    }

    private void setupAntmail() {
        if (!loggedIn) {
            session.antmailStatus = "SETUP ERROR // SIGN IN TO THE COMPUTER FIRST";
            return;
        }
        if (!AntmailAddress.isValidUsername(session.antmailUsername)) {
            session.antmailStatus = "SETUP ERROR // USE 3-16 LOWERCASE LETTERS, NUMBERS, OR _";
            session.antmailFocused = true;
            return;
        }
        AntmailClientState.clear(position);
        AntmailNetworking.setup(position, session.antmailUsername);
        session.antmailStatus = "REGISTERING ADDRESS";
        session.antmailRegistrationPending = true;
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
        if (session.antmailAttachText && (session.textDirty || session.textPath.isBlank() || session.textPath.contains("untitled") || !session.textPath.equals(session.antmailTextPath))) {
            session.antmailStatus = "SEND ERROR // SAVE TEXT FILE FIRST";
            return;
        }
        if (session.antmailAttachPaint && !session.antmailPaintLoadPendingPath.isBlank()) {
            session.antmailStatus = "ATTACHMENT STILL LOADING // TRY AGAIN";
            return;
        }
        if (session.antmailAttachPaint && session.antmailPaintAttachment == null
                && (session.paintDirty || session.paintName.isBlank() || session.paintName.startsWith("UNTITLED")
                || !session.antmailPaintPath.endsWith(session.paintName))) {
            session.antmailStatus = "SEND ERROR // SAVE PAINTING FIRST";
            return;
        }
        List<AntmailAttachment> attachments = composeAttachments();
        session.antmailSendBaseline = AntmailClientState.get(position);
        AntmailNetworking.send(position, session.antmailRecipient, session.antmailSubject, session.antmailBody, attachments);
        session.antmailStatus = "SENDING MESSAGE";
        session.antmailSendPending = true;
        session.antmailFocused = false;
        session.antmailDraftId = null;
        session.antmailDraftLoaded = false;
        session.antmailPickingAttachment = false;
    }

    private void restoreLatestDraft(AntmailResultPayload result) {
        if (session.antmailDraftLoaded) return;
        AntmailMailbox mailbox = mailbox(result);
        if (mailbox == null || mailbox.drafts().isEmpty()) {
            session.antmailDraftLoaded = true;
            return;
        }
        restoreDraft(mailbox.drafts().getLast());
        session.antmailStatus = "DRAFT RECOVERED";
        session.antmailDraftLoaded = true;
    }

    private void restoreDraft(AntmailDraft draft) {
        session.antmailDraftId = draft.id();
        session.antmailRecipient = draft.recipient() == null ? "" : draft.recipient().fullAddress();
        session.antmailSubject = draft.subject();
        session.antmailBody = draft.body();
        session.antmailAttachText = false;
        session.antmailAttachPaint = false;
        session.antmailPickingAttachment = false;
        for (AntmailAttachment attachment : draft.attachments()) {
            if (attachment instanceof AntmailAttachment.TextFile text) {
                session.antmailAttachText = true;
                session.textPath = "/documents/" + text.fileName();
                session.antmailTextPath = session.textPath;
                session.textContent = text.contents();
            } else if (attachment instanceof AntmailAttachment.PaintImage paint) {
                session.antmailAttachPaint = true;
                session.paintName = paint.fileName();
                session.antmailPaintPath = "/pictures/" + paint.fileName();
                session.antmailPaintAttachment = paint;
                session.antmailPaintLoadPendingPath = "";
                AntPaintCanvas canvas = new AntPaintCanvas();
                byte[] pixels = paint.pixels();
                for (int index = 0; index < pixels.length; index++) if (pixels[index] == 1) canvas.set(index % AntPaintCanvas.WIDTH, index / AntPaintCanvas.WIDTH, true);
                session.paintCanvas = canvas;
            }
        }
    }

    private void saveAntmailDraft() {
        try {
            if (session.antmailRecipient.isBlank() && session.antmailSubject.isBlank() && session.antmailBody.isBlank()) return;
            AntmailResultPayload result = AntmailClientState.getMailbox(position);
            AntmailMailbox mailbox = mailbox(result);
            if (mailbox == null) return;
            AntmailDraft draft = new AntmailDraft(session.antmailDraftId, AntmailAddress.parse(mailbox.address().fullAddress()),
                    session.antmailRecipient.isBlank() ? null : AntmailAddress.parse(session.antmailRecipient), session.antmailSubject, session.antmailBody, composeAttachments());
            AntmailNetworking.saveDraft(position, draft);
        } catch (RuntimeException ignored) {
            session.antmailStatus = "DRAFT SAVE ERROR // CHECK RECIPIENT";
        }
    }

    private List<AntmailAttachment> composeAttachments() {
        List<AntmailAttachment> attachments = new ArrayList<>();
        if (session.antmailAttachText) attachments.add(new AntmailAttachment.TextFile(session.textPath.substring(session.textPath.lastIndexOf('/') + 1), session.textContent));
        if (session.antmailAttachPaint && session.antmailPaintAttachment != null) {
            attachments.add(session.antmailPaintAttachment);
        } else if (session.antmailAttachPaint) {
            byte[] pixels = new byte[AntPaintCanvas.WIDTH * AntPaintCanvas.HEIGHT];
            for (int py = 0; py < AntPaintCanvas.HEIGHT; py++) for (int px = 0; px < AntPaintCanvas.WIDTH; px++) pixels[py * AntPaintCanvas.WIDTH + px] = (byte) (session.paintCanvas.get(px, py) ? 1 : 0);
            attachments.add(new AntmailAttachment.PaintImage(session.paintName, AntPaintCanvas.WIDTH, AntPaintCanvas.HEIGHT, pixels));
        }
        return attachments;
    }

    private void syncAntmailPaintAttachment() {
        String requestedPath = session.antmailPaintLoadPendingPath;
        if (requestedPath.isBlank()) return;
        ComputerFileSystemClientState.State fileState = ComputerFileSystemClientState.get(position);
        if (!requestedPath.equals(fileState.openedPath()) || fileState.openedContents().isBlank()) return;
        try {
            AntPaintFile file = AntPaintFileCodec.decode(Base64.getDecoder().decode(fileState.openedContents()));
            session.paintName = file.filename();
            session.antmailPaintPath = requestedPath;
            session.antmailPaintAttachment = AntmailAttachmentFiles.fromPaintFile(file);
            session.antmailPaintLoadPendingPath = "";
            session.antmailStatus = "PAINTING ATTACHED // " + file.filename();
        } catch (RuntimeException exception) {
            session.antmailPaintLoadPendingPath = "";
            session.antmailStatus = "ATTACHMENT ERROR // INVALID PAINT FILE";
        }
    }

    private void saveAntmailAttachment(AntmailAttachment attachment) {
        try {
            if (attachment instanceof AntmailAttachment.TextFile text) {
                String path = "/documents/" + text.fileName();
                if (computerFileExists(path)) ComputerNetworking.saveFile(position, path, text.contents());
                else ComputerNetworking.createFile(position, path, text.contents());
                session.antmailStatus = "ATTACHMENT SAVED // " + text.fileName();
            } else if (attachment instanceof AntmailAttachment.PaintImage paint) {
                String name = paint.fileName().endsWith(".antpaint") ? paint.fileName() : paint.fileName() + ".antpaint";
                AntPaintFile file = AntmailAttachmentFiles.toPaintFile(paint, UUID.randomUUID().toString(), 0L, 0L);
                String encoded = Base64.getEncoder().encodeToString(AntPaintFileCodec.encode(file));
                String path = "/pictures/" + name;
                if (computerFileExists(path)) ComputerNetworking.saveFile(position, path, encoded);
                else ComputerNetworking.createFile(position, path, encoded);
                session.antmailStatus = "ATTACHMENT SAVED // " + name;
            }
            ComputerNetworking.listFiles(position);
        } catch (RuntimeException exception) {
            session.antmailStatus = "ATTACHMENT ERROR // INVALID FILE";
        }
    }

    private boolean computerFileExists(String path) {
        return ComputerFileSystemClientState.get(position).files().stream().anyMatch(entry -> entry.contains("\t" + path + "\t"));
    }

    private void newText() {
        session.textPath = "/documents/untitled.txt";
        session.textContent = "";
        session.textCursor = 0;
        session.textScroll = 0;
        session.textDirty = true;
        session.textListing = false;
        session.textFocused = true;
    }

    private void beginSaveAs() {
        int slash = session.textPath.lastIndexOf('/');
        session.textRename = session.textPath.substring(slash + 1);
        session.textSaveAs = true;
        session.textRenaming = true;
        session.textFocused = false;
    }

    private void setTextCursorFromMouse(double mouseX, double mouseY, int width) {
        if (session.textContent.isEmpty()) { session.textCursor = 0; return; }
        StringBuilder line = new StringBuilder();
        int cursor = 0;
        int targetLine = Math.max(0, (int) (mouseY / 11));
        int currentLine = 0;
        for (int index = 0; index < session.textContent.length(); index++) {
            char character = session.textContent.charAt(index);
            if (character == '\n' || font.width(line.toString() + character) > width) {
                if (currentLine == targetLine) {
                    session.textCursor = index;
                    return;
                }
                currentLine++;
                line.setLength(0);
                if (character == '\n') continue;
            }
            line.append(character);
            cursor = index + 1;
        }
        session.textCursor = cursor;
    }

    private void moveTextCursorVertically(boolean up) {
        int lineStart = session.textContent.lastIndexOf('\n', Math.max(0, session.textCursor - 1)) + 1;
        int column = session.textCursor - lineStart;
        int target = up ? lineStart - 1 : session.textContent.indexOf('\n', session.textCursor);
        if (target < 0) return;
        int targetStart = up ? session.textContent.lastIndexOf('\n', target - 1) + 1 : target + 1;
        int targetEnd = session.textContent.indexOf('\n', targetStart);
        if (targetEnd < 0) targetEnd = session.textContent.length();
        session.textCursor = Math.min(targetStart + column, targetEnd);
    }

    private void writeText() {
        boolean exists = ComputerFileSystemClientState.get(position).files().stream().anyMatch(entry -> entry.contains("\t" + session.textPath + "\t"));
        if (exists) ComputerNetworking.saveFile(position, session.textPath, session.textContent);
        else ComputerNetworking.createFile(position, session.textPath, session.textContent);
        ComputerNetworking.listFiles(position);
    }

    private void saveText() {
        boolean exists = ComputerFileSystemClientState.get(position).files().stream().anyMatch(entry -> entry.contains("\t" + session.textPath + "\t"));
        if (exists) ComputerNetworking.saveFile(position, session.textPath, session.textContent);
        else ComputerNetworking.createFile(position, session.textPath, session.textContent);
        ComputerNetworking.listFiles(position);
    }

    private void renameTextFile() {
        if (!session.textRename.isBlank() && !session.textRename.contains("/") && !ComputerFileSystem.isProtected(session.textPath)) {
            String parent = parentDirectory(session.textPath);
            String destination = parent.equals("/") ? "/" + session.textRename : parent + "/" + session.textRename;
            if (session.textSaveAs) {
                session.textSaveAsPendingPath = destination;
                ComputerNetworking.createFile(position, destination, session.textContent);
            } else {
                ComputerNetworking.moveFile(position, session.textPath, destination);
                session.textMovePendingPath = destination;
            }
            ComputerNetworking.listFiles(position);
        }
        session.textSaveAs = false;
        session.textRenaming = false;
        session.textRename = "";
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
        session.antmailPaintPath = savedPath;
        session.antmailPaintAttachment = null;
        session.antmailPaintLoadPendingPath = "";
        session.paintDirty = false;
        ComputerNetworking.listFiles(position);
    }

    private void paintTool(int offset) {
        session.paintTool = offset < 52 ? AntPaintTool.PENCIL : offset < 108 ? AntPaintTool.ERASER : AntPaintTool.FILL;
    }

    private void paintToolbarAction(int index) {
        if (index == 0) session.paintTool = AntPaintTool.PENCIL;
        else if (index == 1) session.paintTool = AntPaintTool.ERASER;
        else if (index == 2) session.paintTool = AntPaintTool.FILL;
        else if (index == 3) {
            session.paintHistory.record(session.paintCanvas);
            session.paintCanvas.clear();
            session.paintDirty = true;
        } else if (index == 4 && session.paintHistory.canUndo()) {
            session.paintCanvas = session.paintHistory.undo(session.paintCanvas);
            session.paintDirty = true;
        } else if (index == 5 && session.paintHistory.canRedo()) {
            session.paintCanvas = session.paintHistory.redo(session.paintCanvas);
            session.paintDirty = true;
        } else if (index == 6) {
            savePaint();
        }
    }

    private void paintAt(double mouseX, double mouseY, int x, int y, int w, int h) {
        int size = Math.min(w / AntPaintCanvas.WIDTH, h / AntPaintCanvas.HEIGHT);
        if (size <= 0) return;
        int canvasX = x + (w - size * AntPaintCanvas.WIDTH) / 2;
        int px = (int) ((mouseX - canvasX) / size);
        int canvasY = y + Math.max(0, (h - size * AntPaintCanvas.HEIGHT) / 2);
        int py = (int) ((mouseY - canvasY) / size);
        if (px < 0 || py < 0 || px >= AntPaintCanvas.WIDTH || py >= AntPaintCanvas.HEIGHT) {
            lastPaintPixelX = -1;
            lastPaintPixelY = -1;
            return;
        }
        if (session.paintTool == AntPaintTool.FILL) {
            session.paintHistory.record(session.paintCanvas);
            if (session.paintCanvas.floodFill(px, py, true) > 0) session.paintDirty = true;
            return;
        }
        if (lastPaintPixelX < 0 || lastPaintPixelY < 0) applyPaintPixel(px, py);
        else {
            int dx = Math.abs(px - lastPaintPixelX);
            int dy = Math.abs(py - lastPaintPixelY);
            int stepX = lastPaintPixelX < px ? 1 : -1;
            int stepY = lastPaintPixelY < py ? 1 : -1;
            int error = dx - dy;
            int currentX = lastPaintPixelX;
            int currentY = lastPaintPixelY;
            while (currentX != px || currentY != py) {
                int twiceError = error * 2;
                if (twiceError > -dy) { error -= dy; currentX += stepX; }
                if (twiceError < dx) { error += dx; currentY += stepY; }
                applyPaintPixel(currentX, currentY);
            }
        }
        lastPaintPixelX = px;
        lastPaintPixelY = py;
    }

    private void applyPaintPixel(int x, int y) {
        boolean changed = session.paintTool == AntPaintTool.PENCIL
                ? session.paintCanvas.draw(x, y)
                : session.paintCanvas.erase(x, y);
        if (changed) session.paintDirty = true;
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

    private void beginRenameSelectedFile() {
        if (session.fileExplorerSelected.isEmpty() || session.fileExplorerSelected.equals("/") || ComputerFileSystem.isProtected(session.fileExplorerSelected)) return;
        int slash = session.fileExplorerSelected.lastIndexOf('/');
        session.fileExplorerRename = session.fileExplorerSelected.substring(slash + 1);
        session.fileExplorerRenaming = true;
    }

    private void renameSelectedFile() {
        if (!session.fileExplorerRename.isBlank() && !session.fileExplorerRename.contains("/")) {
            String parent = parentDirectory(session.fileExplorerSelected);
            String destination = parent.equals("/") ? "/" + session.fileExplorerRename : parent + "/" + session.fileExplorerRename;
            ComputerNetworking.moveFile(position, session.fileExplorerSelected, destination);
            session.fileExplorerSelected = destination;
            ComputerNetworking.listFiles(position);
        }
        session.fileExplorerRenaming = false;
        session.fileExplorerRename = "";
    }

    private void deleteSelectedFile() {
        if (session.fileExplorerSelected.isEmpty() || session.fileExplorerSelected.equals("/") || ComputerFileSystem.isProtected(session.fileExplorerSelected)) return;
        ComputerNetworking.deleteFile(position, session.fileExplorerSelected);
        session.fileExplorerSelected = "";
        ComputerNetworking.listFiles(position);
    }

    private void deleteTextFile() {
        if (session.textPath.isBlank() || ComputerFileSystem.isProtected(session.textPath)) return;
        ComputerNetworking.deleteFile(position, session.textPath);
        session.textPath = "/documents/untitled.txt";
        session.textContent = "";
        session.textCursor = 0;
        session.textDirty = false;
        ComputerNetworking.listFiles(position);
    }

    private void open(String type) {
        for (int index = 0; index < session.windows.size(); index++) {
            Window window = session.windows.get(index);
            if (!window.type.equals(type)) continue;
            window.minimized = false;
            session.windows.remove(index);
            session.windows.add(window);
            activeWindow = window;
            if (type.equals("FILES")) ComputerNetworking.listFiles(position);
            return;
        }
        Window window = new Window(type, TITLES.get(type), 112 + session.windows.size() * 12, 52 + session.windows.size() * 10);
        session.windows.add(window);
        activeWindow = window;
        if (type.equals("ANTMAIL")) requestAntmailState();
        else if (type.equals("FILES")) ComputerNetworking.listFiles(position);
    }

    private Window windowAt(double mouseX, double mouseY) {
        float scale = uiScale();
        double localMouseX = (mouseX - width / 2.0F) / scale;
        double localMouseY = (mouseY - height / 2.0F) / scale;
        int left = -WIDTH / 2;
        int top = -HEIGHT / 2;
        for (int index = session.windows.size() - 1; index >= 0; index--) {
            Window window = session.windows.get(index);
            if (window.minimized) continue;
            int windowX = left + windowLocalX(window);
            int windowY = top + windowLocalY(window);
            int renderedWidth = windowWidth(window);
            int renderedHeight = windowHeight(window);
            if (inside(windowX, windowY + TITLE_BAR_HEIGHT, renderedWidth, renderedHeight - TITLE_BAR_HEIGHT, localMouseX, localMouseY)) return window;
        }
        return null;
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
        private String terminalResponse = "";
        private boolean terminalFocused;
        private String textPath = "/documents/untitled.txt";
        private String textContent = "";
        private int textCursor;
        private int textScroll;
        private boolean textDirty;
        private boolean textFocused;
        private boolean textListing;
        private int textPickerScroll;
        private boolean textRenaming;
        private boolean textSaveAs;
        private String textSaveAsPendingPath = "";
        private String textMovePendingPath = "";
        private boolean textJustSavedAs;
        private boolean textDeleteConfirm;
        private String textRename = "";
        private ResourceLocation archiveEntryId;
        private int archiveScroll;
        private String archiveSearch = "";
        private boolean archiveSearchFocused;
        private int fileExplorerScroll;
        private boolean fileExplorerDeleteConfirm;
        private boolean fileExplorerCreatingDirectory;
        private boolean fileExplorerMoving;
        private int archiveDetailScroll;
        private int archiveDetailMaxScroll;
        private int archiveMouseX;
        private int archiveMouseY;
        private int mouseX;
        private int mouseY;
        private AntPaintCanvas paintCanvas = new AntPaintCanvas();
        private final AntPaintHistory paintHistory = new AntPaintHistory();
        private AntPaintTool paintTool = AntPaintTool.PENCIL;
        private String paintName = "UNTITLED.ANTPAINT";
        private boolean paintDirty;
        private String antmailUsername = "";
        private String antmailStatus = "";
        private boolean antmailSendPending;
        private AntmailResultPayload antmailSendBaseline;
        private String antmailRetryMessageId = "";
        private AntmailResultPayload antmailRetryBaseline;
        private int antmailRetryTicks;
        private boolean antmailRegistrationPending;
        private int antmailRefreshTicks;
        private int antmailStateWaitTicks;
        private boolean antmailFocused;
        private String antmailMode = "inbox";
        private String antmailRecipient = "";
        private String antmailSubject = "";
        private String antmailBody = "";
        private int antmailField;
        private boolean antmailSent;
        private int antmailMessageIndex = -1;
        private int antmailPage;
        private int antmailAttachmentStartLine = -1;
        private int antmailVisibleAttachmentCount;
        private boolean antmailAttachText;
        private boolean antmailAttachPaint;
        private String antmailTextPath = "";
        private String antmailPaintPath = "";
        private AntmailAttachment.PaintImage antmailPaintAttachment;
        private String antmailPaintLoadPendingPath = "";
        private UUID antmailDraftId;
        private boolean antmailDraftLoaded;
        private boolean antmailPickingAttachment;
        private String fileExplorerDirectory = "/";
        private String fileExplorerSelected = "";
        private String fileExplorerRename = "";
        private boolean fileExplorerRenaming;
        private String wallpaperId = ComputerDesktopState.DEFAULT_WALLPAPER.toString();
        private List<String> wallpapers = List.of(ComputerDesktopState.DEFAULT_WALLPAPER.toString());
        private boolean desktopRequested;
        private int bootTicks = -1;
        private String basiliskResponse = "";
        private boolean basiliskStateRequested;
        private String antmanResponse = "";
        private boolean antmanStateRequested;
        private boolean blockleStateRequested;
    }

    private static final class Window {
        private final String type;
        private final String title;
        private final int width = 230;
        private final int height = 210;
        private int x;
        private int y;
        private int restoreX;
        private int restoreY;
        private int renderWidth = width;
        private int renderHeight = height;
        private boolean minimized;
        private boolean maximized;
        private boolean gameOpen;
        private String gameId = "basilisk";

        private Window(String type, String title, int x, int y) {
            this.type = type;
            this.title = title;
            this.x = x;
            this.y = y;
            this.restoreX = x;
            this.restoreY = y;
        }
    }
}
