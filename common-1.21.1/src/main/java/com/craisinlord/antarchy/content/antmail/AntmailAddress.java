package com.craisinlord.antarchy.content.antmail;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record AntmailAddress(String username) {
    public static final String DOMAIN = "antmail.com";
    private static final Pattern USERNAME = Pattern.compile("[a-z0-9_]{3,16}");

    public AntmailAddress {
        username = normalizeUsername(username);
    }

    public static AntmailAddress parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Address is null");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        String suffix = "@" + DOMAIN;
        if (!normalized.endsWith(suffix)) {
            throw new IllegalArgumentException("Invalid Antmail domain");
        }
        return new AntmailAddress(normalized.substring(0, normalized.length() - suffix.length()));
    }

    public static AntmailAddress ofUsername(String username) {
        return new AntmailAddress(username);
    }

    public String fullAddress() {
        return username + "@" + DOMAIN;
    }

    public static String normalizeUsername(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Username is null");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Username must match [a-z0-9_]{3,16}");
        }
        return normalized;
    }

    public static boolean isValidUsername(String value) {
        if (value == null) {
            return false;
        }
        return USERNAME.matcher(value.trim().toLowerCase(Locale.ROOT)).matches();
    }

    public static boolean isValidAddress(String value) {
        try {
            parse(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @Override
    public String toString() {
        return fullAddress();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AntmailAddress other && username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
