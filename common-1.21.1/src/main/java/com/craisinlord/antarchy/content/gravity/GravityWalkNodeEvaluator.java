package com.craisinlord.antarchy.content.gravity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GravityWalkNodeEvaluator extends WalkNodeEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Antarchy/Pathfinding");
    private static final int[] START_Y_SEARCH = {0, 1, -1, 2, -2, 3, -3, 4, -4};
    private static final int[][] INVERTED_NEIGHBOR_OFFSETS = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1},
            {1, 0, 1},
            {1, 0, -1},
            {-1, 0, 1},
            {-1, 0, -1},
            {0, 1, 0},
            {0, -1, 0}
    };

    private int antarchy$loggedPrepare;
    private int antarchy$loggedGetStart;
    private int antarchy$loggedPathType;
    private int antarchy$loggedNeighbors;

    @Override
    public void prepare(PathNavigationRegion sourceIn, Mob mob) {
        super.prepare(sourceIn, mob);
        this.antarchy$loggedPrepare = 0;
        this.antarchy$loggedGetStart = 0;
        this.antarchy$loggedPathType = 0;
        this.antarchy$loggedNeighbors = 0;

        if (AntarchyGravityApi.isGravityInverted(mob)) {
            LOGGER.debug(
                    "[Path] prepare inverted mob={} pos=({}, {}, {}) size=({}, {})",
                    mob.getClass().getSimpleName(),
                    Mth.floor(mob.getX()),
                    Mth.floor(mob.getY()),
                    Mth.floor(mob.getZ()),
                    mob.getBbWidth(),
                    mob.getBbHeight()
            );
        }
    }

    @Override
    public void done() {
        if (this.mob != null && AntarchyGravityApi.isGravityInverted(this.mob)) {
            LOGGER.debug("[Path] done inverted mob={}", this.mob.getClass().getSimpleName());
        }
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

        if (this.antarchy$loggedGetStart < 8) {
            LOGGER.debug(
                    "[Path] getStart inverted mob={} node=({}, {}, {}) type={} malus={}",
                    this.mob.getClass().getSimpleName(),
                    x,
                    y,
                    z,
                    node.type,
                    node.costMalus
            );
            this.antarchy$loggedGetStart++;
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

        if (this.antarchy$loggedPathType < 16) {
            LOGGER.debug(
                    "[Path] type inverted mob={} pos=({}, {}, {}) ceiling={} result={}",
                    mob.getClass().getSimpleName(),
                    x,
                    y,
                    z,
                    ceilingType,
                    result
            );
            this.antarchy$loggedPathType++;
        }

        return result;
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node current) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return super.getNeighbors(neighbors, current);
        }

        int count = 0;
        for (int[] offset : INVERTED_NEIGHBOR_OFFSETS) {
            count += this.antarchy$addNeighbor(neighbors, count, current.x + offset[0], current.y + offset[1], current.z + offset[2]);
        }

        if (this.antarchy$loggedNeighbors < 16) {
            LOGGER.debug(
                    "[Path] neighbors inverted mob={} node=({}, {}, {}) count={}",
                    this.mob.getClass().getSimpleName(),
                    current.x,
                    current.y,
                    current.z,
                    count
            );
            this.antarchy$loggedNeighbors++;
        }

        return count;
    }

    private int antarchy$addNeighbor(Node[] neighbors, int index, int x, int y, int z) {
        PathType type = this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob);
        if (this.mob.getPathfindingMalus(type) < 0.0F) {
            if (this.antarchy$loggedNeighbors < 16) {
                LOGGER.debug(
                        "[Path] reject mob={} pos=({}, {}, {}) type={} malus={}",
                        this.mob.getClass().getSimpleName(),
                        x,
                        y,
                        z,
                        type,
                        this.mob.getPathfindingMalus(type)
                );
            }
            return 0;
        }

        Node node = this.getNode(x, y, z);
        node.type = type;
        node.costMalus = this.mob.getPathfindingMalus(type);
        neighbors[index] = node;
        return 1;
    }
}
