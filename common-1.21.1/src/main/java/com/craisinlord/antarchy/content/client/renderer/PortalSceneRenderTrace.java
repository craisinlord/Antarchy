package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import java.util.HashSet;
import java.util.Set;

public final class PortalSceneRenderTrace {
    private static final String DEBUG_PROPERTY = "antarchy.portalGun.debugRender";
    private static final long SLOW_PHASE_NANOS = 100_000_000L;
    private static final Set<String> LOGGED_PHASES = new HashSet<>();
    private static long generation;
    private static String armedKey = "";

    private PortalSceneRenderTrace() {
    }

    public static synchronized void arm(String key) {
        if (!armedKey.equals(key)) {
            armedKey = key;
            generation++;
            LOGGED_PHASES.clear();
        }
    }

    public static synchronized void reset() {
        armedKey = "";
        LOGGED_PHASES.clear();
        generation++;
    }

    public static Phase begin(String pair, String phase, String detail) {
        boolean verbose = Boolean.getBoolean(DEBUG_PROPERTY);
        long currentGeneration;
        boolean logStart;
        synchronized (PortalSceneRenderTrace.class) {
            currentGeneration = generation;
            logStart = LOGGED_PHASES.add(currentGeneration + ":" + pair + ":" + phase);
        }
        if (logStart) {
            Antarchy.LOGGER.info("Portal scene trace begin generation={} pair={} phase={} detail={}", currentGeneration, pair, phase, detail);
        }
        return new Phase(currentGeneration, pair, phase, logStart, verbose, System.nanoTime());
    }

    public static void event(String pair, String event, String detail) {
        if (!Boolean.getBoolean(DEBUG_PROPERTY)) {
            return;
        }
        long currentGeneration;
        boolean shouldLog;
        synchronized (PortalSceneRenderTrace.class) {
            currentGeneration = generation;
            shouldLog = LOGGED_PHASES.add(currentGeneration + ":" + pair + ":event:" + event);
        }
        if (shouldLog) {
            Antarchy.LOGGER.info("Portal scene trace event generation={} pair={} event={} detail={}", currentGeneration, pair, event, detail);
        }
    }

    public static final class Phase implements AutoCloseable {
        private final long generation;
        private final String pair;
        private final String phase;
        private final boolean logStart;
        private final boolean verbose;
        private final long startNanos;
        private boolean closed;

        private Phase(long generation, String pair, String phase, boolean logStart, boolean verbose, long startNanos) {
            this.generation = generation;
            this.pair = pair;
            this.phase = phase;
            this.logStart = logStart;
            this.verbose = verbose;
            this.startNanos = startNanos;
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            long durationNanos = System.nanoTime() - this.startNanos;
            if (this.logStart && (this.verbose || durationNanos >= SLOW_PHASE_NANOS)) {
                Antarchy.LOGGER.info("Portal scene trace complete generation={} pair={} phase={} durationMs={}",
                        this.generation, this.pair, this.phase, durationNanos / 1_000_000.0D);
            }
        }
    }
}
