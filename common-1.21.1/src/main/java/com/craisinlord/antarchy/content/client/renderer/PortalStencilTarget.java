package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class PortalStencilTarget {
    private static final Map<RenderTarget, Attachment> ATTACHMENTS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<RenderTarget, Attachment> MAIN_STENCIL_ATTACHMENTS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<RenderTarget, Integer> UNSUPPORTED_MAIN_TARGETS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Set<RenderTarget> DIRECT_TARGETS = Collections.newSetFromMap(new IdentityHashMap<>());

    private PortalStencilTarget() {
    }

    public static void prepareMainTarget(RenderTarget target) {
        target.bindWrite(true);
        if (stencilBits() > 0) {
            UNSUPPORTED_MAIN_TARGETS.remove(target);
            return;
        }
        if (Objects.equals(UNSUPPORTED_MAIN_TARGETS.get(target), target.frameBufferId)) {
            return;
        }
        try {
            java.lang.reflect.Method enabled = target.getClass().getMethod("isStencilEnabled");
            if (!Boolean.TRUE.equals(enabled.invoke(target))) {
                target.getClass().getMethod("enableStencil").invoke(target);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        target.bindWrite(true);
        if (stencilBits() <= 0) {
            try {
                ensureMainStencilAttachment(target);
            } catch (IllegalStateException exception) {
                UNSUPPORTED_MAIN_TARGETS.put(target, target.frameBufferId);
                Antarchy.LOGGER.warn("Portal gun direct stencil is unavailable; using offscreen portal rendering", exception);
            }
        }
    }

    public static boolean supportsDirect(RenderTarget target) {
        target.bindWrite(true);
        return stencilBits() > 0;
    }

    public static Scope beginDirect(RenderTarget target, Runnable drawApertureMask, Runnable clearApertureDepth, Runnable restoreApertureDepth) {
        target.bindWrite(true);
        if (stencilBits() <= 0) {
            throw new IllegalStateException("Main render target has no stencil attachment");
        }
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
        RenderSystem.enableDepthTest();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        try {
            Objects.requireNonNull(drawApertureMask).run();
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(true);
            RenderSystem.stencilMask(0x00);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            Objects.requireNonNull(clearApertureDepth).run();
        } catch (RuntimeException | Error exception) {
            try {
                target.bindWrite(true);
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                RenderSystem.stencilMask(0x00);
                RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
                RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                RenderSystem.colorMask(false, false, false, false);
                RenderSystem.depthMask(true);
                restoreApertureDepth.run();
            } catch (RuntimeException | Error restoreFailure) {
                exception.addSuppressed(restoreFailure);
            } finally {
                DIRECT_TARGETS.remove(target);
                resetState();
            }
            throw exception;
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.enableDepthTest();
        return new Scope(target, restoreApertureDepth);
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
        UNSUPPORTED_MAIN_TARGETS.remove(target);
        Attachment attachment = ATTACHMENTS.remove(target);
        if (attachment != null) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
        Attachment mainAttachment = MAIN_STENCIL_ATTACHMENTS.remove(target);
        if (mainAttachment != null) {
            GL30.glDeleteRenderbuffers(mainAttachment.renderbuffer());
        }
    }

    public static void clear() {
        for (Attachment attachment : ATTACHMENTS.values()) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
        ATTACHMENTS.clear();
        for (Attachment attachment : MAIN_STENCIL_ATTACHMENTS.values()) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
        MAIN_STENCIL_ATTACHMENTS.clear();
        UNSUPPORTED_MAIN_TARGETS.clear();
        DIRECT_TARGETS.clear();
    }

    private static void ensureMainStencilAttachment(RenderTarget target) {
        Attachment attachment = MAIN_STENCIL_ATTACHMENTS.get(target);
        if (attachment != null
                && attachment.framebuffer() == target.frameBufferId
                && attachment.width() == target.width
                && attachment.height() == target.height) {
            throw new IllegalStateException("Main render target stencil attachment has no stencil bits");
        }
        if (attachment != null) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
            MAIN_STENCIL_ATTACHMENTS.remove(target);
        }
        int previousRenderbuffer = GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING);
        int renderbuffer = GL30.glGenRenderbuffers();
        try {
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_STENCIL_INDEX8, target.width, target.height);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, renderbuffer);
            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, 0);
                throw new IllegalStateException("Main render target is incomplete with stencil attachment: 0x" + Integer.toHexString(status));
            }
            MAIN_STENCIL_ATTACHMENTS.put(target, new Attachment(target.frameBufferId, renderbuffer, target.width, target.height));
            PortalSceneRenderTrace.event("main-target", "stencil-attached", "framebuffer=" + target.frameBufferId + " size=" + target.width + "x" + target.height);
        } catch (RuntimeException | Error exception) {
            GL30.glDeleteRenderbuffers(renderbuffer);
            throw exception;
        } finally {
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, previousRenderbuffer);
        }
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

    private static int stencilBits() {
        int type = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT,
                GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
        return type == GL11.GL_NONE ? 0 : GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER,
                GL30.GL_STENCIL_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE);
    }

    private record Attachment(int framebuffer, int renderbuffer, int width, int height) {
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;
        private final RenderTarget target;
        private final Runnable restoreApertureDepth;

        private Scope(RenderTarget target) {
            this(target, null);
        }

        private Scope(RenderTarget target, Runnable restoreApertureDepth) {
            this.target = Objects.requireNonNull(target);
            this.restoreApertureDepth = restoreApertureDepth;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                if (this.restoreApertureDepth != null) {
                    this.target.bindWrite(true);
                    GL11.glEnable(GL11.GL_STENCIL_TEST);
                    RenderSystem.stencilMask(0x00);
                    RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
                    RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                    RenderSystem.colorMask(false, false, false, false);
                    RenderSystem.depthMask(true);
                    this.restoreApertureDepth.run();
                }
            } finally {
                if (this.restoreApertureDepth != null) {
                    DIRECT_TARGETS.remove(this.target);
                }
                resetState();
            }
        }
    }
}
