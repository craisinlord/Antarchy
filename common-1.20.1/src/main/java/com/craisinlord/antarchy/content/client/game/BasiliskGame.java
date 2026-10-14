package com.craisinlord.antarchy.content.client.game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public final class BasiliskGame {
    public enum Phase { READY, PLAYING, LOST }
    public enum Direction { UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx;
        final int dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    private final Deque<Part> body = new ArrayDeque<>();
    private final Random random = new Random();
    private Direction direction = Direction.RIGHT;
    private Direction requested = Direction.RIGHT;
    private Phase phase = Phase.READY;
    private int foodX;
    private int foodY;
    private int score;
    private int highScore;
    private int ticks;

    public BasiliskGame(int highScore, int score) {
        this.highScore = Math.max(0, highScore);
        reset(score);
    }

    public void reset() {
        reset(0);
    }

    public void reset(int currentScore) {
        body.clear();
        body.addFirst(new Part(9, 7));
        body.addLast(new Part(8, 7));
        body.addLast(new Part(7, 7));
        direction = Direction.RIGHT;
        requested = Direction.RIGHT;
        phase = Phase.READY;
        score = Math.max(0, currentScore);
        ticks = 0;
        placeFood();
    }

    public void start() { if (phase == Phase.READY) phase = Phase.PLAYING; }

    public boolean tick() {
        if (phase != Phase.PLAYING || ++ticks < 5) return false;
        ticks = 0;
        if (!isOpposite(requested, direction)) direction = requested;
        Part head = body.peekFirst();
        int nextX = head.x() + direction.dx;
        int nextY = head.y() + direction.dy;
        if (nextX < 0 || nextX >= 20 || nextY < 0 || nextY >= 14 || body.contains(new Part(nextX, nextY))) {
            phase = Phase.LOST;
            return true;
        }
        body.addFirst(new Part(nextX, nextY));
        if (nextX == foodX && nextY == foodY) {
            score += 10;
            highScore = Math.max(highScore, score);
            placeFood();
        } else {
            body.removeLast();
        }
        return true;
    }

    public void setDirection(Direction direction) { if (direction != null && !isOpposite(direction, this.direction)) requested = direction; }
    public Phase phase() { return phase; }
    public int score() { return score; }
    public int highScore() { return highScore; }
    public int width() { return 20; }
    public int height() { return 14; }
    public int foodX() { return foodX; }
    public int foodY() { return foodY; }
    public Deque<Part> body() { return body; }

    private void placeFood() {
        do {
            foodX = random.nextInt(width());
            foodY = random.nextInt(height());
        } while (body.contains(new Part(foodX, foodY)));
    }

    private boolean isOpposite(Direction first, Direction second) {
        return first.dx + second.dx == 0 && first.dy + second.dy == 0;
    }

    public record Part(int x, int y) { }
}
