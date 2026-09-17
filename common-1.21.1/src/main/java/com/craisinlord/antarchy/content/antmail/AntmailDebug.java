package com.craisinlord.antarchy.content.antmail;

import com.craisinlord.antarchy.Antarchy;

/** Opt-in diagnostics for AntMail networking and mailbox state transitions. */
public final class AntmailDebug {
    private static final String PROPERTY = "antarchy.antmail.debug";

    private AntmailDebug() {
    }

    public static void log(String message) {
        if (Boolean.getBoolean(PROPERTY)) Antarchy.LOGGER.info("[AntMail debug] {}", message);
    }

    public static void error(String message, Throwable throwable) {
        Antarchy.LOGGER.error("[AntMail] {}", message, throwable);
    }
}
