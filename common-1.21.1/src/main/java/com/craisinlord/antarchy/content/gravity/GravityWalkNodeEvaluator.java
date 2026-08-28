package com.craisinlord.antarchy.content.gravity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GravityWalkNodeEvaluator extends WalkNodeEvaluator {
    private static final int[] START_Y_SEARCH = {0, 1, -1, 2, -2};
    private static final int[][] INVERTED_CARDINAL_NEIGHBOR_OFFSETS = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1}
    };
    private static final int[][] INVERTED_DIAGONAL_NEIGHBOR_OFFSETS = {
            {1, 0, 1},
            {1, 0, -1},
            {-1, 0, 1},
            {-1, 0, -1}
    };

    @Override
    public void prepare(PathNavigationRegion sourceIn, Mob mob) {
        super.prepare(sourceIn, mob);
    }

    @Override
    public void done() {
        super.done();
    }

    @Override
    public Node getStart() {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return super.getStart();
        }

        int x = Mth.floor(this.mob.getX());
        int z = Mth.floor(this.mob.getZ());
        int y = Mth.floor(this.mob.getY() + this.mob.getBbHeight()) - 1;
        Node node = this.antarchy$findStartNode(x, y, z);
        if (node == null) {
            return super.getStart();
        }

        return node;
    }

    private Node antarchy$findStartNode(int x, int y, int z) {
        for (int dy : START_Y_SEARCH) {
            int candidateY = y + dy;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Node node = this.antarchy$createStartNode(x + dx, candidateY, z + dz);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    private Node antarchy$createStartNode(int x, int y, int z) {
        if (!this.antarchy$hasCeilingSupport(x, y, z)) {
            return null;
        }

        PathType type = this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob);
        float malus = this.mob.getPathfindingMalus(type);
        if (malus < 0.0F || type == PathType.OPEN) {
            return null;
        }

        Node node = this.getNode(x, y, z);
        node.type = type;
        node.costMalus = malus;
        return node;
    }

    private boolean antarchy$hasCeilingSupport(int x, int y, int z) {
        PathType ceilingType = this.getPathType(this.currentContext, x, y + 1, z);
        return this.mob.getPathfindingMalus(ceilingType) < 0.0F;
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return super.getPathTypeOfMob(context, x, y, z, mob);
        }

        PathType ceilingType = this.getPathType(context, x, y + 1, z);
        if (mob.getPathfindingMalus(ceilingType) >= 0.0F) {
            return PathType.OPEN;
        }

        PathType result = PathType.WALKABLE;
        for (int dw = 0; dw < this.entityWidth; dw++) {
            for (int dd = 0; dd < this.entityDepth; dd++) {
                for (int dh = 0; dh < this.entityHeight; dh++) {
                    PathType type = this.getPathType(context, x + dw, y - dh, z + dd);
                    float malus = mob.getPathfindingMalus(type);
                    if (malus < 0.0F) {
                        return type;
                    }
                    if (malus >= mob.getPathfindingMalus(result)) {
                        result = type;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node current) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return super.getNeighbors(neighbors, current);
        }

        int count = 0;
        Node[] cardinalNeighbors = new Node[INVERTED_CARDINAL_NEIGHBOR_OFFSETS.length];
        for (int i = 0; i < INVERTED_CARDINAL_NEIGHBOR_OFFSETS.length; i++) {
            int[] offset = INVERTED_CARDINAL_NEIGHBOR_OFFSETS[i];
            Node neighbor = this.antarchy$getNeighbor(current.x + offset[0], current.y, current.z + offset[2]);
            cardinalNeighbors[i] = neighbor;
            if (neighbor != null) {
                neighbors[count++] = neighbor;
            }
        }

        for (int i = 0; i < INVERTED_DIAGONAL_NEIGHBOR_OFFSETS.length; i++) {
            int[] offset = INVERTED_DIAGONAL_NEIGHBOR_OFFSETS[i];
            if (!this.antarchy$canUseDiagonal(cardinalNeighbors, i)) {
                continue;
            }
            Node neighbor = this.antarchy$getNeighbor(current.x + offset[0], current.y, current.z + offset[2]);
            if (neighbor != null) {
                neighbors[count++] = neighbor;
            }
        }

        return count;
    }

    private boolean antarchy$canUseDiagonal(Node[] cardinalNeighbors, int diagonalIndex) {
        return switch (diagonalIndex) {
            case 0 -> cardinalNeighbors[0] != null && cardinalNeighbors[2] != null;
            case 1 -> cardinalNeighbors[0] != null && cardinalNeighbors[3] != null;
            case 2 -> cardinalNeighbors[1] != null && cardinalNeighbors[2] != null;
            case 3 -> cardinalNeighbors[1] != null && cardinalNeighbors[3] != null;
            default -> false;
        };
    }

    private Node antarchy$getNeighbor(int x, int y, int z) {
        PathType type = this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob);
        if (this.mob.getPathfindingMalus(type) < 0.0F) {
            return null;
        }

        Node node = this.getNode(x, y, z);
        node.type = type;
        node.costMalus = this.mob.getPathfindingMalus(type);
        return node;
    }
}
