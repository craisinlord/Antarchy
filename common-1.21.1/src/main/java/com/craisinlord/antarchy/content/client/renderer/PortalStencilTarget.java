package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class PortalStencilTarget {
    private static final int GL_INCR = 7682;
    private static final int GL_DECR = 7683;
    private static final Map<RenderTarget, Attachment> MAIN_STENCIL_ATTACHMENTS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<RenderTarget, Integer> UNSUPPORTED_MAIN_TARGETS = Collections.synchronizedMap(new IdentityHashMap<>());

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
                Antarchy.LOGGER.debug("Portal gun direct stencil is unavailable ({}); using offscreen portal rendering", exception.getMessage());
            }
        }
    }

    public static boolean supportsDirect(RenderTarget target) {
        target.bindWrite(true);
        return stencilBits() > 0;
    }

    public static void beginFrame(RenderTarget target) {
        target.bindWrite(true);
        RenderSystem.disableScissor();
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        drawInside(0);
    }

    public static void endFrame() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static void pushAperture(int parentLevel, Runnable drawAperture) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, parentLevel, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL_INCR);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        try {
            drawAperture.run();
        } finally {
            drawInside(parentLevel + 1);
        }
    }

    public static void popAperture(int parentLevel, Runnable drawApertureDepth) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, parentLevel + 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL_DECR);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        try {
            drawApertureDepth.run();
        } finally {
            drawInside(parentLevel);
        }
    }

    public static void drawInside(int level) {
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, level, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static void release(RenderTarget target) {
        UNSUPPORTED_MAIN_TARGETS.remove(target);
        Attachment mainAttachment = MAIN_STENCIL_ATTACHMENTS.remove(target);
        if (mainAttachment != null) {
            GL30.glDeleteRenderbuffers(mainAttachment.renderbuffer());
        }
    }

    public static void clear() {
        for (Attachment attachment : MAIN_STENCIL_ATTACHMENTS.values()) {
            GL30.glDeleteRenderbuffers(attachment.renderbuffer());
        }
        MAIN_STENCIL_ATTACHMENTS.clear();
        UNSUPPORTED_MAIN_TARGETS.clear();
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
        } catch (RuntimeException | Error exception) {
            GL30.glDeleteRenderbuffers(renderbuffer);
            throw exception;
        } finally {
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, previousRenderbuffer);
        }
    }

    private static int stencilBits() {
        int type = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT,
                GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
        return type == GL11.GL_NONE ? 0 : GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER,
                GL30.GL_STENCIL_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE);
    }

    private record Attachment(int framebuffer, int renderbuffer, int width, int height) {
    }
}
