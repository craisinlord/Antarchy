package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.ComputerAccessPayload;
import com.craisinlord.antarchy.content.network.ComputerAccessResultPayload;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ComputerFileSystemClientState {
    private static final Map<BlockPos, State> STATES = new ConcurrentHashMap<>();

    private ComputerFileSystemClientState() {
    }

    public static void update(ComputerAccessResultPayload payload) {
        if (payload.data().isEmpty()) return;
        String[] envelope = payload.data().split("\u0000", 3);
        if (envelope.length < 3) return;
        int action;
        try {
            action = Integer.parseInt(envelope[0]);
        } catch (NumberFormatException ignored) {
            return;
        }
        State state = STATES.computeIfAbsent(payload.pos(), ignored -> new State());
        state.success = payload.result() == ComputerAccessResultPayload.SUCCESS;
        state.error = envelope[1];
        if (action == ComputerAccessPayload.FILE_LIST && state.success) {
            state.files = new ArrayList<>();
            if (!envelope[2].isEmpty()) state.files.addAll(List.of(envelope[2].split("\\n")));
        } else if (action == ComputerAccessPayload.FILE_OPEN && state.success) {
            String[] file = envelope[2].split("\u0000", 2);
            if (file.length == 2) {
                state.openedPath = file[0];
                state.openedContents = file[1];
            }
        }
    }

    public static State get(BlockPos pos) {
        return STATES.computeIfAbsent(pos, ignored -> new State());
    }

    public static final class State {
        private List<String> files = List.of();
        private String openedPath = "";
        private String openedContents = "";
        private String error = "";
        private boolean success;

        public List<String> files() { return List.copyOf(files); }
        public String openedPath() { return openedPath; }
        public String openedContents() { return openedContents; }
        public String error() { return error; }
        public boolean success() { return success; }
    }
}
