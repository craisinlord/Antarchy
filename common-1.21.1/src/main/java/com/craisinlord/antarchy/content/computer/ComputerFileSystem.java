package com.craisinlord.antarchy.content.computer;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComputerFileSystem {
    public static final int MAX_FILE_SIZE = 16 * 1024;
    public static final int MAX_TOTAL_SIZE = 64 * 1024;
    public static final int MAX_NODES = 128;
    public static final int MAX_PATH_LENGTH = 64;
    public static final String ROOT = "/";
    public static final String DOCUMENTS = "/documents";
    public static final String PICTURES = "/pictures";
    public static final String SYSTEM = "/system";
    private static final String FILES_TAG = "Files";
    private static final String PATH_TAG = "Path";
    private static final String TYPE_TAG = "Type";
    private static final String CONTENTS_TAG = "Contents";
    private final Map<String, ComputerFile> nodes = new LinkedHashMap<>();
    private int totalBytes;

    public ComputerFileSystem() {
        reset();
    }

    public void reset() {
        nodes.clear();
        nodes.put(ROOT, ComputerFile.directory(ROOT));
        nodes.put(DOCUMENTS, ComputerFile.directory(DOCUMENTS));
        nodes.put(PICTURES, ComputerFile.directory(PICTURES));
        nodes.put(SYSTEM, ComputerFile.directory(SYSTEM));
        totalBytes = 0;
    }

    public ComputerFile get(String path) {
        return nodes.get(normalize(path));
    }

    public List<ComputerFile> list() {
        return List.copyOf(nodes.values());
    }

    public List<ComputerFile> listTextFiles() {
        return nodes.values().stream().filter(file -> file.type() == ComputerFile.Type.TEXT).toList();
    }

    public Result createDirectory(String path) {
        String normalized = normalize(path);
        if (normalized.isEmpty() || ROOT.equals(normalized) || isProtected(normalized) || nodes.containsKey(normalized) || nodes.size() >= MAX_NODES) return Result.failure("invalid_path");
        int slash = normalized.lastIndexOf('/');
        if (slash > 0 && !nodesDirectory(normalized.substring(0, slash))) return Result.failure("missing_directory");
        nodes.put(normalized, ComputerFile.directory(normalized));
        return Result.ok();
    }

    public Result createTextFile(String path, String contents) {
        String normalized = normalize(path);
        Result validation = validateTextPath(normalized, contents);
        if (!validation.successful()) return validation;
        if (nodes.containsKey(normalized)) return Result.failure("already_exists");
        if (nodes.size() >= MAX_NODES) return Result.failure("node_limit");
        int size = contents.length();
        if (totalBytes + size > MAX_TOTAL_SIZE) return Result.failure("storage_limit");
        nodes.put(normalized, ComputerFile.text(normalized, contents));
        totalBytes += size;
        return Result.ok();
    }

    public Result writeTextFile(String path, String contents) {
        String normalized = normalize(path);
        Result validation = validateTextPath(normalized, contents);
        if (!validation.successful()) return validation;
        ComputerFile current = nodes.get(normalized);
        if (current == null) return Result.failure("not_found");
        if (current.type() != ComputerFile.Type.TEXT && current.type() != ComputerFile.Type.IMAGE) return Result.failure("not_file");
        int replacement = totalBytes - current.contents().length() + contents.length();
        if (replacement > MAX_TOTAL_SIZE) return Result.failure("storage_limit");
        nodes.put(normalized, ComputerFile.text(normalized, contents));
        totalBytes = replacement;
        return Result.ok();
    }

    public Result createOrWriteTextFile(String path, String contents) {
        return nodes.containsKey(normalize(path)) ? writeTextFile(path, contents) : createTextFile(path, contents);
    }

    public Result move(String source, String destination) {
        String from = normalize(source);
        String to = normalize(destination);
        if (from.isEmpty() || to.isEmpty() || ROOT.equals(from) || ROOT.equals(to) || isProtected(from) || isProtected(to)) return Result.failure("invalid_path");
        ComputerFile file = nodes.get(from);
        if (file == null) return Result.failure("not_found");
        if (nodes.containsKey(to)) return Result.failure("already_exists");
        int slash = to.lastIndexOf('/');
        if (slash > 0 && !nodesDirectory(to.substring(0, slash))) return Result.failure("missing_directory");
        if (file.type() == ComputerFile.Type.DIRECTORY && to.startsWith(from + "/")) return Result.failure("invalid_path");
        if (file.type() == ComputerFile.Type.DIRECTORY) {
            List<String> descendants = nodes.keySet().stream()
                    .filter(path -> path.startsWith(from + "/"))
                    .toList();
            for (String descendant : descendants) {
                String movedPath = to + descendant.substring(from.length());
                if (nodes.containsKey(movedPath)) return Result.failure("already_exists");
            }
            Map<String, ComputerFile> moved = new LinkedHashMap<>();
            moved.put(to, ComputerFile.directory(to));
            for (String descendant : descendants) {
                ComputerFile child = nodes.get(descendant);
                String movedPath = to + descendant.substring(from.length());
                moved.put(movedPath, child.type() == ComputerFile.Type.DIRECTORY
                        ? ComputerFile.directory(movedPath)
                        : ComputerFile.text(movedPath, child.contents()));
            }
            nodes.keySet().removeIf(path -> path.equals(from) || path.startsWith(from + "/"));
            nodes.putAll(moved);
        } else {
            nodes.remove(from);
            nodes.put(to, ComputerFile.text(to, file.contents()));
        }
        return Result.ok();
    }

    public Result delete(String path) {
        String normalized = normalize(path);
        if (isProtected(normalized)) return Result.failure("protected");
        ComputerFile file = nodes.get(normalized);
        if (file == null) return Result.failure("not_found");
        if (file.type() == ComputerFile.Type.DIRECTORY && nodes.keySet().stream().anyMatch(key -> !key.equals(normalized) && key.startsWith(normalized + "/"))) {
            return Result.failure("not_empty");
        }
        nodes.remove(normalized);
        totalBytes -= file.contents().length();
        return Result.ok();
    }

    public static String normalize(String path) {
        if (path == null || path.isBlank()) return "";
        String value = path.trim().replace('\\', '/');
        if (!value.startsWith("/")) value = "/" + value;
        while (value.contains("//")) value = value.replace("//", "/");
        if (value.length() > 1 && value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
    }

    public static boolean isProtected(String path) {
        String normalized = normalize(path);
        return normalized.equals(SYSTEM) || normalized.startsWith(SYSTEM + "/");
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag files = new ListTag();
        for (ComputerFile file : nodes.values()) {
            if (ROOT.equals(file.path())) continue;
            CompoundTag stored = new CompoundTag();
            stored.putString(PATH_TAG, file.path());
            stored.putString(TYPE_TAG, file.type().name());
            if (file.type() == ComputerFile.Type.TEXT || file.type() == ComputerFile.Type.IMAGE) stored.putString(CONTENTS_TAG, file.contents());
            files.add(stored);
        }
        tag.put(FILES_TAG, files);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        reset();
        ListTag files = tag.getList(FILES_TAG, 10);
        for (int index = 0; index < files.size(); index++) {
            CompoundTag stored = files.getCompound(index);
            String path = normalize(stored.getString(PATH_TAG));
            if (path.isEmpty() || nodes.containsKey(path) || path.length() > MAX_PATH_LENGTH || isProtected(path)) continue;
            String type = stored.getString(TYPE_TAG);
            if ("DIRECTORY".equals(type)) {
                if (nodes.size() < MAX_NODES) nodes.put(path, ComputerFile.directory(path));
            } else if ("TEXT".equals(type) || "IMAGE".equals(type)) {
                String contents = stored.getString(CONTENTS_TAG);
                if (contents.length() <= MAX_FILE_SIZE && nodes.size() < MAX_NODES && totalBytes + contents.length() <= MAX_TOTAL_SIZE) {
                    nodes.put(path, "IMAGE".equals(type) ? ComputerFile.image(path, contents) : ComputerFile.text(path, contents));
                    totalBytes += contents.length();
                }
            }
        }
    }

    private Result validateTextPath(String path, String contents) {
        if (path.isEmpty() || path.length() > MAX_PATH_LENGTH || ROOT.equals(path) || isProtected(path)) return Result.failure("invalid_path");
        if (contents == null || contents.length() > MAX_FILE_SIZE) return Result.failure("file_limit");
        if (path.contains("..") || path.indexOf('\0') >= 0 || path.indexOf('\n') >= 0 || path.indexOf('\r') >= 0) return Result.failure("invalid_path");
        int slash = path.lastIndexOf('/');
        if (slash > 0 && !nodesDirectory(path.substring(0, slash))) return Result.failure("missing_directory");
        return Result.ok();
    }

    private boolean nodesDirectory(String path) {
        ComputerFile parent = nodes.get(path);
        return parent != null && parent.type() == ComputerFile.Type.DIRECTORY;
    }

    public int totalBytes() {
        return totalBytes;
    }

    public record Result(boolean successful, String error) {
        public static Result ok() { return new Result(true, ""); }
        public static Result failure(String error) { return new Result(false, error); }
    }

    public record ComputerFile(String path, Type type, String contents) {
        public static ComputerFile directory(String path) { return new ComputerFile(path, Type.DIRECTORY, ""); }
        public static ComputerFile text(String path, String contents) { return path.endsWith(".antpaint") ? image(path, contents) : new ComputerFile(path, Type.TEXT, contents); }
        public static ComputerFile image(String path, String contents) { return new ComputerFile(path, Type.IMAGE, contents); }
        public enum Type { DIRECTORY, TEXT, IMAGE }
    }
}
