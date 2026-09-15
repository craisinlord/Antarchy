package com.craisinlord.antarchy.content.client.game;

import java.util.ArrayList;
import java.util.List;

public final class AntFarmGame {
    public enum Phase { READY, PLAYING, WON, LOST }
    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), NONE(0, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    private static final String[] MAP = {
            "###################",
            "#.................#",
            "#.###.#####.#####.#",
            "#.................#",
            "#.#####.###.#####.#",
            "#.................#",
            "#.#####.###.#####.#",
            "#.................#",
            "#.#####.###.#####.#",
            "#.................#",
            "###################"
    };
    private static final int TICKS_PER_TILE = 4;
    private static final int PLAYER_START_X = 1;
    private static final int PLAYER_START_Y = 1;
    private static final int ENEMY_START_X = 17;
    private static final int ENEMY_START_Y = 9;

    private final boolean[][] pellets = new boolean[MAP.length][MAP[0].length()];
    private Phase phase = Phase.READY;
    private Direction playerDirection = Direction.NONE;
    private Direction requestedDirection = Direction.NONE;
    private Direction enemyDirection = Direction.LEFT;
    private int playerX;
    private int playerY;
    private int enemyX;
    private int enemyY;
    private int movementTicks;
    private int score;
    private int remainingFood;

    public AntFarmGame() {
        reset();
    }

    public void reset() {
        phase = Phase.READY;
        playerDirection = Direction.NONE;
        requestedDirection = Direction.NONE;
        enemyDirection = Direction.LEFT;
        playerX = PLAYER_START_X;
        playerY = PLAYER_START_Y;
        enemyX = ENEMY_START_X;
        enemyY = ENEMY_START_Y;
        movementTicks = 0;
        score = 0;
        remainingFood = 0;
        for (int y = 0; y < MAP.length; y++) {
            for (int x = 0; x < MAP[y].length(); x++) {
                pellets[y][x] = MAP[y].charAt(x) == '.';
                if (pellets[y][x]) remainingFood++;
            }
        }
    }

    public void start() {
        if (phase == Phase.READY) phase = Phase.PLAYING;
    }

    public void tick() {
        if (phase != Phase.PLAYING) return;
        movementTicks++;
        if (movementTicks < TICKS_PER_TILE) return;
        movementTicks = 0;
        movePlayer();
        if (phase != Phase.PLAYING) return;
        moveEnemy();
        if (playerX == enemyX && playerY == enemyY) phase = Phase.LOST;
    }

    public void setRequestedDirection(Direction direction) {
        if (direction != null && direction != Direction.NONE) requestedDirection = direction;
    }

    public Phase phase() { return phase; }
    public int score() { return score; }
    public int remainingFood() { return remainingFood; }
    public int width() { return MAP[0].length(); }
    public int height() { return MAP.length; }
    public int playerX() { return playerX; }
    public int playerY() { return playerY; }
    public int enemyX() { return enemyX; }
    public int enemyY() { return enemyY; }
    public boolean hasPellet(int x, int y) { return pellets[y][x]; }
    public boolean isWall(int x, int y) { return MAP[y].charAt(x) == '#'; }

    private void movePlayer() {
        if (canMove(playerX, playerY, requestedDirection)) playerDirection = requestedDirection;
        if (canMove(playerX, playerY, playerDirection)) {
            playerX += playerDirection.dx;
            playerY += playerDirection.dy;
        }
        if (pellets[playerY][playerX]) {
            pellets[playerY][playerX] = false;
            remainingFood--;
            score += 10;
            if (remainingFood == 0) phase = Phase.WON;
        }
    }

    private void moveEnemy() {
        List<Direction> directions = legalDirections(enemyX, enemyY);
        if (directions.isEmpty()) return;
        Direction reverse = opposite(enemyDirection);
        if (directions.size() > 1) directions.remove(reverse);
        Direction best = directions.get(0);
        int bestDistance = distanceAfter(best, playerX, playerY, enemyX, enemyY);
        for (Direction direction : directions) {
            int distance = distanceAfter(direction, playerX, playerY, enemyX, enemyY);
            if (distance < bestDistance) {
                best = direction;
                bestDistance = distance;
            }
        }
        enemyDirection = best;
        enemyX += best.dx;
        enemyY += best.dy;
    }

    private int distanceAfter(Direction direction, int targetX, int targetY, int originX, int originY) {
        return Math.abs(originX + direction.dx - targetX) + Math.abs(originY + direction.dy - targetY);
    }

    private List<Direction> legalDirections(int x, int y) {
        List<Direction> result = new ArrayList<>(4);
        for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}) {
            if (canMove(x, y, direction)) result.add(direction);
        }
        return result;
    }

    private boolean canMove(int x, int y, Direction direction) {
        if (direction == Direction.NONE) return false;
        int nextX = x + direction.dx;
        int nextY = y + direction.dy;
        return nextY >= 0 && nextY < MAP.length && nextX >= 0 && nextX < MAP[nextY].length() && !isWall(nextX, nextY);
    }

    private Direction opposite(Direction direction) {
        return switch (direction) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
            case NONE -> Direction.NONE;
        };
    }
}
