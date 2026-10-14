package com.craisinlord.antarchy.neoforge;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class InfinityBlockEntityScanGuard {
    private InfinityBlockEntityScanGuard() {
    }

    public static <T> void forEach(Set<T> entries, Consumer<? super T> scan,
                                   BiConsumer<? super T, LinkageError> reportFailure) {
        entries.forEach(entry -> {
            try {
                scan.accept(entry);
            } catch (LinkageError error) {
                reportFailure.accept(entry, error);
            }
        });
    }
}
