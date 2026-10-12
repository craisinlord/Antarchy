package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public final class PortalGunPortalSkyQueries {
    private static final int MAX_ENTRIES = 128;
    private static final LinkedHashMap<Key, QueryState> QUERIES = new LinkedHashMap<>(16, 0.75F, true);

    private PortalGunPortalSkyQueries() {
    }

    public static boolean shouldRenderSky(UUID source, UUID destination, int level) {
        QueryState state = QUERIES.get(new Key(source, destination, level));
        if (state == null) {
            return true;
        }
        poll(state);
        return !state.hasResult || state.visible;
    }

    public static void querySkyVisibility(UUID source, UUID destination, int level) {
        Key key = new Key(source, destination, level);
        QueryState state = QUERIES.computeIfAbsent(key, ignored -> new QueryState(GL15.glGenQueries()));
        poll(state);
        if (state.inFlight) {
            trim();
            return;
        }
        MeshData mesh = buildFarPlane();
        if (mesh == null) {
            trim();
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        int previousDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        modelViewStack.pushMatrix();
        GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, state.query);
        try {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            modelViewStack.identity();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f(), previousSorting);
            RenderSystem.setShader(GameRenderer::getPositionShader);
            BufferUploader.drawWithShader(mesh);
        } finally {
            GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
            state.inFlight = true;
            GL11.glDepthFunc(previousDepthFunction);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
        trim();
    }

    public static void clear() {
        for (QueryState state : QUERIES.values()) {
            GL15.glDeleteQueries(state.query);
        }
        QUERIES.clear();
    }

    private static void poll(QueryState state) {
        if (state.inFlight && GL15.glGetQueryObjecti(state.query, GL15.GL_QUERY_RESULT_AVAILABLE) != 0) {
            state.visible = GL15.glGetQueryObjecti(state.query, GL15.GL_QUERY_RESULT) != 0;
            state.hasResult = true;
            state.inFlight = false;
        }
    }

    private static MeshData buildFarPlane() {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        buffer.addVertex(-1.0F, -1.0F, 0.99999F);
        buffer.addVertex(3.0F, -1.0F, 0.99999F);
        buffer.addVertex(-1.0F, 3.0F, 0.99999F);
        return buffer.build();
    }

    private static void trim() {
        Iterator<Map.Entry<Key, QueryState>> iterator = QUERIES.entrySet().iterator();
        while (QUERIES.size() > MAX_ENTRIES && iterator.hasNext()) {
            QueryState state = iterator.next().getValue();
            if (!state.inFlight) {
                GL15.glDeleteQueries(state.query);
                iterator.remove();
            }
        }
    }

    private record Key(UUID source, UUID destination, int level) {
    }

    private static final class QueryState {
        private final int query;
        private boolean inFlight;
        private boolean hasResult;
        private boolean visible = true;

        private QueryState(int query) {
            this.query = query;
        }
    }
}
