package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import java.util.ArrayDeque;
import java.util.Deque;

public final class PortalGunPortalRenderState {
    private static final ThreadLocal<Deque<PortalGunWorldPortalShape>> ACTIVE_DESTINATION_SHAPES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<PortalGunWorldPortalShape>> ACTIVE_SOURCE_SHAPES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> RENDER_ALL_DEPTH = ThreadLocal.withInitial(() -> 0);

    private PortalGunPortalRenderState() {
    }

    public static void pushPortalView(PortalGunWorldPortalShape sourceShape, PortalGunWorldPortalShape destinationShape, boolean renderAll) {
        ACTIVE_SOURCE_SHAPES.get().push(sourceShape);
        ACTIVE_DESTINATION_SHAPES.get().push(destinationShape);
        if (renderAll) {
            RENDER_ALL_DEPTH.set(RENDER_ALL_DEPTH.get() + 1);
        }
    }

    public static void popPortalView(boolean renderAll) {
        Deque<PortalGunWorldPortalShape> sourceShapes = ACTIVE_SOURCE_SHAPES.get();
        if (!sourceShapes.isEmpty()) {
            sourceShapes.pop();
        }
        if (sourceShapes.isEmpty()) {
            ACTIVE_SOURCE_SHAPES.remove();
        }
        Deque<PortalGunWorldPortalShape> destinationShapes = ACTIVE_DESTINATION_SHAPES.get();
        if (!destinationShapes.isEmpty()) {
            destinationShapes.pop();
        }
        if (destinationShapes.isEmpty()) {
            ACTIVE_DESTINATION_SHAPES.remove();
        }
        if (renderAll) {
            int depth = Math.max(0, RENDER_ALL_DEPTH.get() - 1);
            if (depth == 0) {
                RENDER_ALL_DEPTH.remove();
            } else {
                RENDER_ALL_DEPTH.set(depth);
            }
        }
    }

    public static void pushDestinationShape(PortalGunWorldPortalShape shape) {
        ACTIVE_DESTINATION_SHAPES.get().push(shape);
    }

    public static void popDestinationShape() {
        Deque<PortalGunWorldPortalShape> shapes = ACTIVE_DESTINATION_SHAPES.get();
        if (!shapes.isEmpty()) {
            shapes.pop();
        }
        if (shapes.isEmpty()) {
            ACTIVE_DESTINATION_SHAPES.remove();
        }
    }

    public static PortalGunWorldPortalShape getDestinationShape() {
        Deque<PortalGunWorldPortalShape> shapes = ACTIVE_DESTINATION_SHAPES.get();
        return shapes.isEmpty() ? null : shapes.peek();
    }

    public static PortalGunWorldPortalShape getSourceShape() {
        Deque<PortalGunWorldPortalShape> shapes = ACTIVE_SOURCE_SHAPES.get();
        return shapes.isEmpty() ? null : shapes.peek();
    }

    public static boolean renderAll() {
        return RENDER_ALL_DEPTH.get() > 0;
    }
}
