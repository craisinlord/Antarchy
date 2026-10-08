package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;

final class PortalRenderWatchdog {
    private static final long STALL_NANOS = 5_000_000_000L;
    private static final long RECENT_PORTAL_NANOS = 10_000_000_000L;
    private static volatile Thread renderThread;
    private static volatile long lastFrameNanos;
    private static volatile long lastPortalViewNanos = Long.MIN_VALUE;
    private static volatile boolean reportedStall;
    private static Thread watcher;

    private PortalRenderWatchdog() {
    }

    static void frame() {
        lastFrameNanos = System.nanoTime();
        reportedStall = false;
        if (watcher == null) {
            renderThread = Thread.currentThread();
            watcher = new Thread(PortalRenderWatchdog::watch, "Antarchy portal render watchdog");
            watcher.setDaemon(true);
            watcher.start();
        }
    }

    static void portalViewDrawn() {
        lastPortalViewNanos = System.nanoTime();
    }

    private static void watch() {
        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException exception) {
                return;
            }
            long now = System.nanoTime();
            long lastFrame = lastFrameNanos;
            long lastPortal = lastPortalViewNanos;
            Thread thread = renderThread;
            if (reportedStall || thread == null || lastPortal == Long.MIN_VALUE
                    || now - lastFrame < STALL_NANOS || lastFrame - lastPortal > RECENT_PORTAL_NANOS) {
                continue;
            }
            reportedStall = true;
            StringBuilder stack = new StringBuilder();
            for (StackTraceElement element : thread.getStackTrace()) {
                stack.append("\n\tat ").append(element);
            }
            Antarchy.LOGGER.warn("Render thread has not finished a frame for {} ms after portal views were drawn (state {}). Stack:{}",
                    (now - lastFrame) / 1_000_000L, thread.getState(), stack);
        }
    }
}
