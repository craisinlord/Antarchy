package com.craisinlord.antarchy.content.computer.terminal;

import java.util.List;

public record TerminalCommand(String name, List<String> arguments, String rawInput) {
    public TerminalCommand {
        name = name.toLowerCase(java.util.Locale.ROOT);
        arguments = List.copyOf(arguments);
        rawInput = rawInput == null ? "" : rawInput;
    }
}
