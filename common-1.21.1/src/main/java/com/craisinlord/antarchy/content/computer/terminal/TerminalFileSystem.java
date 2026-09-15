package com.craisinlord.antarchy.content.computer.terminal;

import java.util.List;
import java.util.Optional;

public interface TerminalFileSystem {
    String ROOT_PATH = "/";

    Optional<Entry> find(String path, String workingDirectory);

    List<Entry> list(String path, String workingDirectory);

    boolean createDirectory(String path, String workingDirectory);

    boolean createFile(String path, String workingDirectory);

    Optional<String> readText(String path, String workingDirectory);

    boolean writeText(String path, String workingDirectory, String contents);

    boolean deleteFile(String path, String workingDirectory);

    boolean deleteEmptyDirectory(String path, String workingDirectory);

    boolean move(String source, String destination, String workingDirectory);

    String normalize(String path, String workingDirectory);

    record Entry(String name, String path, EntryType type) {
        public boolean isDirectory() {
            return type == EntryType.DIRECTORY;
        }
    }

    enum EntryType {
        FILE,
        DIRECTORY
    }
}
