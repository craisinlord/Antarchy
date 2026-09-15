package com.craisinlord.antarchy.content.computer.paint;

import java.util.Objects;

public final class AntPaintRequests {
    private AntPaintRequests() {
    }

    public record Save(String computerId, AntPaintFile file) {
        public Save {
            Objects.requireNonNull(computerId, "computerId");
            Objects.requireNonNull(file, "file");
        }
    }

    public record Load(String computerId, String fileId) {
        public Load {
            Objects.requireNonNull(computerId, "computerId");
            Objects.requireNonNull(fileId, "fileId");
        }
    }

    public record List(String computerId) {
        public List {
            Objects.requireNonNull(computerId, "computerId");
        }
    }

    public record Delete(String computerId, String fileId) {
        public Delete {
            Objects.requireNonNull(computerId, "computerId");
            Objects.requireNonNull(fileId, "fileId");
        }
    }
}
