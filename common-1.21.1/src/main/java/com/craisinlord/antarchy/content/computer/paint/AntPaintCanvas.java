package com.craisinlord.antarchy.content.computer.paint;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class AntPaintCanvas {
    public static final int WIDTH = 32;
    public static final int HEIGHT = 24;
    public static final int PIXEL_COUNT = WIDTH * HEIGHT;
    public static final int PACKED_LENGTH = (PIXEL_COUNT + 7) / 8;

    private final boolean[] pixels;

    public AntPaintCanvas() {
        this.pixels = new boolean[PIXEL_COUNT];
    }

    private AntPaintCanvas(boolean[] pixels) {
        this.pixels = pixels;
    }

    public boolean get(int x, int y) {
        return pixels[index(x, y)];
    }

    public void set(int x, int y, boolean value) {
        pixels[index(x, y)] = value;
    }

    public boolean draw(int x, int y) {
        return setIfChanged(x, y, true);
    }

    public boolean erase(int x, int y) {
        return setIfChanged(x, y, false);
    }

    public void clear() {
        Arrays.fill(pixels, false);
    }

    public int floodFill(int x, int y, boolean value) {
        int start = index(x, y);
        boolean original = pixels[start];
        if (original == value) {
            return 0;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        boolean[] visited = new boolean[PIXEL_COUNT];
        queue.add(start);
        int changed = 0;
        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            if (visited[current] || pixels[current] != original) {
                continue;
            }
            visited[current] = true;
            pixels[current] = value;
            changed++;
            int px = current % WIDTH;
            int py = current / WIDTH;
            if (px > 0) queue.add(current - 1);
            if (px + 1 < WIDTH) queue.add(current + 1);
            if (py > 0) queue.add(current - WIDTH);
            if (py + 1 < HEIGHT) queue.add(current + WIDTH);
        }
        return changed;
    }

    public byte[] pack() {
        byte[] packed = new byte[PACKED_LENGTH];
        for (int i = 0; i < PIXEL_COUNT; i++) {
            if (pixels[i]) {
                packed[i / 8] |= (byte) (1 << (i % 8));
            }
        }
        return packed;
    }

    public static AntPaintCanvas unpack(byte[] packed) {
        if (packed == null || packed.length != PACKED_LENGTH) {
            throw new IllegalArgumentException("Invalid AntPaint pixel data length");
        }
        boolean[] pixels = new boolean[PIXEL_COUNT];
        for (int i = 0; i < PIXEL_COUNT; i++) {
            pixels[i] = (packed[i / 8] & (1 << (i % 8))) != 0;
        }
        return new AntPaintCanvas(pixels);
    }

    public AntPaintCanvas copy() {
        return new AntPaintCanvas(Arrays.copyOf(pixels, pixels.length));
    }

    public boolean[] copyPixels() {
        return Arrays.copyOf(pixels, pixels.length);
    }

    private boolean setIfChanged(int x, int y, boolean value) {
        int index = index(x, y);
        if (pixels[index] == value) {
            return false;
        }
        pixels[index] = value;
        return true;
    }

    private static int index(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            throw new IndexOutOfBoundsException("AntPaint pixel outside canvas");
        }
        return y * WIDTH + x;
    }
}
