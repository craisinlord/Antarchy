package com.craisinlord.antarchy.content.computer.terminal;

import java.util.List;

public record TerminalResult(Status status, List<String> lines, String workingDirectory, boolean clearOutput, String openPath) {
    public TerminalResult {
        status = status == null ? Status.ERROR : status;
        lines = List.copyOf(lines);
        workingDirectory = workingDirectory == null || workingDirectory.isBlank() ? TerminalFileSystem.ROOT_PATH : workingDirectory;
    }

    public static TerminalResult success(String workingDirectory, String... lines) {
        return new TerminalResult(Status.SUCCESS, List.of(lines), workingDirectory, false, null);
    }

    public static TerminalResult error(String workingDirectory, String... lines) {
        return new TerminalResult(Status.ERROR, List.of(lines), workingDirectory, false, null);
    }

    public static TerminalResult clear(String workingDirectory) {
        return new TerminalResult(Status.SUCCESS, List.of(), workingDirectory, true, null);
    }

    public static TerminalResult open(String workingDirectory, String path) {
        return new TerminalResult(Status.OPEN, List.of("OPENING " + path), workingDirectory, false, path);
    }

    public enum Status {
        SUCCESS,
        ERROR,
        OPEN
    }
}
