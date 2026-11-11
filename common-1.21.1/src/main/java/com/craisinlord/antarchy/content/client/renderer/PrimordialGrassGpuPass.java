package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.PrimordialGrassWind;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

final class PrimordialGrassGpuPass {
    static final int INSTANCE_BYTES = 16;

    private static final int MAX_TRAMPLES = 32;
    private static final int GUST_TABLE_FLOATS = 16;
    private static final int MESH_STRIDE = 28;
    private static final float HALF_WIDTH = 0.45F;
    private static final int TIMER_SLOTS = 4;
    private static final double TIMER_SMOOTHING = 0.1D;
    private static boolean coreInstancing;
    private static boolean timerSupported;

    private static final String GRASS_VERTEX = """
            #version 150
            in vec2 Offset;
            in vec2 Shape;
            in vec2 UV0;
            in float Flippable;
            in vec3 Root;
            in vec2 Meta;
            in ivec2 UV2;
            uniform sampler2D Sampler2;
            uniform mat4 ModelViewMat;
            uniform mat4 ProjMat;
            uniform vec3 ChunkOffset;
            uniform int FogShape;
            uniform vec2 WindDir;
            uniform float GustLocal;
            uniform float CalmPhase;
            uniform float WindEnabled;
            uniform vec2 GustTable[8];
            uniform vec4 TramplePos[32];
            uniform vec3 TrampleMotion[32];
            uniform int TrampleCount;
            uniform vec4 VariantUv[3];
            uniform float VariantHeight[3];
            out float vertexDistance;
            out vec4 vertexColor;
            out vec2 texCoord0;
            float smoothCurve(float t) {
                return t * t * (3.0 - 2.0 * t);
            }
            float gustEnvelope(float age) {
                if (age < 0.0 || age >= 136.0) {
                    return 0.0;
                }
                if (age < 25.0) {
                    return smoothCurve(age / 25.0);
                }
                if (age < 54.0) {
                    return 1.0;
                }
                return 1.0 - smoothCurve((age - 54.0) / 82.0);
            }
            void main() {
                float h = Shape.x;
                float f = Shape.y;
                float metaByte = floor(Meta.x * 255.0 + 0.5);
                float exposure = metaByte > 127.5 ? 1.0 : 0.0;
                int variant = clamp(int(mod(metaByte, 128.0)), 0, 2);
                float height = VariantHeight[variant];
                float tuftHash = Meta.y;
                float mirror = Flippable > 0.5 && mod(floor(tuftHash * 255.0 + 0.5), 2.0) > 0.5 ? -1.0 : 1.0;
                vec3 root = Root + ChunkOffset;
                vec3 rel = root + vec3(Offset.x * mirror, h * height, Offset.y);
                float calm = sin(CalmPhase + root.x * 0.07 + root.z * 0.09) * 0.04;
                float gustTime = GustLocal - dot(root.xz, WindDir) / 0.31;
                float idx = floor(gustTime / 800.0);
                vec2 entry = GustTable[clamp(int(idx) + 4, 0, 7)];
                float age = gustTime - idx * 800.0 - entry.x;
                float gust = gustEnvelope(age) * entry.y;
                vec2 windOffset = WindDir * (calm * h * h + gust * f * exposure * 0.34) * WindEnabled;
                vec2 push = vec2(0.0);
                if (TrampleCount > 0 && h > 0.0 && dot(root, root) < 1444.0) {
                    for (int i = 0; i < TrampleCount; i++) {
                        vec4 p = TramplePos[i];
                        float vertical = 1.0 - smoothstep(1.0, 1.6, abs(root.y - p.y));
                        if (vertical <= 0.0) {
                            continue;
                        }
                        vec2 d = root.xz - p.xz;
                        float dist = length(d);
                        vec3 motion = TrampleMotion[i];
                        float w = motion.z * vertical * (1.0 - smoothstep(p.w * 0.45, p.w, dist));
                        push += w * (d / max(dist, 0.25) * 0.45 + motion.xy * 0.85);
                    }
                }
                float raw = length(push);
                float lean = 0.0;
                if (raw > 0.0) {
                    lean = 1.0 - exp(-raw * 1.25);
                    push *= lean / raw;
                }
                float bend = h * (0.35 + 0.65 * h);
                float variation = 0.8 + 0.35 * tuftHash;
                float flutter = sin(CalmPhase * 3.0 + tuftHash * 6.2831855) * 0.1 * bend;
                vec3 pos = rel;
                pos.xz += windOffset * (1.0 - 0.6 * lean) + push * 0.5 * variation * bend + vec2(-push.y, push.x) * flutter;
                pos.y -= h * h * height * 0.14 * lean * lean;
                gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
                vertexDistance = FogShape == 0 ? length(pos) : max(length(pos.xz), abs(pos.y));
                vertexColor = texture(Sampler2, clamp(vec2(UV2) / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
                texCoord0 = VariantUv[variant].xy + UV0 * VariantUv[variant].zw;
            }
            """;

    private static final String GRASS_FRAGMENT = """
            #version 150
            uniform sampler2D Sampler0;
            uniform vec4 ColorModulator;
            uniform float FogStart;
            uniform float FogEnd;
            uniform vec4 FogColor;
            in float vertexDistance;
            in vec4 vertexColor;
            in vec2 texCoord0;
            out vec4 fragColor;
            void main() {
                vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
                if (color.a < 0.1) {
                    discard;
                }
                if (vertexDistance > FogStart) {
                    float fogValue = vertexDistance < FogEnd ? smoothstep(FogStart, FogEnd, vertexDistance) : 1.0;
                    color = vec4(mix(color.rgb, FogColor.rgb, fogValue * FogColor.a), color.a);
                }
                fragColor = color;
            }
            """;

    private static boolean initialized;
    private static boolean failed;
    private static int program;
    private static int meshBuffer;
    private static int sampler0Location;
    private static int sampler2Location;
    private static int modelViewMatLocation;
    private static int projMatLocation;
    private static int chunkOffsetLocation;
    private static int fogShapeLocation;
    private static int windDirLocation;
    private static int gustLocalLocation;
    private static int calmPhaseLocation;
    private static int windEnabledLocation;
    private static int gustTableLocation;
    private static int tramplePosLocation;
    private static int trampleMotionLocation;
    private static int trampleCountLocation;
    private static int colorModulatorLocation;
    private static int fogStartLocation;
    private static int fogEndLocation;
    private static int fogColorLocation;
    private static int variantUvLocation;
    private static int variantHeightLocation;
    private static final float[] VARIANT_UV = new float[12];
    private static final float[] VARIANT_HEIGHT = new float[3];
    private static int[] timerStart;
    private static int[] timerEnd;
    private static final boolean[] TIMER_PENDING = new boolean[TIMER_SLOTS];
    private static int timerSlot;
    private static boolean timerActive;
    private static double gpuNanos;
    @Nullable
    private static Method irisInstance;
    @Nullable
    private static Method irisShaderPackInUse;
    private static boolean irisChecked;

    private PrimordialGrassGpuPass() {
    }

    static boolean available() {
        if (failed) {
            return false;
        }
        return ensureInitialized();
    }

    static void setVariants(TextureAtlasSprite[] sprites) {
        for (int i = 0; i < VARIANT_HEIGHT.length; i++) {
            TextureAtlasSprite sprite = sprites[Math.min(i, sprites.length - 1)];
            VARIANT_UV[i * 4] = sprite.getU0();
            VARIANT_UV[i * 4 + 1] = sprite.getV0();
            VARIANT_UV[i * 4 + 2] = sprite.getU1() - sprite.getU0();
            VARIANT_UV[i * 4 + 3] = sprite.getV1() - sprite.getV0();
            VARIANT_HEIGHT[i] = sprite.contents().height() / 16.0F;
        }
    }

    private static void uploadMesh() {
        ByteBuffer data = BufferUtils.createByteBuffer(PrimordialGrassLod.shapeVertices() * MESH_STRIDE);
        for (int bands : PrimordialGrassLod.shapeBands()) {
            for (int faces = 1; faces <= PrimordialGrassLod.MAX_FACES; faces++) {
                writeShape(data, bands, faces);
            }
        }
        data.flip();
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, meshBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static void writeShape(ByteBuffer data, int bands, int faces) {
        float flippable = faces == 1 ? 1.0F : 0.0F;
        for (int face = 0; face < faces; face++) {
            float x = face == 0 ? HALF_WIDTH : -HALF_WIDTH;
            if (face > 0) writeMeshVertex(data, -x, -HALF_WIDTH, 0.0F, 0.0F, flippable);
            for (int level = 0; level <= bands; level++) {
                float h = level / (float) bands;
                writeMeshVertex(data, -x, -HALF_WIDTH, h, 0.0F, flippable);
                writeMeshVertex(data, x, HALF_WIDTH, h, 1.0F, flippable);
            }
            if (face < faces - 1) writeMeshVertex(data, x, HALF_WIDTH, 1.0F, 1.0F, flippable);
        }
    }

    private static void writeMeshVertex(ByteBuffer data, float x, float z, float h, float u, float flippable) {
        data.putFloat(x).putFloat(z);
        data.putFloat(h).putFloat((float) PrimordialGrassWind.flexibility(h));
        data.putFloat(u).putFloat(1.0F - h);
        data.putFloat(flippable);
    }

    static int createInstanceBuffer() {
        return GL15.glGenBuffers();
    }

    static int createVertexArray(int instanceBuffer) {
        int vao = GL30.glGenVertexArrays();
        GlStateManager._glBindVertexArray(vao);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, meshBuffer);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, MESH_STRIDE, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, MESH_STRIDE, 8L);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, MESH_STRIDE, 16L);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, MESH_STRIDE, 24L);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceBuffer);
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, INSTANCE_BYTES, 0L);
        perInstance(4);
        GL20.glEnableVertexAttribArray(5);
        GL20.glVertexAttribPointer(5, 2, GL11.GL_UNSIGNED_BYTE, true, INSTANCE_BYTES, 12L);
        perInstance(5);
        GL20.glEnableVertexAttribArray(6);
        GL30.glVertexAttribIPointer(6, 2, GL11.GL_UNSIGNED_BYTE, INSTANCE_BYTES, 14L);
        perInstance(6);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        VertexBuffer.unbind();
        return vao;
    }

    private static void perInstance(int attribute) {
        if (coreInstancing) {
            GL33.glVertexAttribDivisor(attribute, 1);
        } else {
            ARBInstancedArrays.glVertexAttribDivisorARB(attribute, 1);
        }
    }

    static void uploadInstances(int instanceBuffer, ByteBuffer data) {
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    static void deleteSection(int vao, int instanceBuffer) {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(instanceBuffer);
    }

    static void begin(PrimordialGrassWind.ShaderFrame frame, float[] gustTable, boolean windEnabled,
                      float[] tramplePositions, float[] trampleMotions, int trampleCount, boolean timed) {
        if (timed) beginTimer();
        GlStateManager._glUseProgram(program);
        GL20.glUniform1i(sampler0Location, 0);
        GL20.glUniform1i(sampler2Location, 2);
        GL20.glUniform2f(windDirLocation, frame.directionX(), frame.directionZ());
        GL20.glUniform1f(gustLocalLocation, frame.gustLocal());
        GL20.glUniform1f(calmPhaseLocation, frame.calmPhase());
        GL20.glUniform1f(windEnabledLocation, windEnabled ? 1.0F : 0.0F);
        GL20.glUniform1f(fogStartLocation, RenderSystem.getShaderFogStart());
        GL20.glUniform1f(fogEndLocation, RenderSystem.getShaderFogEnd());
        float[] fogColor = RenderSystem.getShaderFogColor();
        GL20.glUniform4f(fogColorLocation, fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
        GL20.glUniform1i(fogShapeLocation, RenderSystem.getShaderFogShape().getIndex());
        float[] colorModulator = RenderSystem.getShaderColor();
        GL20.glUniform4f(colorModulatorLocation, colorModulator[0], colorModulator[1], colorModulator[2], colorModulator[3]);
        int count = Math.max(0, Math.min(MAX_TRAMPLES, trampleCount));
        GL20.glUniform1i(trampleCountLocation, count);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrix = stack.mallocFloat(16);
            RenderSystem.getModelViewMatrix().get(matrix);
            GL20.glUniformMatrix4fv(modelViewMatLocation, false, matrix);
            RenderSystem.getProjectionMatrix().get(matrix);
            GL20.glUniformMatrix4fv(projMatLocation, false, matrix);
            FloatBuffer gusts = stack.mallocFloat(GUST_TABLE_FLOATS);
            gusts.put(gustTable, 0, GUST_TABLE_FLOATS).flip();
            GL20.glUniform2fv(gustTableLocation, gusts);
            FloatBuffer variantUv = stack.mallocFloat(VARIANT_UV.length);
            variantUv.put(VARIANT_UV).flip();
            GL20.glUniform4fv(variantUvLocation, variantUv);
            FloatBuffer variantHeight = stack.mallocFloat(VARIANT_HEIGHT.length);
            variantHeight.put(VARIANT_HEIGHT).flip();
            GL20.glUniform1fv(variantHeightLocation, variantHeight);
            if (count > 0) {
                FloatBuffer positions = stack.mallocFloat(count * 4);
                positions.put(tramplePositions, 0, count * 4).flip();
                GL20.glUniform4fv(tramplePosLocation, positions);
                FloatBuffer motions = stack.mallocFloat(count * 3);
                motions.put(trampleMotions, 0, count * 3).flip();
                GL20.glUniform3fv(trampleMotionLocation, motions);
            }
        }
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(RenderSystem.getShaderTexture(0));
        RenderSystem.activeTexture(GL13.GL_TEXTURE2);
        RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
    }

    static void draw(int vao, int first, int vertices, int instances, float offsetX, float offsetY, float offsetZ) {
        GL20.glUniform3f(chunkOffsetLocation, offsetX, offsetY, offsetZ);
        GlStateManager._glBindVertexArray(vao);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, first, vertices, instances);
    }

    static void end() {
        VertexBuffer.unbind();
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._glUseProgram(0);
        resetShaderCache();
        BufferUploader.invalidate();
        endTimer();
    }

    static double gpuMillis() {
        return gpuNanos / 1_000_000.0D;
    }

    private static void beginTimer() {
        if (!timerSupported) {
            return;
        }
        int slot = timerSlot;
        if (TIMER_PENDING[slot]) {
            if (GL15.glGetQueryObjecti(timerEnd[slot], GL15.GL_QUERY_RESULT_AVAILABLE) == GL11.GL_FALSE) {
                return;
            }
            long elapsed = GL33.glGetQueryObjecti64(timerEnd[slot], GL15.GL_QUERY_RESULT)
                    - GL33.glGetQueryObjecti64(timerStart[slot], GL15.GL_QUERY_RESULT);
            gpuNanos += (elapsed - gpuNanos) * TIMER_SMOOTHING;
            TIMER_PENDING[slot] = false;
        }
        GL33.glQueryCounter(timerStart[slot], GL33.GL_TIMESTAMP);
        timerActive = true;
    }

    private static void endTimer() {
        if (!timerActive) {
            return;
        }
        timerActive = false;
        GL33.glQueryCounter(timerEnd[timerSlot], GL33.GL_TIMESTAMP);
        TIMER_PENDING[timerSlot] = true;
        timerSlot = (timerSlot + 1) % TIMER_SLOTS;
    }

    private static boolean ensureInitialized() {
        if (initialized) {
            return true;
        }
        if (failed) {
            return false;
        }
        try {
            GLCapabilities capabilities = GL.getCapabilities();
            coreInstancing = capabilities.OpenGL33;
            if (!coreInstancing && !capabilities.GL_ARB_instanced_arrays) {
                throw new IllegalStateException("OpenGL 3.3 or ARB_instanced_arrays is required");
            }
            timerSupported = capabilities.OpenGL33 || capabilities.GL_ARB_timer_query;
            program = link(GRASS_VERTEX, GRASS_FRAGMENT);
            sampler0Location = GL20.glGetUniformLocation(program, "Sampler0");
            sampler2Location = GL20.glGetUniformLocation(program, "Sampler2");
            modelViewMatLocation = GL20.glGetUniformLocation(program, "ModelViewMat");
            projMatLocation = GL20.glGetUniformLocation(program, "ProjMat");
            chunkOffsetLocation = GL20.glGetUniformLocation(program, "ChunkOffset");
            fogShapeLocation = GL20.glGetUniformLocation(program, "FogShape");
            windDirLocation = GL20.glGetUniformLocation(program, "WindDir");
            gustLocalLocation = GL20.glGetUniformLocation(program, "GustLocal");
            calmPhaseLocation = GL20.glGetUniformLocation(program, "CalmPhase");
            windEnabledLocation = GL20.glGetUniformLocation(program, "WindEnabled");
            gustTableLocation = GL20.glGetUniformLocation(program, "GustTable");
            tramplePosLocation = GL20.glGetUniformLocation(program, "TramplePos");
            trampleMotionLocation = GL20.glGetUniformLocation(program, "TrampleMotion");
            trampleCountLocation = GL20.glGetUniformLocation(program, "TrampleCount");
            colorModulatorLocation = GL20.glGetUniformLocation(program, "ColorModulator");
            fogStartLocation = GL20.glGetUniformLocation(program, "FogStart");
            fogEndLocation = GL20.glGetUniformLocation(program, "FogEnd");
            fogColorLocation = GL20.glGetUniformLocation(program, "FogColor");
            variantUvLocation = GL20.glGetUniformLocation(program, "VariantUv");
            variantHeightLocation = GL20.glGetUniformLocation(program, "VariantHeight");
            meshBuffer = GL15.glGenBuffers();
            uploadMesh();
            timerStart = new int[TIMER_SLOTS];
            timerEnd = new int[TIMER_SLOTS];
            for (int i = 0; i < TIMER_SLOTS; i++) {
                timerStart[i] = GL15.glGenQueries();
                timerEnd[i] = GL15.glGenQueries();
            }
            initialized = true;
            return true;
        } catch (RuntimeException exception) {
            Antarchy.LOGGER.error("Failed to set up Primordial grass GPU pass, using static grass", exception);
            failed = true;
            return false;
        }
    }

    private static int link(String vertexSource, String fragmentSource) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, vertexSource);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);
        int linked = GL20.glCreateProgram();
        GL20.glAttachShader(linked, vertex);
        GL20.glAttachShader(linked, fragment);
        GL20.glBindAttribLocation(linked, 0, "Offset");
        GL20.glBindAttribLocation(linked, 1, "Shape");
        GL20.glBindAttribLocation(linked, 2, "UV0");
        GL20.glBindAttribLocation(linked, 3, "Flippable");
        GL20.glBindAttribLocation(linked, 4, "Root");
        GL20.glBindAttribLocation(linked, 5, "Meta");
        GL20.glBindAttribLocation(linked, 6, "UV2");
        GL20.glLinkProgram(linked);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        if (GL20.glGetProgrami(linked, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(linked);
            GL20.glDeleteProgram(linked);
            throw new IllegalStateException("Primordial grass shader link failed: " + log);
        }
        return linked;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Primordial grass shader compile failed: " + log);
        }
        return shader;
    }

    private static void resetShaderCache() {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            shader = GameRenderer.getPositionShader();
        }
        if (shader != null) {
            shader.clear();
        }
    }

    static boolean shaderPackActive() {
        if (!irisChecked) {
            irisChecked = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisInstance = api.getMethod("getInstance");
                irisShaderPackInUse = api.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                irisInstance = null;
                irisShaderPackInUse = null;
            }
        }
        if (irisInstance == null || irisShaderPackInUse == null) {
            return false;
        }
        try {
            Object instance = irisInstance.invoke(null);
            return Boolean.TRUE.equals(irisShaderPackInUse.invoke(instance));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }
}
