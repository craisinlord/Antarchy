package com.craisinlord.antarchy.content.client.renderer;

import java.util.Queue;
import java.util.function.LongSupplier;

public final class SectionUploadBudget {
    private static final long SHARE_DIVISOR = 5L;
    private static final long MIN_NANOS = 2_000_000L;
    private static final long MAX_NANOS = 6_000_000L;
    private static final SectionUploadBudget FRAME = new SectionUploadBudget();

    private long lastFrame = Long.MIN_VALUE;

    public static void runFrame(Queue<Runnable> uploads) {
        long budget = FRAME.nextBudget(System.nanoTime());
        drain(uploads, budget, System::nanoTime);
    }

    long nextBudget(long now) {
        long elapsed = lastFrame == Long.MIN_VALUE ? 0L : Math.max(0L, now - lastFrame);
        lastFrame = now;
        return Math.max(MIN_NANOS, Math.min(MAX_NANOS, elapsed / SHARE_DIVISOR));
    }

    static int drain(Queue<Runnable> uploads, long budget, LongSupplier clock) {
        long deadline = clock.getAsLong() + budget;
        int ran = 0;
        Runnable upload;
        while ((ran == 0 || clock.getAsLong() < deadline) && (upload = uploads.poll()) != null) {
            upload.run();
            ran++;
        }
        return ran;
    }
}
