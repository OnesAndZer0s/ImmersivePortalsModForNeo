package qouteall.imm_ptl.core.mixin.client.render.framebuffer;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.ducks.IEFrameBuffer;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL30.*;

@Mixin(MainTarget.class)
public abstract class MixinMainTarget extends RenderTarget {
    
    public MixinMainTarget(boolean useDepth) {
        super(useDepth);
        throw new RuntimeException();
    }
    
//    @ModifyArgs( // TODO @Nick1st PRIO Figure out if the redirects below are feasible
//        method = "allocateDepthAttachment",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
//            remap = false
//        )
//    )
//    private void modifyTexImage2D(Args args) {
//        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).ip_getIsStencilBufferEnabled();
//
//        if (isStencilBufferEnabled) {
//            args.set(2, IPCGlobal.useSeparatedStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8);
//            args.set(6, ARBFramebufferObject.GL_DEPTH_STENCIL);
//            args.set(7, IPCGlobal.useSeparatedStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30C.GL_UNSIGNED_INT_24_8);
//        }
//    }
    
    @Redirect(
        method = "Lcom/mojang/blaze3d/pipeline/MainTarget;allocateDepthAttachment(Lcom/mojang/blaze3d/pipeline/MainTarget$Dimension;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
            remap = false
        )
    )
    private void onTexImage2D(
        int target, int level, int internalFormat,
        int width, int height, int border, int format, int type, IntBuffer pixels
    ) {
        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).ip_getIsStencilBufferEnabled();

        if (internalFormat == GL_DEPTH_COMPONENT && isStencilBufferEnabled) {
            GlStateManager._texImage2D(
                target,
                level,
                IPCGlobal.useSeparatedStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8,//
                width,
                height,
                border,
                ARBFramebufferObject.GL_DEPTH_STENCIL,
                IPCGlobal.useSeparatedStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30.GL_UNSIGNED_INT_24_8,//
                pixels
            );
        }
        else {
            GlStateManager._texImage2D(
                target, level, internalFormat, width, height,
                border, format, type, pixels
            );
        }
    }
    
//    @ModifyArgs( // TODO @Nick1st PRIO Figure out if the redirects below are feasible
//        method = "createFrameBuffer",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
//            remap = false
//        )
//    )
//    private void modifyFrameBufferTexture2d(Args args) {
//        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).ip_getIsStencilBufferEnabled();
//
//        if (isStencilBufferEnabled) {
//            if ((int) args.get(1) == GL30.GL_DEPTH_ATTACHMENT) {
//                args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
//            }
//        }
//    }
    
    @Redirect(
        method = "Lcom/mojang/blaze3d/pipeline/MainTarget;createFrameBuffer(II)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
            remap = false
        )
    )
    private void redirectFrameBufferTexture2d(
        int target, int attachment, int textureTarget, int texture, int level
    ) {
        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).ip_getIsStencilBufferEnabled();

        if (attachment == GL30C.GL_DEPTH_ATTACHMENT && isStencilBufferEnabled) {
            GlStateManager._glFramebufferTexture2D(
                target, GL30.GL_DEPTH_STENCIL_ATTACHMENT, textureTarget, texture, level
            );
        }
        else {
            GlStateManager._glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
        }
    }
    
}
