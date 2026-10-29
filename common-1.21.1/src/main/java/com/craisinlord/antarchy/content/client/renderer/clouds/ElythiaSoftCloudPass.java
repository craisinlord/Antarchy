package com.craisinlord.antarchy.content.client.renderer.clouds;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
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
    private static final float RESOLUTION_SCALE = 0.5F;
    private static final float COVERAGE_STEP = 1.0F / 24.0F;
    private static final float SURFACE_OPACITY = 0.22F;
    private static final float MAX_ALPHA = 0.95F;

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
            out float vWeight;
            out float vHeight;
            void main() {
                vec3 rel = Position + Offset;
                gl_Position = ProjMat * ViewMat * vec4(rel, 1.0);
                float fogDistance = max(length(rel.xz), abs(rel.y));
                float fogFade = 1.0 - smoothstep(FogEnd * 0.6, FogEnd, fogDistance);
                float nearFade = smoothstep(6.0, 22.0, length(rel));
                vWeight = fogFade * nearFade;
                vHeight = Color.r;
            }
            """;

    private static final String COVERAGE_FRAGMENT = """
            #version 150
            uniform sampler2D SceneDepth;
            uniform vec2 DepthScale;
            uniform float Step;
            in float vWeight;
            in float vHeight;
            out vec4 fragColor;
            void main() {
                ivec2 size = textureSize(SceneDepth, 0);
                ivec2 coord = clamp(ivec2(gl_FragCoord.xy * DepthScale), ivec2(0), size - ivec2(1));
                if (gl_FragCoord.z > texelFetch(SceneDepth, coord, 0).r) {
                    discard;
                }
                float weight = vWeight * Step;
                fragColor = vec4(weight, weight * vHeight, 0.0, 0.0);
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
            uniform vec3 TopColor;
            uniform vec3 BottomColor;
            uniform float Opacity;
            uniform float MaxAlpha;
            uniform float StepInverse;
            in vec2 texCoord;
            out vec4 fragColor;
            void main() {
                vec4 c = texture(Coverage, texCoord) * 0.4
                        + texture(Coverage, texCoord + vec2(TexelSize.x, 0.0)) * 0.15
                        + texture(Coverage, texCoord - vec2(TexelSize.x, 0.0)) * 0.15
                        + texture(Coverage, texCoord + vec2(0.0, TexelSize.y)) * 0.15
                        + texture(Coverage, texCoord - vec2(0.0, TexelSize.y)) * 0.15;
                float count = c.r * StepInverse;
                if (count < 0.02) {
                    discard;
                }
                float alpha = (1.0 - pow(1.0 - Opacity, count)) * MaxAlpha;
                float height = clamp(c.g / max(c.r, 0.0001), 0.0, 1.0);
                fragColor = vec4(mix(BottomColor, TopColor, height), alpha);
            }
            """;

    record Draw(VertexBuffer buffer, float offsetX, float offsetY, float offsetZ, float fogEnd, float opacity) {
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
    private static int sceneDepthLocation;
    private static int depthScaleLocation;
    private static int stepLocation;
    private static int coverageLocation;
    private static int texelSizeLocation;
    private static int topColorLocation;
    private static int bottomColorLocation;
    private static int opacityLocation;
    private static int maxAlphaLocation;
    private static int stepInverseLocation;
    @Nullable
    private static TextureTarget target;
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

    static void render(List<Draw> draws, Matrix4f viewMatrix, Matrix4f projectionMatrix, Vec3 cloudColor) {
        if (draws.isEmpty() || !ensureInitialized()) {
            return;
        }

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        int width = Math.max(1, Math.round(main.width * RESOLUTION_SCALE));
        int height = Math.max(1, Math.round(main.height * RESOLUTION_SCALE));
        if (target == null) {
            target = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            target.setFilterMode(GL11.GL_LINEAR);
        } else if (target.width != width || target.height != height) {
            target.resize(width, height, Minecraft.ON_OSX);
            target.setFilterMode(GL11.GL_LINEAR);
        }

        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int[] previousViewport = new int[4];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer viewport = stack.mallocInt(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            viewport.get(previousViewport);
        }

        target.bindWrite(true);
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
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(main.getDepthTextureId());

        for (Draw draw : draws) {
            GL20.glUniform3f(offsetLocation, draw.offsetX(), draw.offsetY(), draw.offsetZ());
            GL20.glUniform1f(fogEndLocation, draw.fogEnd());
            GL20.glUniform1f(stepLocation, COVERAGE_STEP * 2.0F * draw.opacity());
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
        float r = (float) cloudColor.x;
        float g = (float) cloudColor.y;
        float b = (float) cloudColor.z;
        GL20.glUniform3f(topColorLocation, r, g, b);
        GL20.glUniform3f(bottomColorLocation, r * 0.8F, g * 0.78F, b * 0.88F);
        GL20.glUniform1f(opacityLocation, SURFACE_OPACITY);
        GL20.glUniform1f(maxAlphaLocation, MAX_ALPHA);
        GL20.glUniform1f(stepInverseLocation, 1.0F / COVERAGE_STEP);
        GlStateManager._bindTexture(target.getColorTextureId());
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
            sceneDepthLocation = GL20.glGetUniformLocation(coverageProgram, "SceneDepth");
            depthScaleLocation = GL20.glGetUniformLocation(coverageProgram, "DepthScale");
            stepLocation = GL20.glGetUniformLocation(coverageProgram, "Step");
            coverageLocation = GL20.glGetUniformLocation(compositeProgram, "Coverage");
            texelSizeLocation = GL20.glGetUniformLocation(compositeProgram, "TexelSize");
            topColorLocation = GL20.glGetUniformLocation(compositeProgram, "TopColor");
            bottomColorLocation = GL20.glGetUniformLocation(compositeProgram, "BottomColor");
            opacityLocation = GL20.glGetUniformLocation(compositeProgram, "Opacity");
            maxAlphaLocation = GL20.glGetUniformLocation(compositeProgram, "MaxAlpha");
            stepInverseLocation = GL20.glGetUniformLocation(compositeProgram, "StepInverse");
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
