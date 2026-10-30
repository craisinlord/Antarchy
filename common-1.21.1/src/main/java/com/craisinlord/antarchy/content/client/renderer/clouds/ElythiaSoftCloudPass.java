package com.craisinlord.antarchy.content.client.renderer.clouds;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

final class ElythiaSoftCloudPass {
    static final int MAX_CLEARINGS = 8;
    static final float NO_GROWTH_LIMIT = 1.0E9F;
    private static final float RESOLUTION_SCALE = 0.5F;
    private static final float COVERAGE_STEP = 1.0F / 24.0F;
    private static final float SURFACE_OPACITY = 0.22F;
    private static final float MAX_ALPHA = 0.93F;
    private static final float HAZE_STRENGTH = 0.55F;

    private static final String COVERAGE_VERTEX = """
            #version 150
            in vec3 Position;
            in vec2 UV0;
            in vec4 Color;
            in vec3 Normal;
            uniform mat4 ViewMat;
            uniform mat4 ProjMat;
            uniform vec3 Offset;
            uniform float FogEnd;
            uniform float GrowthTop;
            uniform float Clearable;
            uniform vec4 Clearings[8];
            uniform int ClearingCount;
            uniform float ClearingStrength;
            uniform vec2 SunLight;
            out float vWeight;
            out float vShade;
            out float vDistance;
            out float vHaze;
            void main() {
                vec3 rel = Position + Offset;
                gl_Position = ProjMat * ViewMat * vec4(rel, 1.0);
                float distance = length(rel);
                float fogDistance = max(length(rel.xz), abs(rel.y));
                float fogFade = 1.0 - smoothstep(FogEnd * 0.6, FogEnd, fogDistance);
                float nearFade = smoothstep(6.0, 22.0, distance);
                float growth = 1.0 - smoothstep(GrowthTop - 40.0, GrowthTop, rel.y);
                float clear = 1.0;
                if (Clearable > 0.0) {
                    for (int i = 0; i < ClearingCount; i++) {
                        float d = length(rel.xz - Clearings[i].xz);
                        clear = min(clear, smoothstep(Clearings[i].w * 0.7, Clearings[i].w * 1.4, d));
                    }
                    clear = mix(1.0, clear, ClearingStrength);
                }
                vWeight = fogFade * nearFade * growth * clear;
                float sunlit = mix(Color.b, Color.g, SunLight.x);
                vShade = Color.r * mix(1.0, 0.55 + 0.45 * sunlit, SunLight.y);
                vDistance = distance;
                vHaze = smoothstep(120.0, FogEnd, fogDistance);
            }
            """;

    private static final String COVERAGE_FRAGMENT = """
            #version 150
            uniform sampler2D SceneDepth;
            uniform vec2 DepthScale;
            uniform float Step;
            in float vWeight;
            in float vShade;
            in float vDistance;
            in float vHaze;
            out vec4 fragColor;
            void main() {
                ivec2 size = textureSize(SceneDepth, 0);
                ivec2 coord = clamp(ivec2(gl_FragCoord.xy * DepthScale), ivec2(0), size - ivec2(1));
                if (gl_FragCoord.z > texelFetch(SceneDepth, coord, 0).r) {
                    discard;
                }
                float coverage = vWeight * Step;
                float weight = coverage * exp2(-vDistance / 120.0) * 64.0;
                fragColor = vec4(weight * vShade, weight, coverage, weight * vHaze);
            }
            """;

    private static final String COMPOSITE_VERTEX = """
            #version 150
            out vec2 texCoord;
            void main() {
                vec2 corner = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
                texCoord = corner;
                gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);
            }
            """;

    private static final String COMPOSITE_FRAGMENT = """
            #version 150
            uniform sampler2D Coverage;
            uniform vec2 TexelSize;
            uniform vec3 LitColor;
            uniform vec3 ShadowColor;
            uniform vec3 FogColor;
            uniform float Opacity;
            uniform float MaxAlpha;
            uniform float StepInverse;
            uniform float HazeStrength;
            in vec2 texCoord;
            out vec4 fragColor;
            void main() {
                vec4 c = texture(Coverage, texCoord) * 0.4
                        + texture(Coverage, texCoord + vec2(TexelSize.x, 0.0)) * 0.15
                        + texture(Coverage, texCoord - vec2(TexelSize.x, 0.0)) * 0.15
                        + texture(Coverage, texCoord + vec2(0.0, TexelSize.y)) * 0.15
                        + texture(Coverage, texCoord - vec2(0.0, TexelSize.y)) * 0.15;
                float count = c.b * StepInverse;
                if (count < 0.02) {
                    discard;
                }
                float weight = max(c.g, 1.0e-6);
                float shade = clamp(c.r / weight, 0.0, 1.0);
                float haze = clamp(c.a / weight, 0.0, 1.0);
                vec3 color = mix(ShadowColor, LitColor, shade);
                color = mix(color, FogColor, haze * HazeStrength);
                float alpha = (1.0 - pow(1.0 - Opacity, count)) * MaxAlpha;
                fragColor = vec4(color, alpha);
            }
            """;

    record Draw(VertexBuffer buffer, float offsetX, float offsetY, float offsetZ, float fogEnd, float opacity,
                float growthTop, boolean clearable) {
    }

    record Frame(Vec3 cloudColor, float[] fogColor, float sunEast, float sunStrength,
                 float[] clearings, int clearingCount, float clearingStrength) {
    }

    private static boolean initialized;
    private static boolean failed;
    private static int coverageProgram;
    private static int compositeProgram;
    private static int emptyVertexArray;
    private static int viewMatLocation;
    private static int projMatLocation;
    private static int offsetLocation;
    private static int fogEndLocation;
    private static int growthTopLocation;
    private static int clearableLocation;
    private static int clearingsLocation;
    private static int clearingCountLocation;
    private static int clearingStrengthLocation;
    private static int sunLightLocation;
    private static int sceneDepthLocation;
    private static int depthScaleLocation;
    private static int stepLocation;
    private static int coverageLocation;
    private static int texelSizeLocation;
    private static int litColorLocation;
    private static int shadowColorLocation;
    private static int fogColorLocation;
    private static int opacityLocation;
    private static int maxAlphaLocation;
    private static int stepInverseLocation;
    private static int hazeStrengthLocation;
    private static int framebuffer;
    private static int colorTexture;
    private static int targetWidth;
    private static int targetHeight;
    @Nullable
    private static Method irisInstance;
    @Nullable
    private static Method irisShaderPackInUse;
    private static boolean irisChecked;

    private ElythiaSoftCloudPass() {
    }

    static boolean supported() {
        return !failed && !irisShaderPackActive();
    }

    static void render(List<Draw> draws, Matrix4f viewMatrix, Matrix4f projectionMatrix, Frame frame) {
        if (draws.isEmpty() || !ensureInitialized()) {
            return;
        }

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        int width = Math.max(1, Math.round(main.width * RESOLUTION_SCALE));
        int height = Math.max(1, Math.round(main.height * RESOLUTION_SCALE));
        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int[] previousViewport = new int[4];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer viewport = stack.mallocInt(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            viewport.get(previousViewport);
        }
        if (!ensureTarget(width, height)) {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
            return;
        }

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GlStateManager._viewport(0, 0, width, height);
        GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

        GlStateManager._glUseProgram(coverageProgram);
        setMatrix(viewMatLocation, viewMatrix);
        setMatrix(projMatLocation, projectionMatrix);
        GL20.glUniform1i(sceneDepthLocation, 0);
        GL20.glUniform2f(depthScaleLocation, main.width / (float) width, main.height / (float) height);
        GL20.glUniform2f(sunLightLocation, frame.sunEast(), frame.sunStrength());
        int clearingCount = Math.min(MAX_CLEARINGS, frame.clearingCount());
        GL20.glUniform1i(clearingCountLocation, clearingCount);
        GL20.glUniform1f(clearingStrengthLocation, frame.clearingStrength());
        if (clearingCount > 0) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(clearingCount * 4);
                buffer.put(frame.clearings(), 0, clearingCount * 4).flip();
                GL20.glUniform4fv(clearingsLocation, buffer);
            }
        }
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(main.getDepthTextureId());

        for (Draw draw : draws) {
            GL20.glUniform3f(offsetLocation, draw.offsetX(), draw.offsetY(), draw.offsetZ());
            GL20.glUniform1f(fogEndLocation, draw.fogEnd());
            GL20.glUniform1f(stepLocation, COVERAGE_STEP * 2.0F * draw.opacity());
            GL20.glUniform1f(growthTopLocation, draw.growthTop());
            GL20.glUniform1f(clearableLocation, draw.clearable() ? 1.0F : 0.0F);
            draw.buffer().bind();
            draw.buffer().draw();
        }
        VertexBuffer.unbind();

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
        GlStateManager._viewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);

        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        GlStateManager._glUseProgram(compositeProgram);
        GL20.glUniform1i(coverageLocation, 0);
        GL20.glUniform2f(texelSizeLocation, 1.0F / width, 1.0F / height);
        float r = (float) frame.cloudColor().x;
        float g = (float) frame.cloudColor().y;
        float b = (float) frame.cloudColor().z;
        float[] fog = frame.fogColor();
        GL20.glUniform3f(litColorLocation, r * 0.94F, g * 0.94F, b * 0.95F);
        GL20.glUniform3f(shadowColorLocation,
                r * 0.6F * 0.7F + fog[0] * 0.3F,
                g * 0.58F * 0.7F + fog[1] * 0.3F,
                b * 0.72F * 0.7F + fog[2] * 0.3F);
        GL20.glUniform3f(fogColorLocation, fog[0], fog[1], fog[2]);
        GL20.glUniform1f(opacityLocation, SURFACE_OPACITY);
        GL20.glUniform1f(maxAlphaLocation, MAX_ALPHA);
        GL20.glUniform1f(stepInverseLocation, 1.0F / COVERAGE_STEP);
        GL20.glUniform1f(hazeStrengthLocation, HAZE_STRENGTH);
        GlStateManager._bindTexture(colorTexture);
        GlStateManager._glBindVertexArray(emptyVertexArray);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GlStateManager._glBindVertexArray(0);
        BufferUploader.invalidate();

        GlStateManager._bindTexture(0);
        GlStateManager._glUseProgram(0);
        resetShaderCache();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static boolean ensureTarget(int width, int height) {
        if (framebuffer != 0 && targetWidth == width && targetHeight == height) {
            return true;
        }
        if (framebuffer == 0) {
            framebuffer = GlStateManager.glGenFramebuffers();
            colorTexture = GlStateManager._genTexture();
        }
        GlStateManager._bindTexture(colorTexture);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (IntBuffer) null);
        GlStateManager._bindTexture(0);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture, 0);
        int status = GlStateManager.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            Antarchy.LOGGER.error("Elythia soft cloud target incomplete ({}), falling back to solid clouds", status);
            failed = true;
            return false;
        }
        targetWidth = width;
        targetHeight = height;
        return true;
    }

    private static boolean ensureInitialized() {
        if (initialized) {
            return true;
        }
        if (failed) {
            return false;
        }
        try {
            coverageProgram = link(COVERAGE_VERTEX, COVERAGE_FRAGMENT, true);
            compositeProgram = link(COMPOSITE_VERTEX, COMPOSITE_FRAGMENT, false);
            emptyVertexArray = GlStateManager._glGenVertexArrays();
            viewMatLocation = GL20.glGetUniformLocation(coverageProgram, "ViewMat");
            projMatLocation = GL20.glGetUniformLocation(coverageProgram, "ProjMat");
            offsetLocation = GL20.glGetUniformLocation(coverageProgram, "Offset");
            fogEndLocation = GL20.glGetUniformLocation(coverageProgram, "FogEnd");
            growthTopLocation = GL20.glGetUniformLocation(coverageProgram, "GrowthTop");
            clearableLocation = GL20.glGetUniformLocation(coverageProgram, "Clearable");
            clearingsLocation = GL20.glGetUniformLocation(coverageProgram, "Clearings");
            clearingCountLocation = GL20.glGetUniformLocation(coverageProgram, "ClearingCount");
            clearingStrengthLocation = GL20.glGetUniformLocation(coverageProgram, "ClearingStrength");
            sunLightLocation = GL20.glGetUniformLocation(coverageProgram, "SunLight");
            sceneDepthLocation = GL20.glGetUniformLocation(coverageProgram, "SceneDepth");
            depthScaleLocation = GL20.glGetUniformLocation(coverageProgram, "DepthScale");
            stepLocation = GL20.glGetUniformLocation(coverageProgram, "Step");
            coverageLocation = GL20.glGetUniformLocation(compositeProgram, "Coverage");
            texelSizeLocation = GL20.glGetUniformLocation(compositeProgram, "TexelSize");
            litColorLocation = GL20.glGetUniformLocation(compositeProgram, "LitColor");
            shadowColorLocation = GL20.glGetUniformLocation(compositeProgram, "ShadowColor");
            fogColorLocation = GL20.glGetUniformLocation(compositeProgram, "FogColor");
            opacityLocation = GL20.glGetUniformLocation(compositeProgram, "Opacity");
            maxAlphaLocation = GL20.glGetUniformLocation(compositeProgram, "MaxAlpha");
            stepInverseLocation = GL20.glGetUniformLocation(compositeProgram, "StepInverse");
            hazeStrengthLocation = GL20.glGetUniformLocation(compositeProgram, "HazeStrength");
            initialized = true;
            return true;
        } catch (RuntimeException exception) {
            Antarchy.LOGGER.error("Failed to set up Elythia soft clouds, falling back to solid clouds", exception);
            failed = true;
            return false;
        }
    }

    private static int link(String vertexSource, String fragmentSource, boolean meshAttributes) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, vertexSource);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        if (meshAttributes) {
            GL20.glBindAttribLocation(program, 0, "Position");
            GL20.glBindAttribLocation(program, 1, "UV0");
            GL20.glBindAttribLocation(program, 2, "Color");
            GL20.glBindAttribLocation(program, 3, "Normal");
        }
        GL20.glLinkProgram(program);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program);
            GL20.glDeleteProgram(program);
            throw new IllegalStateException("Elythia cloud shader link failed: " + log);
        }
        return program;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Elythia cloud shader compile failed: " + log);
        }
        return shader;
    }

    private static void setMatrix(int location, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            GL20.glUniformMatrix4fv(location, false, buffer);
        }
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
