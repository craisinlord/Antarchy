package com.craisinlord.antarchy.content.computer.paint;

import java.util.List;

public final class AntPaintResults {
    private AntPaintResults() {
    }

    public enum Code {
        SUCCESS,
        UNAUTHENTICATED,
        OUT_OF_RANGE,
        INVALID_FILENAME,
        INVALID_FILE,
        FILE_NOT_FOUND,
        DUPLICATE_FILE,
        STORAGE_FULL,
        PAYLOAD_TOO_LARGE,
        BUSY,
        UNKNOWN_ERROR
    }

    public record Save(Code code, String fileId) {
    }

    public record Load(Code code, AntPaintFile file) {
    }

    public record List(Code code, java.util.List<AntPaintFileMetadata> files) {
        public List {
            files = files == null ? java.util.List.of() : java.util.List.copyOf(files);
        }
    }

    public record Delete(Code code, String fileId) {
    }

    public record AntPaintFileMetadata(String fileId, String filename, long modifiedTime) {
    }
}
