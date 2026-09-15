package com.craisinlord.antarchy.content.computer.terminal;

import java.util.ArrayList;
import java.util.List;

public final class TerminalCommandParser {
    private static final int MAX_INPUT_LENGTH = 256;

    private TerminalCommandParser() {
    }

    public static ParseResult parse(String input) {
        if (input == null) {
            return ParseResult.error("ERROR: EMPTY INPUT");
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            return ParseResult.error("ERROR: COMMAND TOO LONG");
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return ParseResult.error("ERROR: EMPTY INPUT");
        }
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        Character quote = null;
        boolean escaped = false;
        for (int index = 0; index < trimmed.length(); index++) {
            char character = trimmed.charAt(index);
            if (escaped) {
                token.append(character);
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (quote != null) {
                if (character == quote) {
                    quote = null;
                } else {
                    token.append(character);
                }
            } else if (character == '\'' || character == '"') {
                quote = character;
            } else if (Character.isWhitespace(character)) {
                if (!token.isEmpty()) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(character);
            }
        }
        if (escaped) {
            token.append('\\');
        }
        if (quote != null) {
            return ParseResult.error("ERROR: UNTERMINATED QUOTE");
        }
        if (!token.isEmpty()) {
            tokens.add(token.toString());
        }
        if (tokens.isEmpty()) {
            return ParseResult.error("ERROR: EMPTY INPUT");
        }
        return ParseResult.success(new TerminalCommand(tokens.get(0), tokens.subList(1, tokens.size()), input));
    }

    public record ParseResult(TerminalCommand command, String error) {
        public boolean isSuccess() {
            return command != null;
        }

        public static ParseResult success(TerminalCommand command) {
            return new ParseResult(command, null);
        }

        public static ParseResult error(String message) {
            return new ParseResult(null, message);
        }
    }
}
