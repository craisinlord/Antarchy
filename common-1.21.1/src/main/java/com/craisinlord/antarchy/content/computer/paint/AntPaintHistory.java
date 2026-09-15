package com.craisinlord.antarchy.content.computer.paint;

import java.util.ArrayDeque;

public final class AntPaintHistory {
    public static final int MAX_STATES = 32;

    private final ArrayDeque<AntPaintCanvas> undo = new ArrayDeque<>();
    private final ArrayDeque<AntPaintCanvas> redo = new ArrayDeque<>();

    public void record(AntPaintCanvas canvas) {
        undo.addLast(canvas.copy());
        while (undo.size() > MAX_STATES) {
            undo.removeFirst();
        }
        redo.clear();
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public AntPaintCanvas undo(AntPaintCanvas current) {
        if (undo.isEmpty()) {
            return current;
        }
        redo.addLast(current.copy());
        return undo.removeLast().copy();
    }

    public AntPaintCanvas redo(AntPaintCanvas current) {
        if (redo.isEmpty()) {
            return current;
        }
        undo.addLast(current.copy());
        return redo.removeLast().copy();
    }

    public void clear() {
        undo.clear();
        redo.clear();
    }
}
