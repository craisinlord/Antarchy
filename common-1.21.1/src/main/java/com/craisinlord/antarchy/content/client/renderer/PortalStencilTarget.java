package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import com.mojang.blaze3d.pipeline.TextureTarget;

public final class PortalStencilTarget {
    private static final Map<RenderTarget, Attachment> ATTACHMENTS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Set<RenderTarget> DIRECT_TARGETS = Collections.newSetFromMap(new IdentityHashMap<>());

    private PortalStencilTarget() {
    }

    public static void prepareMainTarget(RenderTarget target) {
        try {
            java.lang.reflect.Method enabled = target.getClass().getMethod("isStencilEnabled");
            if (!Boolean.TRUE.equals(enabled.invoke(target))) {
                target.getClass().getMethod("enableStencil").invoke(target);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        target.bindWrite(true);
    }

    public static Scope beginDirect(RenderTarget target, TextureTarget depthBackup, Runnable drawApertureMask) {
        target.bindWrite(true);
        if (GL11.glGetInteger(GL11.GL_STENCIL_BITS) <= 0) {
            throw new IllegalStateException("Main render target has no stencil attachment");
        }
        depthBackup.copyDepthFrom(target);
        DIRECT_TARGETS.add(target);
        target.bindWrite(true);
        RenderSystem.disableScissor();
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        try {
            Objects.requireNonNull(drawApertureMask).run();
        } catch (RuntimeException | Error exception) {
            resetState();
            target.copyDepthFrom(depthBackup);
            DIRECT_TARGETS.remove(target);
            throw exception;
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderSystem.enableDepthTest();
        return new Scope(target, depthBackup);
    }

    public static Scope begin(RenderTarget target, Runnable drawApertureMask) {
        ensureAttachment(target);
        target.bindWrite(true);
        RenderSystem.disableScissor();
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        try {
            Objects.requireNonNull(drawApertureMask).run();
        } catch (RuntimeException | Error exception) {
            resetState();
            throw exception;
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderSystem.enableDepthTest();
        return new Scope(target);
    }

    public static void activate(RenderTarget target) {
        if (!ATTACHMENTS.containsKey(target) && !DIRECT_TARGETS.contains(target)) {
            return;
        }
        target.bindWrite(true);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    public static void release(RenderTarget target) {
        DIRECT_TARGETS.remove(target);
        Attachment attachment = ATTACHMENTS.remove(target);
        if (attachment != null) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
    }

    public static void clear() {
        for (Attachment attachment : ATTACHMENTS.values()) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
        ATTACHMENTS.clear();
        DIRECT_TARGETS.clear();
    }

    private static void ensureAttachment(RenderTarget target) {
        Attachment attachment = ATTACHMENTS.get(target);
        if (attachment != null
                && attachment.framebuffer() == target.frameBufferId
                && attachment.width() == target.width
                && attachment.height() == target.height) {
            return;
        }
        if (attachment != null) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int previousRenderbuffer = GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING);
        int renderbuffer = GL30.glGenRenderbuffers();
        try {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.frameBufferId);
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, target.width, target.height);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, renderbuffer);
            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                GL30.glDeleteRenderbuffers(renderbuffer);
                throw new IllegalStateException("Portal view framebuffer is incomplete with packed depth/stencil attachment: 0x" + Integer.toHexString(status));
            }
            ATTACHMENTS.put(target, new Attachment(target.frameBufferId, renderbuffer, target.width, target.height));
        } finally {
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, previousRenderbuffer);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
        }
    }

    private static void resetState() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private record Attachment(int framebuffer, int renderbuffer, int width, int height) {
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;
        private final RenderTarget target;
        private final TextureTarget depthBackup;

        private Scope(RenderTarget target) {
            this(target, null);
        }

        private Scope(RenderTarget target, TextureTarget depthBackup) {
            this.target = Objects.requireNonNull(target);
            this.depthBackup = depthBackup;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (this.depthBackup != null) {
                this.target.copyDepthFrom(this.depthBackup);
                this.target.bindWrite(true);
                DIRECT_TARGETS.remove(this.target);
            }
            resetState();
        }
    }
}
