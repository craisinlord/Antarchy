package com.craisinlord.antarchy.content.client.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class AntmanGame {
    public enum Phase { READY, PLAYING, LOST }
    public enum Direction { UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx;
        final int dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
        Direction opposite() { return switch (this) { case UP -> DOWN; case DOWN -> UP; case LEFT -> RIGHT; case RIGHT -> LEFT; }; }
    }
    public enum EnemyKind { CREEPER, ZOMBIE }
    public record Enemy(EnemyKind kind, double x, double y, Direction direction) { }

    private static final int WIDTH = 28;
    private static final int HEIGHT = 20;
    private static final double SPEED = 0.075D;
    private final Random random = new Random();
    private final boolean[][] walls = new boolean[HEIGHT][WIDTH];
    private final List<Enemy> enemies = new ArrayList<>();
    private Direction direction = Direction.RIGHT;
    private Direction requested = Direction.RIGHT;
    private Phase phase = Phase.READY;
    private double playerX;
    private double playerY;
    private int diamondX;
    private int diamondY;
    private int cornDogX;
    private int cornDogY;
    private int cornDogTicks;
    private int cornDogSpawnTicks;
    private int score;
    private int highScore;
    private int lives = 3;
    private int ticks;
    private boolean collectedDiamond;

    public AntmanGame(int highScore, int score) {
        this.highScore = Math.max(0, highScore);
        reset(score);
    }

    public void reset() { reset(0); }

    public void reset(int currentScore) {
        generateMaze();
        playerX = 1.5D;
        playerY = 1.5D;
        direction = Direction.RIGHT;
        requested = Direction.RIGHT;
        phase = Phase.READY;
        score = Math.max(0, currentScore);
        lives = 3;
        ticks = 0;
        cornDogTicks = 0;
        cornDogSpawnTicks = 0;
        placeDiamond();
        cornDogX = -1;
        cornDogY = -1;
        enemies.clear();
        enemies.add(new Enemy(EnemyKind.CREEPER, WIDTH - 2.5D, HEIGHT - 2.5D, Direction.LEFT));
        enemies.add(new Enemy(EnemyKind.ZOMBIE, WIDTH - 2.5D, 1.5D, Direction.DOWN));
    }

    public void start() { if (phase == Phase.READY) phase = Phase.PLAYING; }

    public boolean tick() {
        collectedDiamond = false;
        if (phase != Phase.PLAYING) return false;
        ticks++;
        if (cornDogTicks > 0) cornDogTicks--;
        if (cornDogX < 0 && ++cornDogSpawnTicks >= 240) {
            placeCornDog();
            cornDogSpawnTicks = 0;
        }
        updatePlayer();
        updateEnemies();
        if (near(playerX, playerY, diamondX + 0.5D, diamondY + 0.5D)) {
            score += 10;
            highScore = Math.max(highScore, score);
            collectedDiamond = true;
            placeDiamond();
        }
        if (cornDogX >= 0 && near(playerX, playerY, cornDogX + 0.5D, cornDogY + 0.5D)) {
            cornDogTicks = 200;
            cornDogX = -1;
            cornDogY = -1;
            cornDogSpawnTicks = 0;
        }
        for (int index = 0; index < enemies.size(); index++) {
            Enemy enemy = enemies.get(index);
            if (!near(playerX, playerY, enemy.x(), enemy.y())) continue;
            if (cornDogTicks > 0) {
                score += 100;
                highScore = Math.max(highScore, score);
                enemies.set(index, new Enemy(enemy.kind(), WIDTH - 2.5D, enemy.kind() == EnemyKind.CREEPER ? HEIGHT - 2.5D : 1.5D, Direction.LEFT));
            } else {
                lives--;
                if (lives <= 0) phase = Phase.LOST;
                else {
                    playerX = 1.5D;
                    playerY = 1.5D;
                    direction = Direction.RIGHT;
                    requested = Direction.RIGHT;
                }
            }
        }
        return true;
    }

    public void setDirection(Direction direction) { if (direction != null) requested = direction; }
    public Phase phase() { return phase; }
    public int score() { return score; }
    public int highScore() { return highScore; }
    public int lives() { return lives; }
    public int width() { return WIDTH; }
    public int height() { return HEIGHT; }
    public double playerX() { return playerX; }
    public double playerY() { return playerY; }
    public int diamondX() { return diamondX; }
    public int diamondY() { return diamondY; }
    public int cornDogX() { return cornDogX; }
    public int cornDogY() { return cornDogY; }
    public int cornDogTicks() { return cornDogTicks; }
    public int animationFrame() { return (ticks / 5) % 2; }
    public Direction direction() { return direction; }
    public List<Enemy> enemies() { return Collections.unmodifiableList(enemies); }
    public boolean collectedDiamond() { return collectedDiamond; }
    public boolean isWall(int x, int y) { return x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT || walls[y][x]; }

    private void updatePlayer() {
        if (atCenter(playerX) && atCenter(playerY) && canMove(playerX, playerY, requested)) {
            playerX = Math.round(playerX - 0.5D) + 0.5D;
            playerY = Math.round(playerY - 0.5D) + 0.5D;
            direction = requested;
        }
        double nextX = playerX + direction.dx * SPEED;
        double nextY = playerY + direction.dy * SPEED;
        if (canMove(nextX, nextY, direction)) { playerX = nextX; playerY = nextY; }
        else { playerX = Math.round(playerX - 0.5D) + 0.5D; playerY = Math.round(playerY - 0.5D) + 0.5D; }
    }

    private void updateEnemies() {
        for (int index = 0; index < enemies.size(); index++) {
            Enemy enemy = enemies.get(index);
            Direction next = enemy.direction();
            if (atCenter(enemy.x()) && atCenter(enemy.y())) next = enemy.kind() == EnemyKind.CREEPER ? chaseDirection(enemy.x(), enemy.y(), next) : zombieDirection(enemy.x(), enemy.y(), next);
            double nextX = enemy.x() + next.dx * (enemy.kind() == EnemyKind.CREEPER ? 0.06D : 0.05D);
            double nextY = enemy.y() + next.dy * (enemy.kind() == EnemyKind.CREEPER ? 0.06D : 0.05D);
            if (!canMove(nextX, nextY, next)) { next = randomDirection(enemy.x(), enemy.y(), next); nextX = enemy.x() + next.dx * 0.05D; nextY = enemy.y() + next.dy * 0.05D; }
            if (canMove(nextX, nextY, next)) enemies.set(index, new Enemy(enemy.kind(), nextX, nextY, next));
        }
    }

    private Direction chaseDirection(double x, double y, Direction current) {
        Direction best = current;
        double distance = Double.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            if (isOpposite(direction, current) || !canMove(x, y, direction)) continue;
            double nx = x + direction.dx;
            double ny = y + direction.dy;
            double value = Math.abs(playerX - nx) + Math.abs(playerY - ny);
            if (value < distance) { distance = value; best = direction; }
        }
        if (distance == Double.MAX_VALUE && canMove(x, y, current.opposite())) best = current.opposite();
        return best;
    }

    private Direction zombieDirection(double x, double y, Direction current) {
        if (random.nextInt(4) != 0 && canMove(x, y, current)) return current;
        return randomDirection(x, y, current);
    }

    private Direction randomDirection(double x, double y, Direction current) {
        List<Direction> options = new ArrayList<>();
        for (Direction direction : Direction.values()) if (!isOpposite(direction, current) && canMove(x, y, direction)) options.add(direction);
        if (options.isEmpty() && canMove(x, y, current.opposite())) return current.opposite();
        if (options.isEmpty()) return current;
        return options.get(random.nextInt(options.size()));
    }

    private boolean canMove(double x, double y, Direction direction) {
        double nx = x + direction.dx * SPEED;
        double ny = y + direction.dy * SPEED;
        return !isWall((int) Math.floor(nx), (int) Math.floor(ny));
    }

    private boolean atCenter(double value) { return Math.abs((value - 0.5D) - Math.rint(value - 0.5D)) < 0.07D; }
    private boolean near(double firstX, double firstY, double secondX, double secondY) { return Math.abs(firstX - secondX) < 0.45D && Math.abs(firstY - secondY) < 0.45D; }
    private boolean isOpposite(Direction first, Direction second) { return first.dx + second.dx == 0 && first.dy + second.dy == 0; }

    private void generateMaze() {
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) walls[y][x] = true;
        carve(1, 1);
    }

    private void carve(int x, int y) {
        walls[y][x] = false;
        List<Direction> directions = new ArrayList<>(List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
        Collections.shuffle(directions, random);
        for (Direction direction : directions) {
            int nx = x + direction.dx * 2;
            int ny = y + direction.dy * 2;
            if (nx > 0 && ny > 0 && nx < WIDTH - 1 && ny < HEIGHT - 1 && walls[ny][nx]) {
                walls[y + direction.dy][x + direction.dx] = false;
                carve(nx, ny);
            }
        }
    }

    private void placeDiamond() {
        int[] cell = randomOpenCell();
        diamondX = cell[0];
        diamondY = cell[1];
    }

    private void placeCornDog() {
        int[] cell = randomOpenCell();
        cornDogX = cell[0];
        cornDogY = cell[1];
    }

    private int[] randomOpenCell() {
        int x;
        int y;
        do { x = random.nextInt(WIDTH); y = random.nextInt(HEIGHT); } while (isWall(x, y) || near(playerX, playerY, x + 0.5D, y + 0.5D));
        return new int[]{x, y};
    }
}
