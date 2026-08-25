package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class PortalGunPortalRenderState {
    private static final ThreadLocal<Deque<PortalGunWorldPortalShape>> ACTIVE_DESTINATION_SHAPES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<PortalGunWorldPortalShape>> ACTIVE_SOURCE_SHAPES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<PortalRenderContext>> ACTIVE_CONTEXTS = ThreadLocal.withInitial(ArrayDeque::new);
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

    public static void pushPortalView(PortalRenderContext context) {
        ACTIVE_CONTEXTS.get().push(context);
        pushPortalView(context.sourceShape(), context.destinationShape(), context.renderAll());
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
        Deque<PortalRenderContext> contexts = ACTIVE_CONTEXTS.get();
        if (!contexts.isEmpty()) {
            contexts.pop();
        }
        if (contexts.isEmpty()) {
            ACTIVE_CONTEXTS.remove();
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

    public static PortalRenderContext getContext() {
        Deque<PortalRenderContext> contexts = ACTIVE_CONTEXTS.get();
        return contexts.isEmpty() ? null : contexts.peek();
    }

    public static int getRenderLevel() {
        return ACTIVE_CONTEXTS.get().size();
    }

    public static boolean renderAll() {
        return RENDER_ALL_DEPTH.get() > 0;
    }

    public static boolean shouldRenderBounds(AABB bounds) {
        if (renderAll()) {
            return true;
        }
        PortalRenderContext context = getContext();
        if (context == null) {
            PortalGunWorldPortalShape destinationShape = getDestinationShape();
            PortalGunWorldPortalShape sourceShape = getSourceShape();
            return destinationShape == null || shouldRenderBounds(bounds, sourceShape, destinationShape, false);
        }
        return shouldRenderBounds(bounds, context.sourceShape(), context.destinationShape(), context.renderAll());
    }

    private static boolean shouldRenderBounds(AABB bounds, PortalGunWorldPortalShape sourceShape, PortalGunWorldPortalShape destinationShape, boolean renderAll) {
        if (renderAll) {
            return true;
        }
        if (!destinationShape.intersectsFront(bounds, 0.125D)) {
            return false;
        }
        if (!destinationShape.intersectsPortalColumn(bounds, 1.5D, 1.5D, destinationShape.halfDepth() + 96.0D)) {
            return false;
        }
        return sourceShape == null || !sourceShape.intersectsFront(bounds, -0.02D);
    }

    public record PortalRenderContext(
            PortalGunWorldPortalShape sourceShape,
            PortalGunWorldPortalShape destinationShape,
            Vec3 cameraPos,
            Vec3 look,
            Vec3 up,
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix,
            int renderLevel,
            boolean renderAll
    ) {
    }
}
