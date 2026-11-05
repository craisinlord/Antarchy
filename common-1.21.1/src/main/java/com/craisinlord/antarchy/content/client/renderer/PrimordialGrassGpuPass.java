package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.PrimordialGrassWind;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

public final class PrimordialGrassGpuPass {
    private static final int MAX_TRAMPLES = 32;
    private static final int GUST_TABLE_FLOATS = 16;

    private static final String GRASS_VERTEX = """
            #version 150
            in vec3 Position;
            in vec4 Color;
            in vec2 UV0;
            in ivec2 UV2;
            in vec3 Normal;
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
                float h = Color.r;
                float f = Color.g;
                float exposure = Color.b;
                vec3 rel = Position + ChunkOffset;
                vec3 root = vec3(rel.x - Normal.x, rel.y - h * 1.125, rel.z - Normal.z);
                float calm = sin(CalmPhase + root.x * 0.07 + root.z * 0.09) * 0.04;
                float gustTime = GustLocal - dot(root.xz, WindDir) / 0.31;
                float idx = floor(gustTime / 800.0);
                vec2 entry = GustTable[clamp(int(idx) + 4, 0, 7)];
                float age = gustTime - idx * 800.0 - entry.x;
                float gust = gustEnvelope(age) * entry.y;
                vec2 windOffset = WindDir * (calm * h * h + gust * f * exposure * 0.34) * WindEnabled;
                vec2 push = vec2(0.0);
                if (TrampleCount > 0 && dot(root, root) < 3136.0) {
                    for (int i = 0; i < TrampleCount; i++) {
                        vec4 p = TramplePos[i];
                        float vertical = 1.0 - smoothstep(1.0, 1.6, abs(root.y - p.y));
                        if (vertical <= 0.0) {
                            continue;
                        }
                        vec2 d = root.xz - p.xz;
                        float dist = length(d);
                        vec3 motion = TrampleMotion[i];
                        float w = motion.z * vertical * (1.0 - smoothstep(p.w * 0.35, p.w, dist));
                        push += w * (d / max(dist, 0.3) + motion.xy * 0.35);
                    }
                }
                float lean = length(push);
                if (lean > 1.0) {
                    push /= lean;
                    lean = 1.0;
                }
                float tuftHash = Color.a;
                float bend = h * (0.35 + 0.65 * h);
                float variation = 0.8 + 0.35 * tuftHash;
                float flutter = sin(CalmPhase * 3.0 + tuftHash * 6.2831855) * 0.1 * bend;
                vec3 pos = rel;
                pos.xz += windOffset * (1.0 - 0.6 * lean) + push * 0.7 * variation * bend + vec2(-push.y, push.x) * flutter;
                pos.y -= h * h * 1.125 * 0.24 * lean * lean;
                gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
                vertexDistance = FogShape == 0 ? length(pos) : max(length(pos.xz), abs(pos.y));
                vertexColor = texture(Sampler2, clamp(vec2(UV2) / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
                texCoord0 = UV0;
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
    @Nullable
    private static Method irisInstance;
    @Nullable
    private static Method irisShaderPackInUse;
    private static boolean irisChecked;

    private PrimordialGrassGpuPass() {
    }

    public static boolean available() {
        if (failed || irisShaderPackActive()) {
            return false;
        }
        return ensureInitialized();
    }

    public static void begin(PrimordialGrassWind.ShaderFrame frame, float[] gustTable, boolean windEnabled,
                             float[] tramplePositions, float[] trampleMotions, int trampleCount) {
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

    public static void draw(VertexBuffer buffer, float offsetX, float offsetY, float offsetZ) {
        GL20.glUniform3f(chunkOffsetLocation, offsetX, offsetY, offsetZ);
        buffer.bind();
        buffer.draw();
    }

    public static void end() {
        VertexBuffer.unbind();
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._glUseProgram(0);
        resetShaderCache();
        BufferUploader.invalidate();
    }

    private static boolean ensureInitialized() {
        if (initialized) {
            return true;
        }
        if (failed) {
            return false;
        }
        try {
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
            initialized = true;
            return true;
        } catch (RuntimeException exception) {
            Antarchy.LOGGER.error("Failed to set up Primordial grass GPU pass, falling back to CPU animation", exception);
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
        GL20.glBindAttribLocation(linked, 0, "Position");
        GL20.glBindAttribLocation(linked, 1, "Color");
        GL20.glBindAttribLocation(linked, 2, "UV0");
        GL20.glBindAttribLocation(linked, 3, "UV2");
        GL20.glBindAttribLocation(linked, 4, "Normal");
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

    private static boolean irisShaderPackActive() {
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
