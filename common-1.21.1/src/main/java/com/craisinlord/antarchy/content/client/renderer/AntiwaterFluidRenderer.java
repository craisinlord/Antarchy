package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class AntiwaterFluidRenderer {
    private static final float SURFACE_NUDGE = 0.001F;

    private AntiwaterFluidRenderer() {
    }

    public static void render(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer buffer,
            BlockState blockState,
            FluidState fluidState,
            TextureAtlasSprite stillSprite,
            TextureAtlasSprite flowSprite,
            TextureAtlasSprite overlaySprite,
            int tint
    ) {
        float alpha = (tint >> 24 & 255) / 255.0F;
        float red = (tint >> 16 & 255) / 255.0F;
        float green = (tint >> 8 & 255) / 255.0F;
        float blue = (tint & 255) / 255.0F;

        BlockState downState = level.getBlockState(pos.below());
        FluidState downFluid = downState.getFluidState();
        BlockState upState = level.getBlockState(pos.above());
        BlockState northState = level.getBlockState(pos.north());
        FluidState northFluid = northState.getFluidState();
        BlockState southState = level.getBlockState(pos.south());
        FluidState southFluid = southState.getFluidState();
        BlockState westState = level.getBlockState(pos.west());
        FluidState westFluid = westState.getFluidState();
        BlockState eastState = level.getBlockState(pos.east());
        FluidState eastFluid = eastState.getFluidState();

        boolean renderBottomSurface = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.DOWN, downFluid);
        boolean renderNorth = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.NORTH, northFluid);
        boolean renderSouth = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.SOUTH, southFluid);
        boolean renderWest = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.WEST, westFluid);
        boolean renderEast = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.EAST, eastFluid);
        boolean renderAttachedTop = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.UP, upState.getFluidState());

        if (!renderBottomSurface && !renderNorth && !renderSouth && !renderWest && !renderEast && !renderAttachedTop) {
            return;
        }

        float shadeDown = level.getShade(Direction.DOWN, true);
        float shadeUp = level.getShade(Direction.UP, true);
        float shadeNorth = level.getShade(Direction.NORTH, true);
        float shadeWest = level.getShade(Direction.WEST, true);
        Fluid fluid = fluidState.getType();
        float centerHeight = getHangingHeight(level, fluid, pos, blockState, fluidState);
        float neHeight;
        float nwHeight;
        float seHeight;
        float swHeight;
        if (centerHeight >= 1.0F) {
            neHeight = 1.0F;
            nwHeight = 1.0F;
            seHeight = 1.0F;
            swHeight = 1.0F;
        } else {
            float northHeight = getHangingHeight(level, fluid, pos.north(), northState, northFluid);
            float southHeight = getHangingHeight(level, fluid, pos.south(), southState, southFluid);
            float eastHeight = getHangingHeight(level, fluid, pos.east(), eastState, eastFluid);
            float westHeight = getHangingHeight(level, fluid, pos.west(), westState, westFluid);
            neHeight = calculateAverageHeight(level, fluid, centerHeight, northHeight, eastHeight, pos.north().east());
            nwHeight = calculateAverageHeight(level, fluid, centerHeight, northHeight, westHeight, pos.north().west());
            seHeight = calculateAverageHeight(level, fluid, centerHeight, southHeight, eastHeight, pos.south().east());
            swHeight = calculateAverageHeight(level, fluid, centerHeight, southHeight, westHeight, pos.south().west());
        }

        float neBottom = 1.0F - neHeight;
        float nwBottom = 1.0F - nwHeight;
        float seBottom = 1.0F - seHeight;
        float swBottom = 1.0F - swHeight;

        float x = pos.getX() & 15;
        float y = pos.getY() & 15;
        float z = pos.getZ() & 15;
        float bottomOffset = renderBottomSurface ? SURFACE_NUDGE : 0.0F;

        if (renderBottomSurface) {
            renderBottomFace(level, pos, buffer, fluidState, flowSprite, stillSprite, alpha, red, green, blue, x, y, z, nwBottom, swBottom, seBottom, neBottom);
        }

        if (renderAttachedTop) {
            float u0 = stillSprite.getU0();
            float u1 = stillSprite.getU1();
            float v0 = stillSprite.getV0();
            float v1 = stillSprite.getV1();
            int light = getLightColor(level, pos.above());
            float r = shadeUp * red;
            float g = shadeUp * green;
            float b = shadeUp * blue;
            emitDoubleSidedQuad(
                    buffer,
                    x, y + 1.0F - SURFACE_NUDGE, z + 1.0F, u0, v1,
                    x, y + 1.0F - SURFACE_NUDGE, z, u0, v0,
                    x + 1.0F, y + 1.0F - SURFACE_NUDGE, z, u1, v0,
                    x + 1.0F, y + 1.0F - SURFACE_NUDGE, z + 1.0F, u1, v1,
                    r, g, b, alpha, light
            );
        }

        int sideLight = getLightColor(level, pos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            float leftBottom;
            float rightBottom;
            float x0;
            float x1;
            float z0;
            float z1;
            boolean renderSide;
            switch (direction) {
                case NORTH -> {
                    leftBottom = nwBottom;
                    rightBottom = neBottom;
                    x0 = x;
                    x1 = x + 1.0F;
                    z0 = z + SURFACE_NUDGE;
                    z1 = z + SURFACE_NUDGE;
                    renderSide = renderNorth;
                }
                case SOUTH -> {
                    leftBottom = seBottom;
                    rightBottom = swBottom;
                    x0 = x + 1.0F;
                    x1 = x;
                    z0 = z + 1.0F - SURFACE_NUDGE;
                    z1 = z + 1.0F - SURFACE_NUDGE;
                    renderSide = renderSouth;
                }
                case WEST -> {
                    leftBottom = swBottom;
                    rightBottom = nwBottom;
                    x0 = x + SURFACE_NUDGE;
                    x1 = x + SURFACE_NUDGE;
                    z0 = z + 1.0F;
                    z1 = z;
                    renderSide = renderWest;
                }
                default -> {
                    leftBottom = neBottom;
                    rightBottom = seBottom;
                    x0 = x + 1.0F - SURFACE_NUDGE;
                    x1 = x + 1.0F - SURFACE_NUDGE;
                    z0 = z;
                    z1 = z + 1.0F;
                    renderSide = renderEast;
                }
            }

            if (!renderSide) {
                continue;
            }

            TextureAtlasSprite sideSprite = flowSprite;

            float u0 = sideSprite.getU(0.0F);
            float u1 = sideSprite.getU(0.5F);
            float lowerVLeft = sideSprite.getV(0.5F);
            float lowerVRight = sideSprite.getV(0.5F);
            float upperVLeft = sideSprite.getV(leftBottom * 0.5F);
            float upperVRight = sideSprite.getV(rightBottom * 0.5F);
            float sideShade = direction.getAxis() == Direction.Axis.Z ? shadeNorth : shadeWest;
            float r = shadeUp * sideShade * red;
            float g = shadeUp * sideShade * green;
            float b = shadeUp * sideShade * blue;

            vertex(buffer, x0, y + leftBottom, z0, r, g, b, alpha, u0, upperVLeft, sideLight);
            vertex(buffer, x1, y + rightBottom, z1, r, g, b, alpha, u1, upperVRight, sideLight);
            vertex(buffer, x1, y + 1.0F - bottomOffset, z1, r, g, b, alpha, u1, lowerVRight, sideLight);
            vertex(buffer, x0, y + 1.0F - bottomOffset, z0, r, g, b, alpha, u0, lowerVLeft, sideLight);

            if (sideSprite != overlaySprite) {
                vertex(buffer, x0, y + 1.0F - bottomOffset, z0, r, g, b, alpha, u0, lowerVLeft, sideLight);
                vertex(buffer, x1, y + 1.0F - bottomOffset, z1, r, g, b, alpha, u1, lowerVRight, sideLight);
                vertex(buffer, x1, y + rightBottom, z1, r, g, b, alpha, u1, upperVRight, sideLight);
                vertex(buffer, x0, y + leftBottom, z0, r, g, b, alpha, u0, upperVLeft, sideLight);
            }
        }
    }

    private static void renderBottomFace(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer buffer,
            FluidState fluidState,
            TextureAtlasSprite flowSprite,
            TextureAtlasSprite stillSprite,
            float alpha,
            float red,
            float green,
            float blue,
            float x,
            float y,
            float z,
            float nwBottom,
            float swBottom,
            float seBottom,
            float neBottom
    ) {
        Vec3 flow = fluidState.getFlow(level, pos);
        float u0;
        float u1;
        float u2;
        float u3;
        float v0;
        float v1;
        float v2;
        float v3;
        if (flow.x == 0.0D && flow.z == 0.0D) {
            u0 = stillSprite.getU(0.0F);
            v0 = stillSprite.getV(0.0F);
            u1 = u0;
            v1 = stillSprite.getV(1.0F);
            u2 = stillSprite.getU(1.0F);
            v2 = v1;
            u3 = u2;
            v3 = v0;
        } else {
            float angle = (float) Mth.atan2(flow.z, flow.x) + (float) (Math.PI / 2);
            float sin = Mth.sin(angle) * 0.25F;
            float cos = Mth.cos(angle) * 0.25F;
            u0 = flowSprite.getU(0.5F + (-cos - sin));
            v0 = flowSprite.getV(0.5F + (-cos + sin));
            u1 = flowSprite.getU(0.5F + (-cos + sin));
            v1 = flowSprite.getV(0.5F + (cos + sin));
            u2 = flowSprite.getU(0.5F + (cos + sin));
            v2 = flowSprite.getV(0.5F + (cos - sin));
            u3 = flowSprite.getU(0.5F + (cos - sin));
            v3 = flowSprite.getV(0.5F + (-cos - sin));
        }

        int light = getLightColor(level, pos.below());
        float r = level.getShade(Direction.DOWN, true) * red;
        float g = level.getShade(Direction.DOWN, true) * green;
        float b = level.getShade(Direction.DOWN, true) * blue;
        emitDoubleSidedQuad(
                buffer,
                x, y + nwBottom + SURFACE_NUDGE, z, u0, v0,
                x, y + swBottom + SURFACE_NUDGE, z + 1.0F, u1, v1,
                x + 1.0F, y + seBottom + SURFACE_NUDGE, z + 1.0F, u2, v2,
                x + 1.0F, y + neBottom + SURFACE_NUDGE, z, u3, v3,
                r, g, b, alpha, light
        );
    }

    private static void emitDoubleSidedQuad(
            VertexConsumer buffer,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float red, float green, float blue, float alpha, int light
    ) {
        vertex(buffer, x0, y0, z0, red, green, blue, alpha, u0, v0, light);
        vertex(buffer, x1, y1, z1, red, green, blue, alpha, u1, v1, light);
        vertex(buffer, x2, y2, z2, red, green, blue, alpha, u2, v2, light);
        vertex(buffer, x3, y3, z3, red, green, blue, alpha, u3, v3, light);

        vertex(buffer, x3, y3, z3, red, green, blue, alpha, u3, v3, light);
        vertex(buffer, x2, y2, z2, red, green, blue, alpha, u2, v2, light);
        vertex(buffer, x1, y1, z1, red, green, blue, alpha, u1, v1, light);
        vertex(buffer, x0, y0, z0, red, green, blue, alpha, u0, v0, light);
    }

    private static float calculateAverageHeight(BlockAndTintGetter level, Fluid fluid, float currentHeight, float height1, float height2, BlockPos pos) {
        if (height2 >= 1.0F || height1 >= 1.0F) {
            return 1.0F;
        }

        float[] weighted = new float[2];
        if (height2 > 0.0F || height1 > 0.0F) {
            float diagonalHeight = getHangingHeight(level, fluid, pos);
            if (diagonalHeight >= 1.0F) {
                return 1.0F;
            }
            addWeightedHeight(weighted, diagonalHeight);
        }

        addWeightedHeight(weighted, currentHeight);
        addWeightedHeight(weighted, height2);
        addWeightedHeight(weighted, height1);
        return weighted[0] / weighted[1];
    }

    private static void addWeightedHeight(float[] output, float height) {
        if (height >= 0.8F) {
            output[0] += height * 10.0F;
            output[1] += 10.0F;
        } else if (height >= 0.0F) {
            output[0] += height;
            output[1] += 1.0F;
        }
    }

    private static float getHangingHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return getHangingHeight(level, fluid, pos, state, state.getFluidState());
    }

    private static float getHangingHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (!fluid.isSame(fluidState.getType())) {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }

        BlockState belowState = level.getBlockState(pos.below());
        return fluid.isSame(belowState.getFluidState().getType())
                ? 1.0F
                : fluidState.getOwnHeight();
    }

    private static void vertex(VertexConsumer buffer, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int light) {
        buffer.addVertex(x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setLight(light)
                .setNormal(0.0F, -1.0F, 0.0F);
    }

    private static int getLightColor(BlockAndTintGetter level, BlockPos pos) {
        int light = LevelRenderer.getLightColor(level, pos);
        int aboveLight = LevelRenderer.getLightColor(level, pos.above());
        int block = Math.max(light & 255, aboveLight & 255);
        int sky = Math.max((light >> 16) & 255, (aboveLight >> 16) & 255);
        return block | (sky << 16);
    }
}
