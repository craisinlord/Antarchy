package com.craisinlord.antarchy.content.computer.terminal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TerminalCommandService {
    private static final int MAX_ARGUMENT_LENGTH = 128;
    private static final List<String> COMMANDS = List.of(
            "help", "clear", "pwd", "ls", "cd", "mkdir", "touch", "cat", "write", "rm", "rmdir", "mv", "open"
    );

    public TerminalResult execute(TerminalFileSystem fileSystem, String workingDirectory, String input) {
        String currentDirectory = normalizeWorkingDirectory(fileSystem, workingDirectory);
        TerminalCommandParser.ParseResult parsed = TerminalCommandParser.parse(input);
        if (!parsed.isSuccess()) {
            return TerminalResult.error(currentDirectory, parsed.error());
        }
        TerminalCommand command = parsed.command();
        if (command.arguments().stream().anyMatch(argument -> argument.length() > MAX_ARGUMENT_LENGTH)) {
            return TerminalResult.error(currentDirectory, "ERROR: ARGUMENT TOO LONG");
        }
        return switch (command.name()) {
            case "help" -> help(currentDirectory, command);
            case "clear" -> requireArguments(currentDirectory, command, 0, TerminalResult.clear(currentDirectory));
            case "pwd" -> requireArguments(currentDirectory, command, 0, TerminalResult.success(currentDirectory, currentDirectory));
            case "ls" -> list(fileSystem, currentDirectory, command);
            case "cd" -> changeDirectory(fileSystem, currentDirectory, command);
            case "mkdir" -> createDirectory(fileSystem, currentDirectory, command);
            case "touch" -> createFile(fileSystem, currentDirectory, command);
            case "cat" -> readFile(fileSystem, currentDirectory, command);
            case "write" -> writeFile(fileSystem, currentDirectory, command);
            case "rm" -> removeFile(fileSystem, currentDirectory, command);
            case "rmdir" -> removeDirectory(fileSystem, currentDirectory, command);
            case "mv" -> move(fileSystem, currentDirectory, command);
            case "open" -> open(fileSystem, currentDirectory, command);
            default -> TerminalResult.error(currentDirectory, "ERROR: INVALID COMMAND: " + command.name());
        };
    }

    private TerminalResult help(String workingDirectory, TerminalCommand command) {
        if (command.arguments().isEmpty()) {
            return TerminalResult.success(workingDirectory,
                    "AVAILABLE COMMANDS",
                    "HELP CLEAR PWD LS CD MKDIR TOUCH CAT WRITE RM RMDIR MV OPEN");
        }
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "help [command]");
        }
        return switch (command.arguments().get(0).toLowerCase(Locale.ROOT)) {
            case "help" -> TerminalResult.success(workingDirectory, "HELP [COMMAND] // SHOW COMMAND HELP");
            case "clear" -> TerminalResult.success(workingDirectory, "CLEAR // CLEAR TERMINAL OUTPUT");
            case "pwd" -> TerminalResult.success(workingDirectory, "PWD // PRINT WORKING DIRECTORY");
            case "ls" -> TerminalResult.success(workingDirectory, "LS [PATH] // LIST DIRECTORY CONTENTS");
            case "cd" -> TerminalResult.success(workingDirectory, "CD [PATH] // CHANGE WORKING DIRECTORY");
            case "mkdir" -> TerminalResult.success(workingDirectory, "MKDIR <PATH> // CREATE DIRECTORY");
            case "touch" -> TerminalResult.success(workingDirectory, "TOUCH <PATH> // CREATE EMPTY TEXT FILE");
            case "cat" -> TerminalResult.success(workingDirectory, "CAT <PATH> // PRINT TEXT FILE");
            case "write" -> TerminalResult.success(workingDirectory, "WRITE <PATH> [TEXT] // REPLACE TEXT FILE CONTENTS");
            case "rm" -> TerminalResult.success(workingDirectory, "RM <PATH> // DELETE TEXT FILE");
            case "rmdir" -> TerminalResult.success(workingDirectory, "RMDIR <PATH> // DELETE EMPTY DIRECTORY");
            case "mv" -> TerminalResult.success(workingDirectory, "MV <SOURCE> <DESTINATION> // MOVE OR RENAME");
            case "open" -> TerminalResult.success(workingDirectory, "OPEN <PATH> // OPEN TEXT FILE IN ANTTEXT");
            default -> TerminalResult.error(workingDirectory, "ERROR: NO HELP FOR " + command.arguments().get(0));
        };
    }

    private TerminalResult list(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() > 1) {
            return usage(workingDirectory, "ls [path]");
        }
        String path = command.arguments().isEmpty() ? workingDirectory : command.arguments().get(0);
        Optional<TerminalFileSystem.Entry> entry = find(fileSystem, path, workingDirectory);
        if (entry.isEmpty()) {
            return missing(workingDirectory, path);
        }
        if (!entry.get().isDirectory()) {
            return TerminalResult.success(workingDirectory, entry.get().name());
        }
        List<TerminalFileSystem.Entry> entries = new ArrayList<>(fileSystem.list(path, workingDirectory));
        entries.sort(Comparator.comparing(TerminalFileSystem.Entry::isDirectory).reversed().thenComparing(TerminalFileSystem.Entry::name));
        if (entries.isEmpty()) {
            return TerminalResult.success(workingDirectory, "DIRECTORY EMPTY");
        }
        return new TerminalResult(TerminalResult.Status.SUCCESS, entries.stream()
                .map(value -> (value.isDirectory() ? "[DIR] " : "      ") + value.name()).toList(), workingDirectory, false, null);
    }

    private TerminalResult changeDirectory(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() > 1) {
            return usage(workingDirectory, "cd [path]");
        }
        String path = command.arguments().isEmpty() ? TerminalFileSystem.ROOT_PATH : command.arguments().get(0);
        Optional<TerminalFileSystem.Entry> entry = find(fileSystem, path, workingDirectory);
        if (entry.isEmpty() || !entry.get().isDirectory()) {
            return missing(workingDirectory, path);
        }
        return TerminalResult.success(entry.get().path(), entry.get().path());
    }

    private TerminalResult createDirectory(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "mkdir <path>");
        }
        return fileSystem.createDirectory(command.arguments().get(0), workingDirectory)
                ? TerminalResult.success(workingDirectory)
                : TerminalResult.error(workingDirectory, "ERROR: CANNOT CREATE DIRECTORY");
    }

    private TerminalResult createFile(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "touch <path>");
        }
        return fileSystem.createFile(command.arguments().get(0), workingDirectory)
                ? TerminalResult.success(workingDirectory)
                : TerminalResult.error(workingDirectory, "ERROR: CANNOT CREATE FILE");
    }

    private TerminalResult readFile(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "cat <path>");
        }
        Optional<String> contents = fileSystem.readText(command.arguments().get(0), workingDirectory);
        return contents.map(value -> TerminalResult.success(workingDirectory, value.split("\\R", -1)))
                .orElseGet(() -> missing(workingDirectory, command.arguments().get(0)));
    }

    private TerminalResult writeFile(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() < 1 || command.arguments().size() > 2) {
            return usage(workingDirectory, "write <path> [text]");
        }
        String contents = command.arguments().size() == 2 ? command.arguments().get(1) : "";
        return fileSystem.writeText(command.arguments().get(0), workingDirectory, contents)
                ? TerminalResult.success(workingDirectory)
                : TerminalResult.error(workingDirectory, "ERROR: CANNOT WRITE FILE");
    }

    private TerminalResult removeFile(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "rm <path>");
        }
        return fileSystem.deleteFile(command.arguments().get(0), workingDirectory)
                ? TerminalResult.success(workingDirectory)
                : TerminalResult.error(workingDirectory, "ERROR: CANNOT DELETE FILE");
    }

    private TerminalResult removeDirectory(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "rmdir <path>");
        }
        return fileSystem.deleteEmptyDirectory(command.arguments().get(0), workingDirectory)
                ? TerminalResult.success(workingDirectory)
                : TerminalResult.error(workingDirectory, "ERROR: DIRECTORY NOT EMPTY OR NOT FOUND");
    }

    private TerminalResult move(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 2) {
            return usage(workingDirectory, "mv <source> <destination>");
        }
        return fileSystem.move(command.arguments().get(0), command.arguments().get(1), workingDirectory)
                ? TerminalResult.success(workingDirectory)
                : TerminalResult.error(workingDirectory, "ERROR: CANNOT MOVE PATH");
    }

    private TerminalResult open(TerminalFileSystem fileSystem, String workingDirectory, TerminalCommand command) {
        if (command.arguments().size() != 1) {
            return usage(workingDirectory, "open <path>");
        }
        Optional<TerminalFileSystem.Entry> entry = find(fileSystem, command.arguments().get(0), workingDirectory);
        if (entry.isEmpty() || entry.get().isDirectory()) {
            return missing(workingDirectory, command.arguments().get(0));
        }
        return TerminalResult.open(workingDirectory, entry.get().path());
    }

    private Optional<TerminalFileSystem.Entry> find(TerminalFileSystem fileSystem, String path, String workingDirectory) {
        try {
            return fileSystem.find(path, workingDirectory);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private String normalizeWorkingDirectory(TerminalFileSystem fileSystem, String workingDirectory) {
        try {
            String normalized = fileSystem.normalize(workingDirectory, TerminalFileSystem.ROOT_PATH);
            Optional<TerminalFileSystem.Entry> entry = fileSystem.find(normalized, TerminalFileSystem.ROOT_PATH);
            return entry.filter(TerminalFileSystem.Entry::isDirectory).map(TerminalFileSystem.Entry::path).orElse(TerminalFileSystem.ROOT_PATH);
        } catch (RuntimeException ignored) {
            return TerminalFileSystem.ROOT_PATH;
        }
    }

    private TerminalResult missing(String workingDirectory, String path) {
        return TerminalResult.error(workingDirectory, "ERROR: PATH NOT FOUND: " + path);
    }

    private TerminalResult usage(String workingDirectory, String usage) {
        return TerminalResult.error(workingDirectory, "USAGE: " + usage);
    }

    private TerminalResult requireArguments(String workingDirectory, TerminalCommand command, int count, TerminalResult result) {
        return command.arguments().size() == count ? result : usage(workingDirectory, command.name() + " requires " + count + " arguments");
    }
}
